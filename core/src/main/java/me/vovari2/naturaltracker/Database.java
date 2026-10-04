package me.vovari2.naturaltracker;

import io.agroal.api.AgroalDataSource;
import io.agroal.api.configuration.AgroalConnectionFactoryConfiguration;
import io.agroal.api.configuration.AgroalDataSourceConfiguration;
import io.agroal.api.configuration.supplier.AgroalDataSourceConfigurationSupplier;
import io.agroal.api.security.NamePrincipal;
import io.agroal.api.security.SimplePassword;
import me.vovari2.naturaltracker.changes.Position;
import me.vovari2.naturaltracker.settings.Settings;

import java.sql.*;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class Database {
    private static AgroalDataSource DATA_SOURCE;

    private static final Set<Position> BUFFER = new HashSet<>();
    private static volatile int BUFFER_SIZE;
    private static volatile int DATABASE_SIZE;

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
        BUFFER_SIZE = Settings.DATABASE.BUFFER_SIZE;
        DATABASE_SIZE = Settings.DATABASE.SIZE;

        DatabaseType type = Settings.DATABASE.TYPE;
        String url = type.buildUrl(Settings.DATABASE.URL);
        if (type == DatabaseType.MYSQL)
            url += (url.contains("?") ? "&" : "?") + "useServerPrepStmts=true&cachePrepStmts=true";

        final String jdbcUrl = url;
        AgroalDataSourceConfigurationSupplier configuration = new AgroalDataSourceConfigurationSupplier()
                .dataSourceImplementation( AgroalDataSourceConfiguration.DataSourceImplementation.AGROAL )
                .metricsEnabled( false )
                .connectionPoolConfiguration( cp -> cp.minSize(Settings.DATABASE.POOL.MIN_SIZE)
                        .maxSize(Settings.DATABASE.POOL.MAX_SIZE)
                        .acquisitionTimeout(Duration.of(Settings.DATABASE.POOL.TIMEOUT_TIME, ChronoUnit.SECONDS))
                        .connectionFactoryConfiguration( cf -> {
                            cf.jdbcUrl(jdbcUrl)
                                    .connectionProviderClassName(type.driverClass())
                                    .jdbcTransactionIsolation(AgroalConnectionFactoryConfiguration.TransactionIsolation.SERIALIZABLE)
                                    .principal( new NamePrincipal(Settings.DATABASE.USER) )
                                    .credential(new SimplePassword(Settings.DATABASE.PASSWORD));

                            // sqlite-jdbc выполняет только первый оператор, поэтому одна PRAGMA (внешних ключей в схеме нет)
                            if (type == DatabaseType.SQLITE)
                                cf.initialSql("PRAGMA journal_mode = WAL;");
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

    public static void insertPosition(Position pos){
        if (DATA_SOURCE == null)
            return;

        BUFFER.add(pos);
        if (BUFFER.size() >= BUFFER_SIZE)
            flushRemaining();
    }

    private static void flush(Connection conn) {
        if (BUFFER.isEmpty()) return;

        // Делаем снимок и очищаем буфер до записи.
        // Если insertBatch упадёт — данные потеряются. Это осознанное решение: позиции перезапишутся при следующем has для тех же блоков.
        Set<Position> snapshot = new HashSet<>(BUFFER);
        BUFFER.clear();

        insertBatch(conn, snapshot);
    }
    private static void insertBatch(Connection conn, Set<Position> batch) {
        if (batch.isEmpty()) return;

        String query = Settings.DATABASE.TYPE.queryInsert();
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setQueryTimeout(Settings.DATABASE.TIMEOUT);
            for (Position pos : batch) {
                setPositionParams(ps, pos);
                ps.addBatch();
            }
            ps.executeBatch();

        } catch (SQLException e) {
            Console.error("Не получилось записать %s позиций в БД!".formatted(batch.size()), e);
        }

        enforceSizeLimit(conn);
    }
    private static void enforceSizeLimit(Connection conn) {
        String countQuery = Settings.DATABASE.TYPE.queryCount();
        String deleteQuery = Settings.DATABASE.TYPE.queryDeleteOldest();

        try {
            int count;
            try (PreparedStatement ps = conn.prepareStatement(countQuery);
                 ResultSet rs = ps.executeQuery()) {
                rs.next();
                count = rs.getInt(1);
            }

            int excess = count - DATABASE_SIZE;
            if (excess <= 0) return;

            try (PreparedStatement ps = conn.prepareStatement(deleteQuery)) {
                ps.setQueryTimeout(Settings.DATABASE.TIMEOUT);
                ps.setInt(1, excess);
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            Console.error("Не получилось подрезать таблицу до %d записей!".formatted(DATABASE_SIZE), e);
        }
    }

    private static void setPositionParams(PreparedStatement ps, Position pos) throws SQLException {
        ps.setBytes(1, pos.world());
        ps.setInt(2, pos.x());
        ps.setInt(3, pos.y());
        ps.setInt(4, pos.z());
    }
}
