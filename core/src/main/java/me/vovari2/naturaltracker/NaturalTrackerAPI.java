package me.vovari2.naturaltracker;

import me.vovari2.naturaltracker.changes.ChangesCache;
import org.bukkit.Location;

public interface NaturalTrackerAPI {
    static boolean wasGenerated(Location location){
        return !ChangesCache.has(location);
    }
}
