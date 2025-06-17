package me.vovari2.naturaltracker.tasks;

import me.vovari2.naturaltracker.NaturalTracker;
import me.vovari2.naturaltracker.Blocks;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

public class AutoClearChanges extends BukkitRunnable {
    private static BukkitTask task;

    public static void start(){
        stop();
        task = new AutoClearChanges().runTaskTimerAsynchronously(NaturalTracker.getInstance(), 20L, 20L);
    }
    public static void stop(){
        if (task != null && !task.isCancelled())
            task.cancel();
    }

    @Override
    public void run() {
        Blocks.clearOldChanges();
    }
}
