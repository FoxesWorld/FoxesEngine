package org.foxesworld.engine.locale;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.foxesworld.engine.Engine;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@SuppressWarnings("unused")
public class LanguageProvider {
    private final Map<String, Map<String, String>> localizationData = new HashMap<>();
    private final Engine engine;
    private String langFilePath;
    private final Set<String> sectionsSet = new HashSet<>();
    private final Set<String> localesSet = new HashSet<>();
    private int localeIndex = 0;

    public LanguageProvider(Engine engine, String langFilePath, int localeIndex) {
        this.localeIndex = localeIndex;
        this.engine = engine;
        this.langFilePath = langFilePath;
        loadLocalizationData(engine, langFilePath);
    }

    private void loadLocalizationData(Engine engine, String langFilePath) {
        try {
            Gson gson = new Gson();
            InputStreamReader reader = new InputStreamReader(engine.getClass().getClassLoader().getResourceAsStream(langFilePath), StandardCharsets.UTF_8);
            BufferedReader bufferedReader = new BufferedReader(reader);
            StringBuilder jsonStringBuilder = new StringBuilder();
            String line;

            while ((line = bufferedReader.readLine()) != null) {
                jsonStringBuilder.append(line);
            }

            JsonElement jsonElement = gson.fromJson(jsonStringBuilder.toString(), JsonElement.class);
            JsonObject jsonObject = jsonElement.getAsJsonObject();

            for (Map.Entry<String, JsonElement> categoryEntry : jsonObject.entrySet()) {
                String langKey = categoryEntry.getKey();
                sectionsSet.add(langKey);

                JsonObject categoryData = categoryEntry.getValue().getAsJsonObject();
                Map<String, String> categoryMap = new HashMap<>();

                for (Map.Entry<String, JsonElement> localizedData : categoryData.entrySet()) {
                    String localizedKey = localizedData.getKey();
                    JsonObject localizedValues = localizedData.getValue().getAsJsonObject();
                    analyzeSection(localizedValues);
                    String localeKey = getLocaleKeyByIndex(localeIndex);
                    if (localizedValues.has(localeKey)) {
                        String localizedValue = localizedValues.get(localeKey).getAsString();
                        categoryMap.put(localizedKey, localizedValue);
                    }
                }

                localizationData.put(langKey, categoryMap);
            }
            bufferedReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void analyzeSection(JsonObject localizedValues) {
        for (Map.Entry<String, JsonElement> langSet : localizedValues.entrySet()) {
            localesSet.add(langSet.getKey());
        }
    }

    private String getLocaleKeyByIndex(int index) {
        return localesSet.toArray(new String[0])[index];
    }

    public void setLocaleIndex(int index) {
        if (index >= 0 && index < localesSet.size()) {
            this.localeIndex = index;
            reloadLocalizationData();
        } else {
            throw new IndexOutOfBoundsException("Invalid locale index: " + index);
        }
    }

    private void reloadLocalizationData() {
        localizationData.clear();
        loadLocalizationData(this.engine, this.langFilePath);
    }

    public String getString(String key) {
        if (key != null && key.contains(".")) {
            String[] parts = key.split("\\.");
            if (parts.length == 2) {
                String category = parts[0];
                String localizedKey = parts[1];
                Map<String, String> categoryMap = localizationData.get(category);
                if (categoryMap != null) {
                    return categoryMap.getOrDefault(localizedKey, key);
                }
            }
        }
        return key;
    }

    public String getStringWithKey(String langKey, String[] replaceKeys, String[] replaceValues) {
        String langLine = getString(langKey);
        if (replaceKeys != null && replaceValues != null && replaceKeys.length == replaceValues.length) {
            for (int i = 0; i < replaceKeys.length; i++) {
                langLine = langLine.replace("{" + replaceKeys[i] + "}", replaceValues[i]);
            }
        }
        return langLine;
    }

    public String[] getSectionsSet() {
        return sectionsSet.toArray(new String[0]);
    }

    public String[] getLocalesSet() {
        return localesSet.toArray(new String[0]);
    }

    public int getLocaleIndex() {
        return localeIndex;
    }
}