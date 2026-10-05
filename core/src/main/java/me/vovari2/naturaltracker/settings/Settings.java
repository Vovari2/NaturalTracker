package me.vovari2.naturaltracker.settings;

import me.vovari2.naturaltracker.Console;
import me.vovari2.naturaltracker.DatabaseType;

public final class Settings {
    public static class CHANGES {
        public static int AUTOSAVE;
    }
    public static class DATABASE{
        public static DatabaseType TYPE;
        public static String URL;
        public static String USER;
        public static String PASSWORD;
        public static boolean LIMIT;
        public static int LIMIT_SIZE;
        public static class POOL {
            public static int MIN_SIZE;
            public static int MAX_SIZE;
            public static int TIMEOUT_TIME;
            public static int TIMEOUT_QUERY;
        }
    }

    public static void initialize(){
        try { new Loader(); }
        catch(Exception e){ Console.error("Не удалось загрузить настройки!", e); }
    }
}
