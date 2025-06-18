package me.vovari2.naturaltracker.commands;

import dev.jorel.commandapi.executors.CommandArguments;
import me.vovari2.naturaltracker.utils.NamespacedKeyUtils;
import me.vovari2.naturaltracker.utils.TextUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;

public class InspectorCommand {
    public static void executes(Player player, CommandArguments ignored2){
        player.getInventory().addItem(getBlockInspector());
        player.sendMessage(TextUtils.toComponent("<green>Выдан инспектор для блоков плагина!"));
    }

    public static ItemStack getBlockInspector(){
        ItemStack itemStack = new ItemStack(Material.SEA_LANTERN);
        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.displayName(TextUtils.toComponent("<!italic><gradient:#624E88:#CB80AB>Инспектор изменения блока"));
        itemMeta.lore(List.of(
                TextUtils.toComponent("<!italic><gray>При установке блока, показывает информацию о позиции, в которую его установили"),
                TextUtils.toComponent("<!italic><gray>При ломании блока, показывает информацию о позиции, которую сломали")));
        itemMeta.getPersistentDataContainer().set(NamespacedKeyUtils.getCheckerBlock(), PersistentDataType.BOOLEAN, true);
        itemMeta.setEnchantmentGlintOverride(true);
        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }
}
