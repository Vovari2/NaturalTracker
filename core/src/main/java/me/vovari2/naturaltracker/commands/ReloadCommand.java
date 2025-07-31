package me.vovari2.naturaltracker.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import me.vovari2.naturaltracker.NaturalTracker;
import me.vovari2.naturaltracker.utils.TextUtils;

public class ReloadCommand {
    public static int executes(CommandContext<CommandSourceStack> ctx){
        NaturalTracker.getInstance().onReload();
        ctx.getSource().getSender().sendMessage(TextUtils.toComponent("<gradient:#54B435:#82CD47>Плагин был перезагружен!"));
        return Command.SINGLE_SUCCESS;
    }
}
