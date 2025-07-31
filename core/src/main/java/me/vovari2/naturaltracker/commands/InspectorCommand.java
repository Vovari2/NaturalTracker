package me.vovari2.naturaltracker.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import me.vovari2.naturaltracker.utils.NamespacedKeyUtils;
import me.vovari2.naturaltracker.utils.TextUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;

public class InspectorCommand {
    public static int executes(CommandContext<CommandSourceStack> ctx){
        if (!(ctx.getSource().getSender() instanceof Player player))
            return Command.SINGLE_SUCCESS;

        player.getInventory().addItem(getBlockInspector());
        player.sendMessage(TextUtils.toComponent("<gradient:#54B435:#82CD47>Выдан инспектор для блоков плагина!"));
        return Command.SINGLE_SUCCESS;
    }

    public static ItemStack getBlockInspector(){
        ItemStack itemStack = new ItemStack(Material.SEA_LANTERN);
        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.displayName(TextUtils.toComponent("<!italic><gradient:#624E88:#CB80AB>Инспектор изменения блока"));
        itemMeta.lore(List.of(
                TextUtils.toComponent("<!italic><gray>При установке блока, показывает информацию о позиции, в которую его установили"),
                TextUtils.toComponent("<!italic><gray>При ломании блока, показывает информацию о позиции, которую сломали")));
        itemMeta.getPersistentDataContainer().set(NamespacedKeyUtils.getInspectorBlock(), PersistentDataType.BOOLEAN, true);
        itemMeta.setEnchantmentGlintOverride(true);
        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }
}
