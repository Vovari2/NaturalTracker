package me.vovari2.naturaltracker;

import me.vovari2.naturaltracker.changes.ChunkCache;
import org.bukkit.Location;

public interface NaturalTrackerAPI {
    /** Только из главного потока. Пока чанк не подгрузился из БД, может ошибочно вернуть true. */
    static boolean wasGenerated(Location location){
        return !ChunkCache.wasChanged(location);
    }
}
