package me.vovari2.naturaltracker.commands;

import dev.jorel.commandapi.executors.CommandArguments;
import me.vovari2.naturaltracker.NaturalTracker;
import org.bukkit.command.CommandSender;

public class ReloadCommand {
    public static void executes(CommandSender ignored1, CommandArguments ignored2){
        NaturalTracker.getInstance().onReload();
    }
}
