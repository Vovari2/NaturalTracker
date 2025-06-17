package me.vovari2.auditron.tasks;

import me.vovari2.auditron.Auditron;
import me.vovari2.auditron.Blocks;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

public class AutoClearChanges extends BukkitRunnable {
    private static BukkitTask task;

    public static void start(){
        stop();
        task = new AutoClearChanges().runTaskTimerAsynchronously(Auditron.getInstance(), 20L, 20L);
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
