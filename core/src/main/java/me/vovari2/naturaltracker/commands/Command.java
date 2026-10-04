package me.vovari2.naturaltracker.commands;

import me.vovari2.naturaltracker.NaturalTracker;
import org.bukkit.command.CommandSender;

public abstract class Command {
    protected final NaturalTracker instance;
    protected final CommandSender sender;
    protected final String[] args;
    protected Command(NaturalTracker instance, CommandSender sender, String[] args){
        this.instance = instance;
        this.sender = sender;
        this.args = args;
    }
    public abstract boolean execute();
}
