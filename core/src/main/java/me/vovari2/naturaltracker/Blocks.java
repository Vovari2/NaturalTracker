package me.vovari2.naturaltracker;

import me.vovari2.naturaltracker.tasks.Timer;
import net.coreprotect.CoreProtect;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedList;
import java.util.List;

public class Blocks {
    private static Timer autoCleanerChanges;
    private static List<BlockChange> blocks;

    public static void enable(){
        blocks = new LinkedList<>();
        autoCleanerChanges = Timer.period(0, 200, Blocks::clearOldChanges);

    }
    public static void disable(){
        if(autoCleanerChanges != null && !autoCleanerChanges.task().isCancelled())
            autoCleanerChanges.task().cancel();
    }

    public static boolean wasGenerated(@NotNull final Location location){
        BlockChange change = get(location);
        if (change != null)
            return change.operation;
        return wasDestroyedInCoreProtect(location);
    }
    private static boolean wasDestroyedInCoreProtect(@NotNull final Location location){
        List<String[]> list = CoreProtect.getInstance().getAPI().blockLookup(location.getBlock(), (int) System.currentTimeMillis() / 1000);
        if (list.isEmpty())
            return true;
        return list.getFirst()[7].equals("0");
    }

    public static void logPlace(@NotNull final Location location){
        blocks.removeIf(block -> block.equals(location));
        blocks.add(new BlockChange(location.getBlockX(), location.getBlockY(), location.getBlockZ(), (int) System.currentTimeMillis() / 1000 + NaturalTracker.AUTO_HOLD_TIME, false));
    }
    public static void logDestroy(@NotNull final  Location location){
        blocks.removeIf(block -> block.equals(location));
        blocks.add(new BlockChange(location.getBlockX(), location.getBlockY(), location.getBlockZ(), (int) System.currentTimeMillis() / 1000 + NaturalTracker.AUTO_HOLD_TIME, true));
    }
    private static void clearOldChanges(){
        int currentTime = (int)System.currentTimeMillis()/1000;
        blocks.removeIf(block -> currentTime > block.time);
    }

    private static @Nullable Blocks.BlockChange get(@NotNull final Location location){
        for (BlockChange block : blocks)
            if (block.equals(location))
                return block;
        return null;
    }
    private record BlockChange(int x, int y, int z, int time, boolean operation){
        private boolean equals(@NotNull final Location location){
            return x == location.getBlockX() && y == location.getBlockY() && z == location.getBlockZ();
        }
    }
}
