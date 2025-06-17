package me.vovari2.auditron;

import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPIBukkitConfig;
import me.vovari2.auditron.listeners.BlockListener;
import me.vovari2.auditron.loaders.ConfigLoader;
import me.vovari2.auditron.tasks.AutoClearChanges;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

public final class Auditron extends JavaPlugin {
    private static Auditron INSTANCE;

    public void onLoad(){
        INSTANCE = this;
        Console.LOGGER = getComponentLogger();

        CommandAPI.onLoad(new CommandAPIBukkitConfig(this).verboseOutput(false));
    }

    public void onEnable() {
        boolean isEnabled = true;
        long enableTime = System.currentTimeMillis();

        CommandAPI.onEnable();
        Executor.preInitialize(this);

        try {
            ConfigLoader.initialize();
            Executor.initialize(this);

            AutoClearChanges.start();
        } catch(Exception e){Console.error(e.getMessage());isEnabled = false;}

        enableListeners();

        if (isEnabled) Console.info("<green>Plugin {} {} enabled! ({} ms)", INSTANCE.getName(), INSTANCE.getPluginMeta().getVersion(), System.currentTimeMillis() - enableTime);
        else Console.warn("Plugin {} {} is not enabled! There was an error in the console above!", INSTANCE.getName(), INSTANCE.getPluginMeta().getVersion());
    }

    public void onDisable() {
        CommandAPI.onDisable();
        AutoClearChanges.stop();
        disableListeners();

        Console.info("<red>Plugin {} {} disabled!", INSTANCE.getName(), INSTANCE.getPluginMeta().getVersion());
    }

    public void onReload(){
        try {
            ConfigLoader.initialize();

            AutoClearChanges.stop();
            AutoClearChanges.start();

        } catch(Exception e){Console.error(e.getMessage());}

        disableListeners();
        enableListeners();

        Console.info("<dark_green>Plugin {} {} reloaded!", INSTANCE.getName(), INSTANCE.getPluginMeta().getVersion());
    }

    private void enableListeners(){
        getServer().getPluginManager().registerEvents(new BlockListener(), this);
    }
    private void disableListeners(){
        HandlerList.unregisterAll(this);
    }

    public static Auditron getInstance() {
        return INSTANCE;
    }
}
