package me.vovari2.naturaltracker.messages;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import me.vovari2.naturaltracker.utils.FileUtils;

import java.nio.file.Files;
import java.nio.file.Path;

final class Loader {
    private static final String RESOURCE_NAME = "messages.json";

    Loader() throws Exception {
        Path file = Path.of(FileUtils.getPluginDirectory().toString(), RESOURCE_NAME);
        JsonObject messages;
        if (!Files.exists(file)){
            messages = new JsonObject();
            for (Messages message : Messages.values())
                messages.addProperty(message.name().toLowerCase(), message.string());

            FileUtils.saveJsonFile(file, messages);
        }
        else {
            messages = FileUtils.loadJsonFile(file);
            boolean changed = false;
            for (Messages message : Messages.values()){
                String key = message.name().toLowerCase();
                JsonElement elem = messages.get(key);
                if (elem == null){
                    messages.addProperty(key, message.string());
                    changed = true;
                    continue;
                }
                if (elem.isJsonPrimitive()){
                    message.set(new Message(elem.getAsString()));
                    continue;
                }
                if (elem.isJsonArray()){
                    message.set(new Message(String.join("<newline>", elem.getAsJsonArray().asList().stream().map(JsonElement::getAsString).toList())));
                    continue;
                }
            }
            if (changed)
                FileUtils.saveJsonFile(file, messages);
        }
    }
}
