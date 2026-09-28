package me.vovari2.naturaltracker.settings;

import me.vovari2.naturaltracker.utils.FileUtils;
import org.bukkit.configuration.file.YamlConfiguration;

final class Loader {
    private static final String RESOURCE_NAME = "settings.yml";

    private final YamlConfiguration config;

    Loader() throws Exception {
        config = FileUtils.loadYamlFile(RESOURCE_NAME);

        Settings.CHANGES.MAX_SIZE = config.getInt("changes.max_size", 1_000_000);
        Settings.CHANGES.BUFFER = config.getInt("changes.buffer", 10_000);
    }
}
