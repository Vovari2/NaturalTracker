package me.vovari2.auditron;

import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandTree;
import dev.jorel.commandapi.arguments.LiteralArgument;
import me.vovari2.auditron.commands.ReloadCommand;

public class Executor {
    public final static String PERMISSION = "auditron.*";

    static void preInitialize(Auditron instance){
        CommandTree commandEditor = new CommandTree("auditron");
        commandEditor.setAliases(new String[]{"audit"});
        commandEditor.then(new LiteralArgument("reload")
                .withPermission(PERMISSION)
                .executes(ignored->{ Auditron.getInstance().onReload(); }));
        commandEditor.register(instance);
    }

    static void initialize(Auditron instance) {
        CommandAPI.unregister("auditron");

        CommandTree command = new CommandTree("auditron");
        command.setAliases(new String[]{"audit"});
        command.then(new LiteralArgument("reload")
                .withPermission(PERMISSION)
                .executes(ReloadCommand::executes));

        command.register(instance);
    }
}
