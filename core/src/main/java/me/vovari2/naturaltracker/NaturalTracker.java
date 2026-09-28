package me.vovari2.naturaltracker;

import me.vovari2.naturaltracker.changes.ChangesCache;
import me.vovari2.naturaltracker.listeners.BlockListener;
import me.vovari2.naturaltracker.listeners.InspectorListener;
import org.bukkit.NamespacedKey;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Locale;

public final class NaturalTracker extends JavaPlugin {
    private static NaturalTracker INSTANCE;

    private final NamespacedKey INSPECTOR_NAMESPACED_KEY = new NamespacedKey("natural_traker", "inspector");
    private String PLUGIN_NAME;
    private String VERSION;

    public void onEnable() {
        long time = System.currentTimeMillis();

        INSTANCE = this;
        Console.LOGGER = getComponentLogger();
        Executor.register(this);

        ChangesCache.enable();
        registerListeners();

        Console.info("<green>Плагин %s %s включён! (%d ms)".formatted(PLUGIN_NAME, VERSION, System.currentTimeMillis() - time));
    }

    public void onDisable() {
        ChangesCache.disable();
        unregisterListeners();

        Console.info("<red>Плагин %s %s выключен!".formatted(PLUGIN_NAME, VERSION));
    }

    public long onReload(){
        long time = System.currentTimeMillis();

        ChangesCache.reload();
        unregisterListeners();
        registerListeners();

        Console.info("<green>Плагин %s %s перезагружен! (%d ms)".formatted(PLUGIN_NAME, VERSION, System.currentTimeMillis() - time));
        return System.currentTimeMillis() - time;
    }

    private void registerListeners(){
        getServer().getPluginManager().registerEvents(new BlockListener(), this);
        getServer().getPluginManager().registerEvents(new InspectorListener(), this);
    }
    private void unregisterListeners(){
        HandlerList.unregisterAll(this);
    }

    public static NaturalTracker getInstance() {
        return INSTANCE;
    }
    public static NamespacedKey getInspectorNamespacedKey() {
        return INSTANCE.INSPECTOR_NAMESPACED_KEY;
    }
}
