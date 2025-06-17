package me.vovari2.auditron;

import org.bukkit.Location;

public interface AuditronAPI {
    static boolean wasGenerated(Location location){
        return Blocks.wasGenerated(location);
    }
}
