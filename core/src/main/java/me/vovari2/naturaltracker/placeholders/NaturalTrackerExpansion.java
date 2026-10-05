package me.vovari2.naturaltracker.placeholders;

import me.clip.placeholderapi.PlaceholderAPI;
import me.clip.placeholderapi.PlaceholderAPIPlugin;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.vovari2.naturaltracker.NaturalTracker;
import me.vovari2.naturaltracker.NaturalTrackerAPI;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

public class NaturalTrackerExpansion extends PlaceholderExpansion {
    private static final String HAS_PREFIX = "has_";

    public @NotNull String getIdentifier() { return NaturalTracker.getPluginName().toLowerCase(Locale.ROOT); }
    public @NotNull String getAuthor() { return NaturalTracker.getAuthors(); }
    public @NotNull String getVersion() { return NaturalTracker.getVersion(); }
    public boolean persist() { return true; }

    public @Nullable String onRequest(OfflinePlayer player, @NotNull String params){
        if (!params.startsWith(HAS_PREFIX))
            return null;

        // Кэш доступен только из главного потока, а PlaceholderAPI может вызывать плейсхолдеры асинхронно
        if (!Bukkit.isPrimaryThread())
            return null;

        Location location = parseLocation(PlaceholderAPI.setBracketPlaceholders(player, params.substring(HAS_PREFIX.length())));
        if (location == null)
            return null;

        return NaturalTrackerAPI.wasGenerated(location) ? PlaceholderAPIPlugin.booleanTrue() : PlaceholderAPIPlugin.booleanFalse();
    }

    // Разбор справа: имя мира может содержать "_"
    private static @Nullable Location parseLocation(@NotNull String text){
        int zIndex = text.lastIndexOf('_');
        if (zIndex <= 0) return null;
        int yIndex = text.lastIndexOf('_', zIndex - 1);
        if (yIndex <= 0) return null;
        int xIndex = text.lastIndexOf('_', yIndex - 1);
        if (xIndex <= 0) return null;

        World world = Bukkit.getWorld(text.substring(0, xIndex));
        if (world == null)
            return null;

        try {
            return new Location(world,
                    Math.floor(Double.parseDouble(text.substring(xIndex + 1, yIndex))),
                    Math.floor(Double.parseDouble(text.substring(yIndex + 1, zIndex))),
                    Math.floor(Double.parseDouble(text.substring(zIndex + 1))));
        } catch(NumberFormatException e){ return null; }
    }
}
