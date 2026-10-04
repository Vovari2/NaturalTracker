package me.vovari2.naturaltracker;

import me.vovari2.naturaltracker.changes.ChangesCache;
import me.vovari2.naturaltracker.listeners.BlockListener;
import me.vovari2.naturaltracker.listeners.InspectorListener;
import me.vovari2.naturaltracker.messages.Messages;
import me.vovari2.naturaltracker.placeholders.NaturalTrackerExpansion;
import me.vovari2.naturaltracker.settings.Settings;
import org.bukkit.NamespacedKey;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

public final class NaturalTracker extends JavaPlugin {
    private static NaturalTracker INSTANCE;

    private final NamespacedKey INSPECTOR_NAMESPACED_KEY = new NamespacedKey("natural_tracker", "inspector");
    private String PLUGIN_NAME;
    private String VERSION;

    public void onEnable() {
        long time = System.currentTimeMillis();

        INSTANCE = this;
        PLUGIN_NAME = INSTANCE.getPluginMeta().getName();
        VERSION = INSTANCE.getPluginMeta().getVersion();
        Console.LOGGER = getComponentLogger();

        Messages.initialize();
        Settings.initialize();
        ChangesCache.enable();

        registerListeners();
        this.getServer().getCommandMap().register(PLUGIN_NAME.toLowerCase(), new NaturalTrackerCommand(INSTANCE));

        // PlaceholderAPI
        if (getServer().getPluginManager().isPluginEnabled("PlaceholderAPI"))
            new NaturalTrackerExpansion().register();

        Console.info("<green>Плагин %s %s включён! (%d ms)".formatted(PLUGIN_NAME, VERSION, System.currentTimeMillis() - time));
    }

    public void onDisable() {
        ChangesCache.disable();
        unregisterListeners();

        Console.info("<red>Плагин %s %s выключен!".formatted(PLUGIN_NAME, VERSION));
    }

    public long onReload(){
        long time = System.currentTimeMillis();


        Messages.initialize();
        Settings.initialize();
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
    public static String getPluginName(){
        return INSTANCE.PLUGIN_NAME;
    }
    public static String getAuthors(){
        return String.join(", ", INSTANCE.getPluginMeta().getAuthors());
    }
    public static String getVersion(){
        return INSTANCE.VERSION;
    }
    public static NamespacedKey getInspectorNamespacedKey() {
        return INSTANCE.INSPECTOR_NAMESPACED_KEY;
    }
}
