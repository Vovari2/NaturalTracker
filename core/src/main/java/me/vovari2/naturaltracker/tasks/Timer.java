package me.vovari2.naturaltracker.tasks;

import me.vovari2.naturaltracker.NaturalTracker;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

public record Timer(@NotNull BukkitRunnable runnable, @NotNull BukkitTask task) {
    public static @NotNull Timer period(int wait, int period, @NotNull Runnable periodOperation) {
        BukkitRunnable runnable = new BukkitRunnable() {
            public void run() {
                periodOperation.run();
            }
        };
        BukkitTask task = runnable.runTaskTimer(NaturalTracker.getInstance(), wait, period);
        return new Timer(runnable, task);
    }
}
