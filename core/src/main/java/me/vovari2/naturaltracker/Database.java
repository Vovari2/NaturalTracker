package me.vovari2.naturaltracker;

import io.agroal.api.AgroalDataSource;
import io.agroal.api.configuration.AgroalConnectionFactoryConfiguration;
import io.agroal.api.configuration.AgroalDataSourceConfiguration;
import io.agroal.api.configuration.supplier.AgroalDataSourceConfigurationSupplier;
import io.agroal.api.security.NamePrincipal;
import io.agroal.api.security.SimplePassword;
import me.vovari2.naturaltracker.changes.ChunkEntry;
import me.vovari2.naturaltracker.changes.ChunkKey;
import me.vovari2.naturaltracker.changes.ChunkSnapshot;
import me.vovari2.naturaltracker.settings.Settings;
import me.vovari2.naturaltracker.utils.UUIDUtils;
import org.jetbrains.annotations.Nullable;

import java.sql.*;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.BitSet;
import java.util.List;
import java.util.zip.DataFormatException;

public class Database {
    private static AgroalDataSource DATA_SOURCE;

    // Снимок настроек на момент создания пула: при reload Settings меняются раньше, чем пересоздаётся пул
    private static DatabaseType TYPE;
    private static int TIMEOUT;
    private static boolean LIMIT_ENABLED;
    private static int DATABASE_SIZE;

    public static void enable() {
        initialize();
    }
    public static void reload() {
        disable();
        initialize();
    }
    public static void disable() {
        if (DATA_SOURCE != null) {
            try {
                DATA_SOURCE.close();
                Console.info("Источник данных успешно закрыт!");
            } catch (Exception e) {
                Console.warn("Не получилось закрыть старый источник данных!", e);
            }
            DATA_SOURCE = null;
        }
    }

    private static void initialize(){
        LIMIT_ENABLED = Settings.DATABASE.LIMIT;
        DATABASE_SIZE = Settings.DATABASE.LIMIT_SIZE;

        TYPE = Settings.DATABASE.TYPE;
        TIMEOUT = Settings.DATABASE.POOL.TIMEOUT_QUERY;

        DatabaseType type = TYPE;
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
                                    .jdbcTransactionIsolation(AgroalConnectionFactoryConfiguration.TransactionIsolation.SERIALIZABLE);

                            if (type.requiresCredentials())
                                cf.principal(new NamePrincipal(Settings.DATABASE.USER))
                                        .credential(new SimplePassword(Settings.DATABASE.PASSWORD));

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
                if (type.queryCreateIndex() != null)
                    try (Statement stmt = conn.createStatement()) {
                        stmt.execute(type.queryCreateIndex());
                    }
                Console.info("Таблицы в базе данных успешно инициализированы!");

                // Если database.size уменьшили или лимит включили после перезапуска, лишнее удалится сразу
                enforceSizeLimit(conn);
            }

        } catch (Exception e) {
            if (DATA_SOURCE != null)
                DATA_SOURCE.close();
            DATA_SOURCE = null;
            Console.error("Не удалось инициализировать базу данных!", e);
        }
    }

    /** Только из потока воркера. null — в БД чанка нет (или БД недоступна). */
    public static @Nullable BitSet loadChunk(ChunkKey key) throws SQLException, DataFormatException {
        if (DATA_SOURCE == null) return null;

        try (Connection conn = DATA_SOURCE.getConnection()) {
            byte[] data = selectChunk(conn, key);
            return data == null ? null : ChunkEntry.decode(data);
        }
    }
    /** Только из потока воркера. Вся пачка пишется одной транзакцией. false, если записать не получилось. */
    public static boolean saveChunks(List<ChunkSnapshot> snapshots) {
        if (snapshots.isEmpty()) return true;
        if (DATA_SOURCE == null) return false;

        try (Connection conn = DATA_SOURCE.getConnection()) {
            conn.setAutoCommit(false);
            try {
                long now = System.currentTimeMillis();
                for (ChunkSnapshot snapshot : snapshots)
                    writeChunk(conn, snapshot, now);
                conn.commit();
            } catch (Exception e) {
                conn.rollback();
                Console.error("Не получилось записать %d чанков в БД!".formatted(snapshots.size()), e);
                return false;
            } finally {
                conn.setAutoCommit(true);
            }

            enforceSizeLimit(conn);
            return true;
        } catch (SQLException e) {
            Console.error("Не получилось сохранить чанки в БД!", e);
            return false;
        }
    }

    private static void writeChunk(Connection conn, ChunkSnapshot snapshot, long now) throws SQLException, DataFormatException {
        ChunkKey key = snapshot.key();
        byte[] raw = snapshot.raw();

        if (snapshot.merge()) {
            byte[] old = selectChunk(conn, key);
            if (old != null) {
                BitSet merged = ChunkEntry.decode(old);
                merged.or(BitSet.valueOf(raw));
                raw = merged.toByteArray();
            }
        }

        // Удаляем чанк из БД, если тот пустой
        if (raw.length == 0) {
            try (PreparedStatement ps = conn.prepareStatement(TYPE.queryDelete())) {
                ps.setQueryTimeout(TIMEOUT);
                setKeyParams(ps, key);
                ps.executeUpdate();
            }
            return;
        }

        // Обновляем данные уже существующего чанка
        try (PreparedStatement ps = conn.prepareStatement(TYPE.queryUpsert())) {
            ps.setQueryTimeout(TIMEOUT);
            setKeyParams(ps, key);
            ps.setBytes(4, ChunkEntry.encode(raw));
            ps.setLong(5, now);
            ps.executeUpdate();
        }
    }
    private static @Nullable byte[] selectChunk(Connection conn, ChunkKey key) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(TYPE.querySelect())) {
            ps.setQueryTimeout(TIMEOUT);
            setKeyParams(ps, key);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getBytes(1) : null;
            }
        }
    }
    private static void enforceSizeLimit(Connection conn) {
        if (!LIMIT_ENABLED) return;

        try {
            long count;
            try (PreparedStatement ps = conn.prepareStatement(TYPE.queryCount())) {
                ps.setQueryTimeout(TIMEOUT);
                try (ResultSet rs = ps.executeQuery()) {
                    count = rs.next() ? rs.getLong(1) : 0;
                }
            }

            // Удаляем чанки, которые не обновлялись дольше всех
            long excess = count - DATABASE_SIZE;
            if (excess <= 0) return;

            try (PreparedStatement ps = conn.prepareStatement(TYPE.queryDeleteOldest())) {
                ps.setQueryTimeout(TIMEOUT);
                ps.setLong(1, excess);
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            Console.error("Не получилось подрезать таблицу до %d чанков!".formatted(DATABASE_SIZE), e);
        }
    }

    private static void setKeyParams(PreparedStatement ps, ChunkKey key) throws SQLException {
        ps.setBytes(1, UUIDUtils.toBytes(key.world()));
        ps.setInt(2, key.x());
        ps.setInt(3, key.z());
    }
}
