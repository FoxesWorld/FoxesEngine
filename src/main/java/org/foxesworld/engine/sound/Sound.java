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
    private final Engine engine;
    private final Map<String, Map<String, List<String>>> soundsMap = new HashMap<>();
    private final Random random = new Random();

    @SuppressWarnings("unused")
    public Sound(Engine engine, InputStream inputStream) {
        this.engine = engine;
        Engine.getLOGGER().debug("FoxesSound init");
        this.soundPlayer = new SoundPlayer(engine);
        loadSounds(inputStream);
    }

    private void loadSounds(InputStream inputStream) {
        engine.getExecutorServiceProvider().submitTask(() -> {
            try (InputStreamReader reader = new InputStreamReader(inputStream)) {
                JsonObject jsonObject = JsonParser.parseReader(reader).getAsJsonObject();

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
                Engine.getLOGGER().info("Sounds loaded successfully");
            } catch (IOException e) {
                Engine.getLOGGER().error("Failed to load sounds", e);
            }
        }, "Load Sounds Task");
    }

    public List<String> getSounds(String category, String subCategory) {
        Map<String, List<String>> subCategorySoundsMap = soundsMap.get(category);
        if (subCategorySoundsMap != null) {
            return subCategorySoundsMap.get(subCategory);
        }
        return null;
    }

    public String playSound(String category, String subCategory) {
        String randomSound = "";
        List<String> subCategorySounds = getSounds(category, subCategory);
        if (subCategorySounds != null && !subCategorySounds.isEmpty()) {
            int randomIndex = random.nextInt(subCategorySounds.size());
            randomSound = subCategorySounds.get(randomIndex);
            this.soundPlayer.playSound(randomSound, false);
        }
        return randomSound;
    }

    public String playSound(String category, String subCategory, PlaybackStatusListener playbackStatusListener) {
        String randomSound = "";
        List<String> subCategorySounds = getSounds(category, subCategory);
        if (subCategorySounds != null && !subCategorySounds.isEmpty()) {
            int randomIndex = random.nextInt(subCategorySounds.size());
            randomSound = subCategorySounds.get(randomIndex);
            this.soundPlayer.playSound(randomSound, false, playbackStatusListener);
        }
        return randomSound;
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
