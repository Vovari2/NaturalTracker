package me.vovari2.naturaltracker.changes;

/**
 * Копия битов чанка для записи в БД.
 * merge = true, если при выгрузке данные из БД так и не успели подгрузиться: перед записью их нужно подмешать.
 */
public record ChunkSnapshot(ChunkKey key, byte[] raw, boolean merge) {
    public static ChunkSnapshot of(ChunkKey key, ChunkEntry entry){
        entry.dirty = false;
        return new ChunkSnapshot(key, entry.toBytes(), !entry.loaded);
    }

}
