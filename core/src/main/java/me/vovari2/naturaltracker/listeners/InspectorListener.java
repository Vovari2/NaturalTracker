package me.vovari2.naturaltracker.listeners;

import me.vovari2.naturaltracker.NaturalTracker;
import me.vovari2.naturaltracker.changes.ChunkCache;
import me.vovari2.naturaltracker.messages.Messages;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class InspectorListener implements Listener {
    @EventHandler
    public void onCheckerBlocks(PlayerInteractEvent event){
        Block block = event.getClickedBlock();
        if (block == null)
            return;

        ItemStack itemStack = event.getItem();
        if (itemStack == null || itemStack.getType() != Material.SEA_LANTERN)
            return;

        if (!itemStack.getItemMeta().getPersistentDataContainer().has(NaturalTracker.getInspectorNamespacedKey()))
            return;

        if (event.getAction() == Action.RIGHT_CLICK_BLOCK){
            BlockFace face = event.getBlockFace();
            block = block.getWorld().getBlockAt(block.getLocation().clone().add(face.getModX(), face.getModY(), face.getModZ()));
        }

        if (!ChunkCache.changeIsAccurate(block.getLocation())){
            Messages.INSPECT_CHUNK_LOADING.send(event.getPlayer());
            event.setCancelled(true);
            return;
        }

        Messages message = switch (ChunkCache.stateOf(block.getLocation())) {
            case GENERATED -> Messages.INSPECT_GENERATED;
            case PLACED -> Messages.INSPECT_PLACED;
            case DESTROYED -> Messages.INSPECT_DESTROYED;
        };
        message.send(event.getPlayer());
        event.setCancelled(true);
    }
}
