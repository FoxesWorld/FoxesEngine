package org.foxesworld.engine.sound;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.foxesworld.engine.Engine;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

public class Sound {
    private final SoundPlayer soundPlayer;
    private final Map<String, Map<String, List<String>>> soundsMap = new HashMap<>();
    private Random random = new Random();

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
                Map<String, List<String>> subCategorySoundsMap = new HashMap<>();

                Set<Map.Entry<String, JsonElement>> subCategoryEntries = categoryObj.entrySet();
                for (Map.Entry<String, JsonElement> subCategoryEntry : subCategoryEntries) {
                    String subCategory = subCategoryEntry.getKey();
                    JsonArray soundsArray = subCategoryEntry.getValue().getAsJsonObject().getAsJsonArray("sounds");
                    List<String> subCategorySounds = new ArrayList<>();

                    for (JsonElement soundElement : soundsArray) {
                        subCategorySounds.add(soundElement.getAsString());
                    }

                    subCategorySoundsMap.put(subCategory, subCategorySounds);
                }

                soundsMap.put(category, subCategorySoundsMap);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<String> getSounds(String category, String subCategory) {
        Map<String, List<String>> subCategorySoundsMap = soundsMap.get(category);
        if (subCategorySoundsMap != null) {
            return subCategorySoundsMap.get(subCategory);
        }
        return null;
    }

    public void playSound(String category, String subCategory) {
        List<String> subCategorySounds = getSounds(category, subCategory);
        if (subCategorySounds != null && !subCategorySounds.isEmpty()) {
            int randomIndex = random.nextInt(subCategorySounds.size());
            String randomSound = subCategorySounds.get(randomIndex);
            this.soundPlayer.playSound(randomSound, false);
        }
    }

    public void playSound(String category, String subCategory, boolean loop) {
        List<String> subCategorySounds = getSounds(category, subCategory);
        if (subCategorySounds != null && !subCategorySounds.isEmpty()) {
            int randomIndex = random.nextInt(subCategorySounds.size());
            String randomSound = subCategorySounds.get(randomIndex);
            this.soundPlayer.playSound(randomSound, loop);
        }
    }
    public SoundPlayer getSoundPlayer() {
        return soundPlayer;
    }
}
