package me.vovari2.naturaltracker.listeners;

import me.vovari2.naturaltracker.changes.ChunkCache;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;

public class ChunkListener implements Listener {
    @EventHandler(priority=EventPriority.MONITOR)
    public void onChunkLoad(ChunkLoadEvent event){
        ChunkCache.onChunkLoad(event.getChunk(), event.isNewChunk());
    }
    @EventHandler(priority=EventPriority.MONITOR)
    public void onChunkUnload(ChunkUnloadEvent event){
        ChunkCache.onChunkUnload(event.getChunk());
    }
}
