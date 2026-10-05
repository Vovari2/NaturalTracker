package me.vovari2.naturaltracker.listeners;

import me.vovari2.naturaltracker.NaturalTracker;
import me.vovari2.naturaltracker.changes.ChunkCache;
import me.vovari2.naturaltracker.utils.TextUtils;
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
            event.getPlayer().sendMessage(TextUtils.toComponent("<yellow>Данные чанка ещё загружаются, попробуйте снова!"));
            event.setCancelled(true);
            return;
        }

        boolean wasGenerated = !ChunkCache.wasChanged(block.getLocation());
        event.getPlayer().sendMessage(TextUtils.toComponent(wasGenerated ? "<green>Блок был сгенерирован!" : "<red>Блок не был сгенерирован!"));
        event.setCancelled(true);
    }
}
