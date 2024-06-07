package org.foxesworld.engine.gui.styles;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import org.foxesworld.engine.Engine;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class StyleProvider {
    private final Map<String, Map<String, StyleAttributes>> elementStyles = new HashMap<>();
    public StyleProvider(String[] styles) {
        Engine.LOGGER.info("Loading styles...");
        for(String style: styles){
            loadStyle(style);
        }
    }
    private void loadStyle(String component) {
        if (elementStyles.containsKey(component)) {
            return;
        }

        String stylePath = "assets/styles/" + component + ".json";
        try {
            Gson gson = new Gson();
            InputStreamReader reader = new InputStreamReader(
                    this.getClass().getClassLoader().getResourceAsStream(stylePath),
                    StandardCharsets.UTF_8
            );
            Engine.getLOGGER().debug("Loading " + component + " style from " + stylePath);

            JsonObject jsonRoot = gson.fromJson(reader, JsonObject.class);
            JsonObject stylesObject = jsonRoot.getAsJsonObject("styles");

            Map<String, StyleAttributes> styleMap = new HashMap<>();

            JsonObject componentStyles = stylesObject.getAsJsonObject(component);
            for (Map.Entry<String, JsonElement> entry : componentStyles.entrySet()) {
                String styleName = entry.getKey();
                JsonObject styleData = entry.getValue().getAsJsonObject();

                StyleAttributes styleAttributes = gson.fromJson(styleData, StyleAttributes.class);
                styleMap.put(styleName, styleAttributes);
            }
            elementStyles.put(component, styleMap);

        } catch (JsonSyntaxException e) {
            e.printStackTrace();
        }
    }
    public Map<String, Map<String, StyleAttributes>> getElementStyles() {
        return elementStyles;
    }
}
