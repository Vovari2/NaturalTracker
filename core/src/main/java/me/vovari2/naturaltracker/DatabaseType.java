package me.vovari2.naturaltracker;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public enum DatabaseType {
    SQLITE(
            "jdbc:sqlite:",
            "org.sqlite.JDBC",
            false,
            "INSERT OR IGNORE INTO changes (position) VALUES (?);",
            "SELECT id FROM changes WHERE position = ?;",
            """
                CREATE TABLE IF NOT EXISTS changes (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    position TEXT NOT NULL UNIQUE
                );
            """
    ),
    MYSQL(
            "jdbc:mysql://",
            "com.mysql.cj.jdbc.Driver",
            true,
            "INSERT IGNORE INTO changes (position) VALUES (?);",
            "SELECT id FROM changes WHERE position = ?;",
            """
                CREATE TABLE IF NOT EXISTS changes (
                    id BIGINT AUTO_INCREMENT PRIMARY KEY,
                    position VARCHAR(255) NOT NULL UNIQUE
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
            """
    ),
    POSTGRESQL(
            "jdbc:postgresql://",
            "org.postgresql.Driver",
            true,
            "INSERT INTO changes (position) VALUES (?) ON CONFLICT (position) DO NOTHING;",
            "SELECT id FROM changes WHERE position = ?;",
            """
                CREATE TABLE IF NOT EXISTS changes (
                    id BIGSERIAL PRIMARY KEY,
                    position VARCHAR(255) NOT NULL UNIQUE
                )
            """
    );

    private final String urlPrefix;
    private final String driverClass;
    private final boolean requiresCredentials;
    private final String queryInsert;
    private final String querySelect;
    private final String queryCreateTable;

    DatabaseType(String urlPrefix, String driverClass, boolean requiresCredentials, String queryInsert, String querySelect,String queryCreateTable) {
        this.urlPrefix = urlPrefix;
        this.driverClass = driverClass;
        this.requiresCredentials = requiresCredentials;
        this.queryInsert = queryInsert;
        this.querySelect = querySelect;
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
    public String queryCreateTable(){
        return queryCreateTable;
    }



    public static @Nullable DatabaseType of(@NotNull String name) {
        try {return valueOf(name.toUpperCase());}
        catch (IllegalArgumentException e) { return null;}
    }
}
