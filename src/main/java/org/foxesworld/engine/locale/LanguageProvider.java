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
    private final Set<String> sectionsSet = new HashSet<>();
    private final Set<String> localesSet = new HashSet<>();
    private int localeIndex = 0;
    private String currentLang;

    public LanguageProvider(Engine engine, String langFilePath, String locale) {
        this.currentLang = locale;

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
                    this.analyzeSection(localizedData.getValue());
                    if (localizedValues.has(currentLang)) {
                        String localizedValue = localizedValues.get(currentLang).getAsString();
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

    private void analyzeSection(JsonElement jsonElement){
        for (Map.Entry<String, JsonElement> langSet: jsonElement.getAsJsonObject().entrySet()){
            this.localesSet.add(langSet.getKey());
        }
    }

    public void setCurrentLang(String lang) {
        this.currentLang = lang;
    }

    public String getString(String key) {
        if (key != null) {
            if (key.contains(".")) {
                String[] parts = key.split("\\.");
                if (parts.length == 2) {
                    String category = parts[0];
                    String localizedKey = parts[1];
                    if (localizationData.containsKey(category)) {
                        Map<String, String> categoryMap = localizationData.get(category);
                        if (categoryMap.containsKey(localizedKey)) {
                            return categoryMap.get(localizedKey);
                        }
                    }
                }
            }
        }
        return key;
    }
    public String[] getSectionsSet() {
        return sectionsSet.toArray(new String[0]);
    }

    public void setLocaleIndex(String locale) {
        for(int k = 0; this.localesSet.size() > k; k++){
            if(this.localesSet.toArray()[k].equals(locale)){
                this.localeIndex =  k;
            }
        }
    }
    public String[] getLocalesSet() {
        return localesSet.toArray(new String[0]);
    }

    public int getLocaleIndex() {
        return localeIndex;
    }
}
