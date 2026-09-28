package me.vovari2.naturaltracker.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.vovari2.naturaltracker.Console;
import me.vovari2.naturaltracker.NaturalTracker;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class FileUtils {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static @NotNull Path getPluginDirectory(){
        return NaturalTracker.getInstance().getDataFolder().toPath();
    }

    public static void createDirectory(@NotNull Path directory){
        if (Files.exists(directory))
            return;

        try { Files.createDirectories(directory); }
        catch(IOException e){ Console.warn("Не удалось создать директорию '%s'!".formatted(directory.getFileName())); }
    }

    public static @NotNull YamlConfiguration loadYamlResource(@NotNull String resource) throws Exception {
        InputStream stream = NaturalTracker.class.getClassLoader().getResourceAsStream(resource);
        if (stream == null)
            throw new Exception("Ресурс '%s' не найден!".formatted(resource));

        try (InputStreamReader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)){
            return YamlConfiguration.loadConfiguration(reader);
        }
    }

    public static @NotNull YamlConfiguration loadYamlFile(@NotNull String resource) throws Exception {
        createDirectory(getPluginDirectory());

        Path file = Path.of(getPluginDirectory().toString(), resource);
        if (!Files.exists(file))
            NaturalTracker.getInstance().saveResource(resource, false);

        YamlConfiguration config = YamlConfiguration.loadConfiguration(file.toFile());
        YamlConfiguration defaults = loadYamlResource(resource);
        config.setDefaults(defaults);

        boolean changed = false;
        for (String key : defaults.getKeys(true)){
            if (!defaults.isConfigurationSection(key) && !config.contains(key)){
                config.set(key, defaults.get(key));
                changed = true;
            }
        }

        if (changed)
            config.save(file.toFile());

        return config;
    }

    public static @NotNull JsonObject loadJsonFile(@NotNull Path file) throws Exception {
        if (!Files.exists(file))
            throw new Exception("Файл '%s' не существует!".formatted(file.getFileName()));

        try { return JsonParser.parseString(Files.readString(file, StandardCharsets.UTF_8)).getAsJsonObject(); }
        catch(Exception e){ throw new Exception("Не удалось загрузить JSON-файл '%s'!".formatted(file.getFileName())); }
    }

    public static void saveJsonFile(@NotNull Path file, @NotNull JsonObject json) throws Exception {
        if (file.getParent() != null)
            createDirectory(file.getParent());

        try { Files.writeString(file, GSON.toJson(json), StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING); }
        catch(Exception e){ throw new Exception("Не удалось сохранить JSON-файл '%s'!".formatted(file.getFileName())); }
    }
}
