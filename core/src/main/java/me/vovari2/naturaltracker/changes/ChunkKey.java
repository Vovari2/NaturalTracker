package me.vovari2.naturaltracker.changes;

import org.bukkit.Chunk;
import org.bukkit.Location;

import java.util.UUID;

public final class ChunkKey {
    private final UUID world;
    private final int x;
    private final int z;

    private ChunkKey(UUID world, int x, int z) {
        this.world = world;
        this.x = x;
        this.z = z;
    }

    public static ChunkKey of(Location loc) {
        return of(loc.getWorld().getUID(), loc.getBlockX() >> 4, loc.getBlockZ() >> 4);
    }
    public static ChunkKey of(Chunk chunk) {
        return of(chunk.getWorld().getUID(), chunk.getX(), chunk.getZ());
    }
    public static ChunkKey of(UUID world, int x, int z) {
        return new ChunkKey(world, x, z);
    }

    public UUID world() { return world; }
    public int x() { return x; }
    public int z() { return z; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ChunkKey other)) return false;
        return x == other.x
                && z == other.z
                && world.equals(other.world);
    }
    @Override
    public int hashCode() {
        return 31 * (31 * world.hashCode() + x) + z;
    }
    @Override
    public String toString() {
        return "%s:%d:%d".formatted(world, x, z);
    }
}
