package me.vovari2.naturaltracker.changes;

/**
 * Копия состояний и временных отметок чанка для записи в БД.
 * merge = true, если при выгрузке данные из БД так и не успели подгрузиться: перед записью их нужно подмешать.
 */
public record ChunkSnapshot(ChunkKey key, byte[] raw, boolean merge) {
    public static ChunkSnapshot of(ChunkKey key, ChunkEntry entry){
        byte[] raw = entry.toBytes();
        entry.dirty = false;
        return new ChunkSnapshot(key, raw, !entry.loaded);
    }

}
