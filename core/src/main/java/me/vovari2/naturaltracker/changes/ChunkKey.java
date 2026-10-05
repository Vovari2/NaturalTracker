package me.vovari2.naturaltracker.changes;

import me.vovari2.naturaltracker.utils.UUIDUtils;
import org.bukkit.Chunk;
import org.bukkit.Location;

import java.util.Arrays;
import java.util.UUID;

public final class ChunkKey {
    private final byte[] world;
    private final int x;
    private final int z;

    private ChunkKey(byte[] world, int x, int z) {
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
        return new ChunkKey(UUIDUtils.toBytes(world), x, z);
    }

    public byte[] world() { return world; }
    public int x() { return x; }
    public int z() { return z; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ChunkKey other)) return false;
        return x == other.x
                && z == other.z
                && Arrays.equals(world, other.world);
    }
    @Override
    public int hashCode() {
        return 31 * (31 * Arrays.hashCode(world) + x) + z;
    }
    @Override
    public String toString() {
        return "%s:%d:%d".formatted(UUIDUtils.fromBytes(world), x, z);
    }
}
