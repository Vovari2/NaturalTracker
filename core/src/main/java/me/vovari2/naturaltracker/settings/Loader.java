package me.vovari2.naturaltracker.settings;

import me.vovari2.naturaltracker.DatabaseType;
import me.vovari2.naturaltracker.NaturalTracker;
import me.vovari2.naturaltracker.utils.FileUtils;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;

final class Loader {
    private static final String RESOURCE_NAME = "settings.yml";

    private final YamlConfiguration config;

    Loader() throws Exception {
        config = FileUtils.loadYamlFile(RESOURCE_NAME);

        Settings.CHANGES.MAX_SIZE = config.getInt("changes.max_size", 1_000_000);
        Settings.CHANGES.BUFFER = config.getInt("changes.buffer", 10_000);

        Settings.DATABASE.TYPE = parseDatabaseType("database.type", DatabaseType.SQLITE);
        Settings.DATABASE.URL = config.getString("database.url", Path.of(NaturalTracker.getInstance().getDataFolder().toString(), "data.db").toString());
        Settings.DATABASE.USER = config.getString("database.user", "root");
        Settings.DATABASE.PASSWORD = config.getString("database.password", "");
        Settings.DATABASE.BUFFER = config.getInt("database.buffer", 20);

        Settings.DATABASE.POOL.MIN_SIZE = config.getInt("database.pool.min_size", 2);
        Settings.DATABASE.POOL.MAX_SIZE = config.getInt("database.pool.max_size", 10);
        Settings.DATABASE.POOL.TIMEOUT_TIME = config.getInt("database.pool.timeout_time", 10);
    }
    private @NotNull DatabaseType parseDatabaseType(@NotNull String path, @NotNull DatabaseType def){
        @Nullable String strType = config.getString(path);
        if (strType == null) return def;

        @Nullable DatabaseType type = DatabaseType.of(strType);
        if (type == null) return def;

        return type;
    }
}
