package me.vovari2.naturaltracker;

import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import me.vovari2.naturaltracker.commands.InspectorCommand;
import me.vovari2.naturaltracker.commands.ReloadCommand;
import org.jetbrains.annotations.NotNull;

public class Executor {
    public final static String PERMISSION = "naturaltracker.*";

    static void register(@NotNull NaturalTracker instance){
        LiteralCommandNode<CommandSourceStack> basicCommand = Commands.literal("naturaltracker")
                .requires(ctx -> ctx.getSender().hasPermission(PERMISSION))
                .then(Commands.literal("reload").executes(ReloadCommand::executes))
                .then(Commands.literal("inspect").executes(InspectorCommand::executes)).build();

        instance.getLifecycleManager().registerEventHandler(
                LifecycleEvents.COMMANDS,
                commands -> commands.registrar().register(basicCommand));
    }
}
