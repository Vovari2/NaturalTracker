package me.vovari2.naturaltracker.listeners;

import me.vovari2.naturaltracker.changes.ChunkCache;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockPlaceEvent;

public class BlockListener implements Listener {
    @EventHandler(priority=EventPriority.MONITOR)
    public void onPlayerPlaceBlock(BlockPlaceEvent event){
        if (event.isCancelled())
            return;
        ChunkCache.onBlockPlace(event.getBlock().getLocation());
    }
    @EventHandler(priority=EventPriority.MONITOR)
    public void onPlayerBreakBlock(BlockBreakEvent event){
        if (event.isCancelled())
            return;

        ChunkCache.onBlockDestroy(event.getBlock().getLocation());
    }
    @EventHandler(priority=EventPriority.MONITOR)
    public void onPistonExtend(BlockPistonExtendEvent event){
        if (event.isCancelled())
            return;

        BlockFace face = event.getDirection();
        for (int i = event.getBlocks().size() - 1; i >= 0; i--){
            Location location = event.getBlocks().get(i).getLocation();
            // Сначала источник, потом цель: клетка в середине цепочки — и то и другое, итоговым должно остаться «поставлен»
            ChunkCache.onBlockDestroy(location);
            ChunkCache.onBlockPlace(location.clone().add(face.getModX(), face.getModY(), face.getModZ()));
        }
    }
    @EventHandler(priority=EventPriority.MONITOR)
    public void onPistonRetract(BlockPistonRetractEvent event){
        if (event.isCancelled())
            return;

        BlockFace face = event.getDirection();
        for (int i = event.getBlocks().size() - 1; i >= 0; i--){
            Location location = event.getBlocks().get(i).getLocation();
            // Сначала источник, потом цель: клетка в середине цепочки — и то и другое, итоговым должно остаться «поставлен»
            ChunkCache.onBlockDestroy(location);
            ChunkCache.onBlockPlace(location.clone().add(face.getModX(), face.getModY(), face.getModZ()));
        }
    }
}
