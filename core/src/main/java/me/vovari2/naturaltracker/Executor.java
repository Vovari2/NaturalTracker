package me.vovari2.naturaltracker;

import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandTree;
import dev.jorel.commandapi.arguments.LiteralArgument;
import me.vovari2.naturaltracker.commands.ReloadCommand;

public class Executor {
    public final static String PERMISSION = "auditron.*";

    static void preInitialize(NaturalTracker instance){
        CommandTree commandEditor = new CommandTree("auditron");
        commandEditor.setAliases(new String[]{"audit"});
        commandEditor.then(new LiteralArgument("reload")
                .withPermission(PERMISSION)
                .executes(ignored->{ NaturalTracker.getInstance().onReload(); }));
        commandEditor.register(instance);
    }

    static void initialize(NaturalTracker instance) {
        CommandAPI.unregister("auditron");

        CommandTree command = new CommandTree("auditron");
        command.setAliases(new String[]{"audit"});
        command.then(new LiteralArgument("reload")
                .withPermission(PERMISSION)
                .executes(ReloadCommand::executes));

        command.register(instance);
    }
}
