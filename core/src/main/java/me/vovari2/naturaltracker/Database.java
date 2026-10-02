package me.vovari2.naturaltracker;

import io.agroal.api.AgroalDataSource;
import io.agroal.api.configuration.AgroalConnectionFactoryConfiguration;
import io.agroal.api.configuration.AgroalDataSourceConfiguration;
import io.agroal.api.configuration.supplier.AgroalDataSourceConfigurationSupplier;
import io.agroal.api.security.NamePrincipal;
import io.agroal.api.security.SimplePassword;
import me.vovari2.naturaltracker.settings.Settings;

import java.sql.*;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class Database {
    private static AgroalDataSource DATA_SOURCE;

    private static final Set<String> BUFFER = new HashSet<>();
    private static volatile int BUFFER_SIZE = 100;

    public static void enable() {
        initialize();
    }
    public static void reload() {
        // Закрываем старый источник данных, если он был, при этом сохраняя буффер в БД
        if (DATA_SOURCE != null) {
            flushRemaining();
            try {
                DATA_SOURCE.close();
            } catch (Exception e) {
                Console.warn("Не получилось закрыть старый источник данных!", e);
            }
        }
        initialize();
    }
    public static void disable() {
        if (DATA_SOURCE != null) {
            flushRemaining();
            try {
                DATA_SOURCE.close();
                Console.info("Источник данных успешно закрыт.");
            } catch (Exception e) {
                Console.warn("Ошибка при закрытии: ", e);
            }
        }
    }

    private static void initialize(){
        BUFFER_SIZE = Settings.DATABASE.BUFFER;

        DatabaseType type = Settings.DATABASE.TYPE;
        AgroalDataSourceConfigurationSupplier configuration = new AgroalDataSourceConfigurationSupplier()
                .dataSourceImplementation( AgroalDataSourceConfiguration.DataSourceImplementation.AGROAL )
                .metricsEnabled( false )
                .connectionPoolConfiguration( cp -> cp.minSize(Settings.DATABASE.POOL.MIN_SIZE)
                        .maxSize(Settings.DATABASE.POOL.MAX_SIZE)
                        .acquisitionTimeout(Duration.of(Settings.DATABASE.POOL.TIMEOUT_TIME, ChronoUnit.SECONDS))
                        .connectionFactoryConfiguration( cf -> {
                            cf.jdbcUrl(type.buildUrl(Settings.DATABASE.URL))
                                    .connectionProviderClassName(type.driverClass())
                                    .jdbcTransactionIsolation(AgroalConnectionFactoryConfiguration.TransactionIsolation.SERIALIZABLE)
                                    .principal( new NamePrincipal(Settings.DATABASE.USER) )
                                    .credential(new SimplePassword(Settings.DATABASE.PASSWORD));

                            switch (type) {
                                case SQLITE -> cf.initialSql("PRAGMA journal_mode = WAL; PRAGMA foreign_keys = ON;");
                                case MYSQL -> cf.jdbcUrl(cf.get().jdbcUrl() + "?useServerPrepStmts=true&cachePrepStmts=true");
                            }
                            return cf;
                        })
                );

        try {
            DATA_SOURCE = AgroalDataSource.from(configuration.get());

            try (Connection conn = DATA_SOURCE.getConnection()) {
                try (Statement stmt = conn.createStatement()) {
                    stmt.execute(type.queryCreateTable());
                }
                Console.info("Таблицы в базе данных успешно инициализированы!");
            }

        } catch (SQLException e) {
            DATA_SOURCE = null;
            Console.warn("Критическая ошибка при инициализации базы данных: ", e);
        }
    }
    private static void flushRemaining() {
        if (DATA_SOURCE == null) return;
        try (Connection conn = DATA_SOURCE.getConnection()) {
            flush(conn);
        } catch (SQLException e) {
            Console.error("Не получилось сбросить буфер БД!", e);
        }
    }

    public static CheckResult checkOrInsertPosition(String pos){
        if (DATA_SOURCE == null)
            return CheckResult.DB_UNAVAILABLE;

        try (Connection conn = DATA_SOURCE.getConnection()){
            if (existsInDatabase(conn, pos))
                return CheckResult.EXISTS;

            BUFFER.add(pos);
            if (BUFFER.size() >= BUFFER_SIZE)
                flush(conn);

            return CheckResult.NOT_EXISTS;
        } catch (SQLException e) {
            Console.error("Не получилось подключиться к БД!", e);
            return CheckResult.DB_UNAVAILABLE;
        }
    }

    private static boolean existsInDatabase(Connection conn, String pos) throws SQLException {
        String query = Settings.DATABASE.TYPE.querySelect();
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setQueryTimeout(Settings.DATABASE.TIMEOUT);
            ps.setString(1, pos);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }
    private static void flush(Connection conn) {
        if (BUFFER.isEmpty()) return;

        // Делаем снимок и очищаем буфер до записи.
        // Если insertBatch упадёт — данные потеряются. Это осознанное решение: позиции перезапишутся при следующем has для тех же блоков.
        Set<String> snapshot = new HashSet<>(BUFFER);
        BUFFER.clear();

        insertBatch(conn, snapshot);
    }
    private static void insertBatch(Connection conn, Set<String> batch) {
        if (batch.isEmpty()) return;

        String query = Settings.DATABASE.TYPE.queryInsert();
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setQueryTimeout(Settings.DATABASE.TIMEOUT);
            for (String pos : batch) {
                ps.setString(1, pos);
                ps.addBatch();
            }
            ps.executeBatch();

        } catch (SQLException e) {
            Console.error("Не получилось записать %s позиций в БД!".formatted(batch.size()), e);
        }
    }
    public enum CheckResult { EXISTS, NOT_EXISTS, DB_UNAVAILABLE }
}
