package me.vovari2.naturaltracker;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public enum DatabaseType {
    SQLITE(
            "jdbc:sqlite:",
            "org.sqlite.JDBC",
            false,
            "SELECT data FROM chunks WHERE world = ? AND cx = ? AND cz = ?;",
            "INSERT INTO chunks (world, cx, cz, data, timestamp) VALUES (?, ?, ?, ?, ?) ON CONFLICT (world, cx, cz) DO UPDATE SET data = excluded.data, timestamp = excluded.timestamp;",
            "DELETE FROM chunks WHERE world = ? AND cx = ? AND cz = ?;",
            "SELECT COUNT(*) FROM chunks;",
            "DELETE FROM chunks WHERE (world, cx, cz) IN (SELECT world, cx, cz FROM chunks ORDER BY timestamp LIMIT ?);",
            """
                CREATE TABLE IF NOT EXISTS chunks (
                    world BLOB NOT NULL,
                    cx INTEGER NOT NULL,
                    cz INTEGER NOT NULL,
                    data BLOB NOT NULL,
                    timestamp INTEGER NOT NULL,
                    PRIMARY KEY (world, cx, cz)
                );
            """,
            "CREATE INDEX IF NOT EXISTS idx_chunks_timestamp ON chunks (timestamp);"
    ),
    MYSQL(
            "jdbc:mysql://",
            "com.mysql.cj.jdbc.Driver",
            true,
            "SELECT data FROM chunks WHERE world = ? AND cx = ? AND cz = ?;",
            "INSERT INTO chunks (world, cx, cz, data, timestamp) VALUES (?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE data = VALUES(data), timestamp = VALUES(timestamp);",
            "DELETE FROM chunks WHERE world = ? AND cx = ? AND cz = ?;",
            "SELECT COUNT(*) FROM chunks;",
            "DELETE FROM chunks ORDER BY timestamp LIMIT ?;",
            """
                CREATE TABLE IF NOT EXISTS chunks (
                    world BINARY(16) NOT NULL,
                    cx INT NOT NULL,
                    cz INT NOT NULL,
                    data MEDIUMBLOB NOT NULL,
                    timestamp BIGINT NOT NULL,
                    PRIMARY KEY (world, cx, cz),
                    INDEX idx_timestamp (timestamp)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
            """,
            null
    ),
    POSTGRESQL(
            "jdbc:postgresql://",
            "org.postgresql.Driver",
            true,
            "SELECT data FROM chunks WHERE world = ? AND cx = ? AND cz = ?;",
            "INSERT INTO chunks (world, cx, cz, data, timestamp) VALUES (?, ?, ?, ?, ?) ON CONFLICT (world, cx, cz) DO UPDATE SET data = excluded.data, timestamp = excluded.timestamp;",
            "DELETE FROM chunks WHERE world = ? AND cx = ? AND cz = ?;",
            "SELECT COUNT(*) FROM chunks;",
            "DELETE FROM chunks WHERE (world, cx, cz) IN (SELECT world, cx, cz FROM chunks ORDER BY timestamp LIMIT ?);",
            """
                CREATE TABLE IF NOT EXISTS chunks (
                    world BYTEA NOT NULL,
                    cx INT NOT NULL,
                    cz INT NOT NULL,
                    data BYTEA NOT NULL,
                    timestamp BIGINT NOT NULL,
                    PRIMARY KEY (world, cx, cz)
                )
            """,
            "CREATE INDEX IF NOT EXISTS idx_chunks_timestamp ON chunks (timestamp)"
    );

    private final String urlPrefix;
    private final String driverClass;
    private final boolean requiresCredentials;
    private final String querySelect;
    private final String queryUpsert;
    private final String queryDelete;
    private final String queryCount;
    private final String queryDeleteOldest;
    private final String queryCreateTable;
    private final @Nullable String queryCreateIndex;

    DatabaseType(String urlPrefix, String driverClass, boolean requiresCredentials,
                 String querySelect, String queryUpsert, String queryDelete,
                 String queryCount, String queryDeleteOldest,
                 String queryCreateTable, @Nullable String queryCreateIndex) {
        this.urlPrefix = urlPrefix;
        this.driverClass = driverClass;
        this.requiresCredentials = requiresCredentials;
        this.querySelect = querySelect;
        this.queryUpsert = queryUpsert;
        this.queryDelete = queryDelete;
        this.queryCount = queryCount;
        this.queryDeleteOldest = queryDeleteOldest;
        this.queryCreateTable = queryCreateTable;
        this.queryCreateIndex = queryCreateIndex;
    }
    public String buildUrl(String path) {
        return urlPrefix + path;
    }

    public String driverClass() { return driverClass; }
    public boolean requiresCredentials() { return requiresCredentials; }
    public String querySelect() { return querySelect; }
    public String queryUpsert() { return queryUpsert; }
    public String queryDelete() { return queryDelete; }
    public String queryCount() { return queryCount; }
    public String queryDeleteOldest() { return queryDeleteOldest; }
    public String queryCreateTable() { return queryCreateTable; }
    public @Nullable String queryCreateIndex() { return queryCreateIndex; }



    public static @Nullable DatabaseType of(@NotNull String name) {
        try {return valueOf(name.toUpperCase());}
        catch (IllegalArgumentException e) { return null;}
    }
}
