package me.vovari2.naturaltracker;

import org.bukkit.Location;

public interface NaturalTrackerAPI {
    static boolean wasGenerated(Location location){
        return Blocks.wasGenerated(location);
    }
}
