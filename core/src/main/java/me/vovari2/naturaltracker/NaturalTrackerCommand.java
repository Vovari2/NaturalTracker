package me.vovari2.naturaltracker;

import me.vovari2.naturaltracker.commands.Command;
import me.vovari2.naturaltracker.commands.InspectorCommand;
import me.vovari2.naturaltracker.commands.ReloadCommand;
import me.vovari2.naturaltracker.messages.Messages;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.stream.Stream;

public class NaturalTrackerCommand extends BukkitCommand {
    private final NaturalTracker instance;
    public NaturalTrackerCommand(NaturalTracker instance) {
        super("naturaltracker", "Команда для работы с плагином NaturalTracker", "/<command>", List.of("nt"));
        this.instance = instance;
    }

    public boolean execute(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) {
        if (!sender.hasPermission("naturaltracker.admin"))
            return Messages.DONT_HAVE_PERMISSION.send(sender);
        if (args.length < 1)
            return Messages.NOT_ENOUGH_ARGUMENTS.send(sender);

        @Nullable Command command = switch(args[0].toLowerCase()){
            case "reload" -> new ReloadCommand(instance, sender);
            case "inspect" -> new InspectorCommand(instance, sender);
            default -> null;
        };

        if (command == null)
            return Messages.INVALID_COMMAND_ARGUMENT.send(sender);

        return command.execute();
    }

    public @NotNull List<String> tabComplete(@NotNull CommandSender sender, @NotNull String alias, String[] args) throws IllegalArgumentException {
        if (!sender.hasPermission("naturaltracker.admin") || args.length != 1)
            return List.of();
        return Stream.of("reload", "inspect").filter(s -> s.startsWith(args[0].toLowerCase())).toList();
    }
}
