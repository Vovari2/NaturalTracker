package me.vovari2.naturaltracker.changes;

import org.bukkit.Location;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.UUID;

public final class Position {
    private final byte[] world;
    private final int x;
    private final int y;
    private final int z;

    private Position(byte[] world, int x, int y, int z) {
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public static Position of(Location loc) {
        return of(loc.getWorld().getUID(),
                loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
    }
    public static Position of(UUID world, int x, int y, int z) {
        return new Position(toBytes(world), x, y, z);
    }

    public byte[] world() { return world; }
    public int x() { return x; }
    public int y() { return y; }
    public int z() { return z; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Position other)) return false;
        return x == other.x
                && y == other.y
                && z == other.z
                && Arrays.equals(world, other.world);
    }
    @Override
    public int hashCode() {
        return 31 * (31 * (31 * Arrays.hashCode(world) + x) + y) + z;
    }
    @Override
    public String toString() {
        return "%s:%d:%d:%d".formatted(uuidFromBytes(world), x, y, z);
    }

    private static byte[] toBytes(UUID uuid) {
        ByteBuffer bb = ByteBuffer.allocate(16);
        bb.putLong(uuid.getMostSignificantBits());
        bb.putLong(uuid.getLeastSignificantBits());
        return bb.array();
    }
    private static UUID uuidFromBytes(byte[] bytes) {
        ByteBuffer bb = ByteBuffer.wrap(bytes);
        return new UUID(bb.getLong(), bb.getLong());
    }
}