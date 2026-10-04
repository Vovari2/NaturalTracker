package me.vovari2.naturaltracker.commands;

import me.vovari2.naturaltracker.NaturalTracker;
import me.vovari2.naturaltracker.messages.Messages;
import org.bukkit.command.CommandSender;

public class ReloadCommand extends Command{
    public ReloadCommand(NaturalTracker instance, CommandSender sender){
        super(instance, sender, new String[]{});
    }
    public boolean execute(){
        long time = instance.onReload();
        return Messages.RELOAD_SUCCESS
                .replace("time", String.valueOf(time))
                .send(sender);
    }
}
