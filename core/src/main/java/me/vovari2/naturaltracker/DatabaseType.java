package me.vovari2.naturaltracker;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public enum DatabaseType {
    SQLITE(
            "jdbc:sqlite:",
            "org.sqlite.JDBC",
            false,
            "INSERT OR IGNORE INTO changes (world, x, y, z) VALUES (?, ?, ?, ?);",
            "SELECT id FROM changes WHERE world = ? AND x = ? AND y = ? AND z = ?;",
            "SELECT COUNT(id) FROM changes;",
            """
                DELETE FROM changes
                WHERE id IN (
                    SELECT id FROM changes
                    ORDER BY id ASC
                    LIMIT ?
                );
            """,
            """
                CREATE TABLE IF NOT EXISTS changes (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    world BLOB NOT NULL,
                    x INTEGER NOT NULL,
                    y INTEGER NOT NULL,
                    z INTEGER NOT NULL,
                    UNIQUE (world, x, y, z)
                );
            """
    ),
    MYSQL(
            "jdbc:mysql://",
            "com.mysql.cj.jdbc.Driver",
            true,
            "INSERT IGNORE INTO changes (world, x, y, z) VALUES (?, ?, ?, ?);",
            "SELECT id FROM changes WHERE world = ? AND x = ? AND y = ? AND z = ?;",
            "SELECT COUNT(id) FROM changes;",
            """
                DELETE FROM changes
                ORDER BY id ASC
                LIMIT ?;
            """,
            """
                CREATE TABLE IF NOT EXISTS changes (
                    id BIGINT AUTO_INCREMENT PRIMARY KEY,
                    world BINARY(16) NOT NULL,
                    x INT NOT NULL,
                    y INT NOT NULL,
                    z INT NOT NULL,
                    UNIQUE KEY idx_position (world, x, y, z)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
            """
    ),
    POSTGRESQL(
            "jdbc:postgresql://",
            "org.postgresql.Driver",
            true,
            "INSERT INTO changes (world, x, y, z) VALUES (?, ?, ?, ?) ON CONFLICT (world, x, y, z) DO NOTHING;",
            "SELECT id FROM changes WHERE world = ? AND x = ? AND y = ? AND z = ?;",
            "SELECT COUNT(id) FROM changes;",
            """
                DELETE FROM changes
                WHERE id IN (
                    SELECT id FROM changes
                    ORDER BY id ASC
                    LIMIT ?
                );
            """,
            """
                CREATE TABLE IF NOT EXISTS changes (
                    id BIGSERIAL PRIMARY KEY,
                    world BYTEA NOT NULL,
                    x INT NOT NULL,
                    y INT NOT NULL,
                    z INT NOT NULL,
                    UNIQUE (world, x, y, z)
                )
            """
    );

    private final String urlPrefix;
    private final String driverClass;
    private final boolean requiresCredentials;
    private final String queryInsert;
    private final String querySelect;
    private final String queryCount;
    private final String queryDeleteOldest;
    private final String queryCreateTable;

    DatabaseType(String urlPrefix, String driverClass, boolean requiresCredentials,
                 String queryInsert, String querySelect,
                 String queryCount, String queryDeleteOldest,
                 String queryCreateTable) {
        this.urlPrefix = urlPrefix;
        this.driverClass = driverClass;
        this.requiresCredentials = requiresCredentials;
        this.queryInsert = queryInsert;
        this.querySelect = querySelect;
        this.queryCount = queryCount;
        this.queryDeleteOldest = queryDeleteOldest;
        this.queryCreateTable = queryCreateTable;
    }
    public String buildUrl(String path) {
        return urlPrefix + path;
    }

    public String driverClass() { return driverClass; }
    public boolean requiresCredentials() { return requiresCredentials; }
    public String queryInsert(){
        return queryInsert;
    }
    public String querySelect(){
        return querySelect;
    }
    public String queryCount() { return queryCount; }
    public String queryDeleteOldest() { return queryDeleteOldest; }
    public String queryCreateTable(){
        return queryCreateTable;
    }



    public static @Nullable DatabaseType of(@NotNull String name) {
        try {return valueOf(name.toUpperCase());}
        catch (IllegalArgumentException e) { return null;}
    }
}
