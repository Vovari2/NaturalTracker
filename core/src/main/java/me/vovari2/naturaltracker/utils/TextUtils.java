package me.vovari2.naturaltracker.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.jetbrains.annotations.NotNull;

public class TextUtils {
    public static @NotNull Component toComponent(@NotNull final String string){
        return MiniMessage.miniMessage().deserialize(string);
    }
}
