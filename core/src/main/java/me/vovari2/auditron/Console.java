package me.vovari2.auditron;

import me.vovari2.auditron.utils.TextUtils;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;

public class Console {
    static ComponentLogger LOGGER;

    public static void info(String message, Object... objects){
        LOGGER.info(TextUtils.toComponent(message), objects);

    }
    public static void warn(String message, Object... objects){
        LOGGER.warn(TextUtils.toComponent(message), objects);
    }
    public static void error(String message, Object... objects){
        LOGGER.error(TextUtils.toComponent(message), objects);
    }
}
