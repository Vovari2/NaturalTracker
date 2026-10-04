package me.vovari2.naturaltracker.messages;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Set;

public class Message {
    protected final @NotNull String message;
    protected final boolean silent;

    protected Message(@Nullable String message) {
        this(message, false);
    }
    protected Message(@Nullable String message, boolean silent){
        this.message = message == null ? "" : message;
        this.silent = silent;
    }

    public @NotNull String string(){
        return message;
    }
    public @NotNull Component component(){
        return MiniMessage.miniMessage().deserialize(message);
    }

    public @NotNull Message silent(boolean silent){
        return new Message(message, silent);
    }
    public @NotNull Message newline(){
        return new Message(message + "<newline>", silent);
    }
    public @NotNull Message append(@NotNull Message message){
        return new Message(this.message + message.message, this.silent);
    }

    public @NotNull Message replace(@NotNull String placeholder, int replacement){
        return replace(placeholder, String.valueOf(replacement));
    }
    public @NotNull Message replace(@NotNull String placeholder, double replacement){
        return replace(placeholder, String.format("%.2f", replacement));
    }
    public @NotNull Message replace(@NotNull String placeholder, @NotNull String replacement){
        return new Message(message.replace("{" + placeholder + "}", replacement), silent);
    }
    public @NotNull Message replaceByKey(@NotNull Set<String> keys, @NotNull Map<String, String> placeholders, @NotNull String def){
        String newMessage = message;
        for(String key : keys){
            @NotNull String value = placeholders.getOrDefault(key, def);
            newMessage = newMessage.replace("{" + key + "}", value);
        }

        return new Message(newMessage, silent);
    }

    public boolean send(@NotNull Audience audience){
        if (message.isEmpty() || silent)
            return true;
        audience.sendMessage(component());
        return true;
    }
}
