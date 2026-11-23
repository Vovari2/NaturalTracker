package me.vovari2.naturaltracker.events;

import me.vovari2.naturaltracker.changes.BlockState;
import me.vovari2.naturaltracker.ChangeAge;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Игрок поставил блок. Вызывается до записи изменения в кэш, поэтому состояние блока — то, что было до установки.
 * Событие информационное: отменить его нельзя.
 */
public class NaturallyBlockPlaceEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();

    private final Player player;
    private final Location location;
    private final BlockState state;
    private final @Nullable ChangeAge changeAge;

    public NaturallyBlockPlaceEvent(@NotNull Player player, @NotNull Location location, @NotNull BlockState state){
        this(player, location, state, null);
    }
    public NaturallyBlockPlaceEvent(@NotNull Player player, @NotNull Location location, @NotNull BlockState state, @Nullable ChangeAge changeAge){
        this.player = player;
        this.location = location;
        this.state = state;
        this.changeAge = changeAge;
    }

    public Player getPlayer() { return player; }
    public Location getLocation() { return location; }

    /** Давность предыдущего изменения до установки; null — сведений нет или использован прежний конструктор. */
    public @Nullable ChangeAge getChangeAge() { return changeAge; }
    /** Индекс давности от 0 до 3; -1, если давность неизвестна. */
    public int getIndexChangeAge() { return changeAge == null ? -1 : changeAge.ordinal(); }

    /** Блок был создан миром и до установки с ним ничего не делали. */
    public boolean wasGenerated() { return state == BlockState.GENERATED; }
    /** Блок до установки был поставлен игроком или механизмом. */
    public boolean wasPlaced() { return state == BlockState.PLACED; }
    /** Блок до установки считался уничтоженным. */
    public boolean wasDestroyed() { return state == BlockState.DESTROYED; }

    @Override
    public @NotNull HandlerList getHandlers() { return HANDLERS; }
    public static HandlerList getHandlerList() { return HANDLERS; }
}
