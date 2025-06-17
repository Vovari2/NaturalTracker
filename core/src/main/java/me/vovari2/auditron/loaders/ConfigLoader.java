package me.vovari2.auditron.loaders;

import me.vovari2.auditron.utils.FileUtils;

public class ConfigLoader extends Loader{
    public static int ACTION_HOLD_TIME;

    public static void initialize() throws Exception {
        new ConfigLoader("config.yml");
    }
    private ConfigLoader(String resourcePath) throws Exception {
        FileUtils.createResourceFileInFolder(resourcePath);
        configuration = FileUtils.getContentFromYamlFile(FileUtils.getPluginFolder(), resourcePath);

        ACTION_HOLD_TIME = getInt("action-hold-time", 20);
    }
}
