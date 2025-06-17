package me.vovari2.auditron.utils;

import me.vovari2.auditron.Auditron;
import me.vovari2.auditron.Console;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileUtils {

    public static File getPluginFolder(){
        return Auditron.getInstance().getDataFolder();
    }
    private static File createPluginFolder(){
        File pluginFolder = getPluginFolder();
        try{boolean ignored = pluginFolder.mkdir();}
        catch(Exception e){ Console.error("Error when creating a folder \"{}\"!", pluginFolder.getName()); }
        return pluginFolder;
    }

    public static void createFolder(File folder){
        boolean ignored = folder.mkdir();
    }
    public static @NotNull File createFolder(File parentFolder, String folderName){
        File folder = new File(parentFolder, folderName);
        boolean ignored = folder.mkdir();
        return folder;
    }

    public static void createFileInFolder(File folder, File file){
        try { Path ignored = Files.createFile(file.toPath());}
        catch(IOException e){Console.error("Error when creating a file '{}' in folder '{}'!", file.getName(), folder.getName());}
    }
    public static void createResourceFileInFolder(String resourcePath){
        File pluginFolder = createPluginFolder();
        File file = new File(pluginFolder, resourcePath);

        if (!file.exists())
            Auditron.getInstance().saveResource(resourcePath, false);
    }

    public static String getContentFromFile(File file){
        try{
            return Files.readString(file.toPath());
        } catch(IOException e){
            Console.error("Error when loading data from a file \"{}\"!", file.getName());}
        return null;
    }
    public static void setContentInFile(File file, String content){
        try (FileWriter writer = new FileWriter(file, false)) {
            writer.append(content);
        } catch(IOException e){ Console.error("Error when saving content in a file \"{}\"!", file.getName());}
    }
    public static YamlConfiguration getContentFromYamlFile(File folder, String fileName){
        return YamlConfiguration.loadConfiguration(new File(folder, fileName));
    }
    public static void setContentInYamlFile(File folder, String fileName, FileConfiguration configuration){
        try{configuration.save(new File(folder, fileName));}
        catch(Exception e){ Console.error("Error when saving content in a yaml file \"{}\"!", fileName);}
    }
}
