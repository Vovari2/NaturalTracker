package me.vovari2.naturaltracker.changes;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/** Последнее известное действие с блоком. */
public enum BlockState {
    GENERATED, // блок создан миром
    PLACED, // последним действием блок поставили
    DESTROYED; // последним действием блок уничтожили

    public static @Nullable BlockState of(@NotNull String name) {
        try { return valueOf(name.toUpperCase()); }
        catch (IllegalArgumentException e) { return null; }
    }
}
