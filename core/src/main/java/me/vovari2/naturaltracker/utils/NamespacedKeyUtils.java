package me.vovari2.naturaltracker.utils;

import org.bukkit.NamespacedKey;

public class NamespacedKeyUtils {
    private static final String pluginNamespacedKey = "natural_tracker";
    public static NamespacedKey getCheckerBlock(){
        return NamespacedKey.fromString(pluginNamespacedKey + ".inspector");
    }
}
