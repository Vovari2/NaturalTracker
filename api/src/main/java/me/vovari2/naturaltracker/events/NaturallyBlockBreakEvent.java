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
 * Игрок сломал блок. Вызывается до записи изменения в кэш, поэтому состояние блока — то, что было до поломки.
 * Событие информационное: отменить его нельзя.
 */
public class NaturallyBlockBreakEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();

    private final Player player;
    private final Location location;
    private final BlockState state;
    private final @Nullable ChangeAge changeAge;

    public NaturallyBlockBreakEvent(@NotNull Player player, @NotNull Location location, @NotNull BlockState state){
        this(player, location, state, null);
    }
    public NaturallyBlockBreakEvent(@NotNull Player player, @NotNull Location location, @NotNull BlockState state, @Nullable ChangeAge changeAge){
        this.player = player;
        this.location = location;
        this.state = state;
        this.changeAge = changeAge;
    }

    public Player getPlayer() { return player; }
    public Location getLocation() { return location; }

    /** Давность предыдущего изменения до поломки; null — сведений нет или использован прежний конструктор. */
    public @Nullable ChangeAge getChangeAge() { return changeAge; }

    /** Блок был создан миром и до поломки с ним ничего не делали. */
    public boolean wasGenerated() { return state == BlockState.GENERATED; }
    /** Блок до поломки был поставлен игроком или механизмом. */
    public boolean wasPlaced() { return state == BlockState.PLACED; }
    /** Блок до поломки уже считался уничтоженным (на его месте поставили блок без учёта плагином). */
    public boolean wasDestroyed() { return state == BlockState.DESTROYED; }

    @Override
    public @NotNull HandlerList getHandlers() { return HANDLERS; }
    public static HandlerList getHandlerList() { return HANDLERS; }
}
