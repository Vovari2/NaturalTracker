package me.vovari2.naturaltracker.commands;

import me.vovari2.naturaltracker.NaturalTracker;
import me.vovari2.naturaltracker.messages.Messages;
import me.vovari2.naturaltracker.utils.TextUtils;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;

public class InspectorCommand extends Command{
    public InspectorCommand(NaturalTracker instance, CommandSender sender){
        super(instance, sender, new String[]{});
    }
    public boolean execute(){
        if (!(sender instanceof Player player))
            return Messages.ONLY_FOR_PLAYERS.send(sender);

        if (!player.getInventory().addItem(getBlockInspector()).isEmpty())
            return Messages.INSPECT_INVENTORY_FULL.send(player);
        return Messages.INSPECT_SUCCESS.send(player);
    }

    public static ItemStack getBlockInspector(){
        ItemStack itemStack = new ItemStack(Material.SEA_LANTERN);
        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.displayName(TextUtils.toComponent("<!italic><gradient:#624E88:#CB80AB>Инспектор изменения блока"));
        itemMeta.lore(List.of(
                TextUtils.toComponent("<!italic><gray>Покажет информацию о натуральности позиции блока")));
        itemMeta.getPersistentDataContainer().set(NaturalTracker.getInspectorNamespacedKey(), PersistentDataType.BOOLEAN, true);
        itemMeta.addEnchant(Enchantment.DURABILITY, 1, true);
        itemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }
}
