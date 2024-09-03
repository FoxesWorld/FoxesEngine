package org.foxesworld.engine.gui.styles;

import com.google.gson.*;
import com.google.gson.annotations.SerializedName;
import org.foxesworld.engine.Engine;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class StyleProvider {

    private final Map<String, Map<String, StyleAttributes>> elementStyles = new HashMap<>();
    private final String[] styles = {"button", "label", "multiButton", "progressBar"};
    private final Engine engine;

    public StyleProvider(Engine engine) {
        engine.getLOGGER().info("Loading styles...");
        this.engine = engine;
        for(String style: this.styles){
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
                    Objects.requireNonNull(StyleProvider.class.getClassLoader().getResourceAsStream(stylePath)),
                    StandardCharsets.UTF_8
            );
            engine.getLOGGER().debug("Loading " + component + " style from " + stylePath);

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