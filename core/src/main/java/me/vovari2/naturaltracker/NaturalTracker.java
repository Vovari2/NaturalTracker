package me.vovari2.naturaltracker;

import me.vovari2.naturaltracker.listeners.BlockListener;
import me.vovari2.naturaltracker.listeners.InspectorListener;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

public final class NaturalTracker extends JavaPlugin {
    private static NaturalTracker INSTANCE;
    public static final int AUTO_HOLD_TIME = 20;

    public void onLoad(){
        INSTANCE = this;
        Console.LOGGER = getComponentLogger();
    }

    public void onEnable() {
        long enableTime = System.currentTimeMillis();

        Executor.register(this);
        Blocks.enable();
        registerListeners();

        Console.info("<green>Plugin {} {} enabled! ({} ms)", INSTANCE.getName(), INSTANCE.getPluginMeta().getVersion(), System.currentTimeMillis() - enableTime);
    }

    public void onDisable() {
        Blocks.disable();
        unregisterListeners();

        Console.info("<red>Plugin {} {} disabled!", INSTANCE.getName(), INSTANCE.getPluginMeta().getVersion());
    }

    public void onReload(){
        unregisterListeners();
        registerListeners();

        Console.info("<dark_green>Plugin {} {} reloaded!", INSTANCE.getName(), INSTANCE.getPluginMeta().getVersion());
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
}
