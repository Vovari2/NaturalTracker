package me.vovari2.naturaltracker.settings;

import me.vovari2.naturaltracker.Console;

public final class Settings {
    public static class CHANGES {
        public static int MAX_SIZE;
        public static int BUFFER;
    }

    public static void enable(){
        try { new Loader(); }
        catch(Exception e){ Console.error("Не удалось загрузить настройки: %s".formatted(e.getMessage())); }
    }
}
