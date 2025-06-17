package me.vovari2.naturaltracker.loaders;

import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

public abstract class Loader {
    protected FileConfiguration configuration;
    protected Loader(){}

    protected @NotNull Object getObject(@NotNull String path) throws Exception{
        Object object = configuration.get(path);
        if (object == null)
            throw new Exception("Value '%s' must not be a null!".formatted(path));
        return object;
    }
    protected @Nullable Object getObject(@NotNull String path, @Nullable Object def){
        Object object = configuration.get(path);
        return object == null ? def : object;
    }

    protected boolean getBoolean(@NotNull String path) throws Exception{
        Object object = getObject(path);
        if (!(object instanceof Boolean value))
            throw new Exception("<red>Value '%s' must be true or false!".formatted(path));
        return value;
    }
    protected boolean getBoolean(@NotNull String path, boolean def) throws Exception{
        Object object = getObject(path);
        return object instanceof Boolean value ? value : def;
    }

    protected int getInt(@NotNull String path) throws Exception{
        Object object = getObject(path);
        if (!(object instanceof Integer value))
            throw new Exception("Value '%s' must be an integer!".formatted(path));
        return value;
    }
    protected int getInt(@NotNull String path, int min) throws Exception{
        int value = getInt(path);
        if (value < min)
            throw new Exception("Value '%s' must be greater than %s!".formatted(path, min));
        return value;
    }
}
