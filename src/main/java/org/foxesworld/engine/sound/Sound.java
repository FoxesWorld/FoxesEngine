package org.foxesworld.engine.sound;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.foxesworld.engine.Engine;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public class Sound {
    private final SoundPlayer soundPlayer;
    private final Map<String, Map<String, String>> soundsMap = new HashMap<>();
    private final Random random = new Random();

    public Sound(Engine engine, InputStream inputStream) {
        this.soundPlayer = new SoundPlayer(engine);
        loadSounds(inputStream);
    }

    private void loadSounds(InputStream inputStream) {
        try (InputStreamReader reader = new InputStreamReader(inputStream)) {
            JsonParser parser = new JsonParser();
            JsonObject jsonObject = parser.parse(reader).getAsJsonObject();

            Set<Map.Entry<String, JsonElement>> entries = jsonObject.entrySet();
            for (Map.Entry<String, JsonElement> entry : entries) {
                String category = entry.getKey();
                JsonObject categoryObj = entry.getValue().getAsJsonObject();

                Map<String, String> categorySounds = new HashMap<>();
                Set<Map.Entry<String, JsonElement>> soundEntries = categoryObj.entrySet();

                for (Map.Entry<String, JsonElement> soundEntry : soundEntries) {
                    String soundName = soundEntry.getKey();
                    JsonElement soundElement = soundEntry.getValue();

                    if (soundElement.isJsonObject()) {
                        JsonObject soundObj = soundElement.getAsJsonObject();
                        JsonArray soundsArray = soundObj.getAsJsonArray("sounds");

                        if (soundsArray != null && soundsArray.size() > 0) {
                            String randomSound = chooseRandomSound(soundsArray);
                            categorySounds.put(soundName, randomSound);
                        }
                    }
                }

                if (!categorySounds.isEmpty()) {
                    soundsMap.put(category, categorySounds);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String chooseRandomSound(JsonArray soundArray) {
        int randomIndex = random.nextInt(soundArray.size());
        return soundArray.get(randomIndex).getAsString();
    }

    public Map<String, String> getSounds(String category) {
        return soundsMap.get(category);
    }

    public void playSound(String category, String soundName) {
        Map<String, String> categorySounds = soundsMap.get(category);
        if (categorySounds != null) {
            this.soundPlayer.playSound(categorySounds.get(soundName), false);
        }
    }

    public SoundPlayer getSoundPlayer() {
        return soundPlayer;
    }
}
