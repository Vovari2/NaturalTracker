package me.vovari2.auditron.commands;

import dev.jorel.commandapi.executors.CommandArguments;
import me.vovari2.auditron.Auditron;
import org.bukkit.command.CommandSender;

public class ReloadCommand {
    public static void executes(CommandSender ignored1, CommandArguments ignored2){
        Auditron.getInstance().onReload();
    }
}
