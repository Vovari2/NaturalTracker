package me.vovari2.naturaltracker;

import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandTree;
import dev.jorel.commandapi.arguments.LiteralArgument;
import me.vovari2.naturaltracker.commands.InspectorCommand;
import me.vovari2.naturaltracker.commands.ReloadCommand;

public class Executor {
    public final static String PERMISSION = "naturaltracker.*";

    static void preInitialize(NaturalTracker instance){
        CommandTree commandEditor = new CommandTree("naturaltracker");
        commandEditor.setAliases(new String[]{"nt"});
        commandEditor.then(new LiteralArgument("reload")
                .withPermission(PERMISSION)
                .executes(ignored->{ NaturalTracker.getInstance().onReload(); }));
        commandEditor.register(instance);
    }

    static void initialize(NaturalTracker instance) {
        CommandAPI.unregister("naturaltracker");

        CommandTree command = new CommandTree("naturaltracker");
        command.setAliases(new String[]{"nt"});
        command.then(new LiteralArgument("reload")
                .withPermission(PERMISSION)
                .executes(ReloadCommand::executes));
        command.then(new LiteralArgument("inspect")
                .withPermission(PERMISSION)
                .executesPlayer(InspectorCommand::executes));

        command.register(instance);
    }
}
