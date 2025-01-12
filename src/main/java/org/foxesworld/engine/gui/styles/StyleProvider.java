package org.foxesworld.engine.gui.styles;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import org.foxesworld.engine.Engine;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("unused")
public class StyleProvider {

    private static final String DEFAULT_STYLE_PATH = "assets/styles/";
    private final Map<String, Map<String, StyleAttributes>> elementStyles = new HashMap<>();
    private final String stylePath;
    private final Gson gson;

    public StyleProvider(String[] styles) {
        this(styles, DEFAULT_STYLE_PATH);
    }

    public StyleProvider(String[] styles, String stylePath) {
        this.stylePath = stylePath;
        this.gson = new Gson();
        Engine.LOGGER.info("Initializing StyleProvider with path: " + stylePath);
        loadStyles(styles);
    }

    private void loadStyles(String[] styles) {
        for (String style : styles) {
            try {
                loadStyle(style);
            } catch (StyleLoadingException e) {
                Engine.getLOGGER().error("Failed to load style: " + style, e);
            }
        }
    }

    private void loadStyle(String component) throws StyleLoadingException {
        if (elementStyles.containsKey(component)) {
            return;
        }

        String fullPath = stylePath + component + ".json";
        JsonObject jsonRoot = loadJson(fullPath);

        JsonObject stylesObject = jsonRoot.getAsJsonObject("styles");
        if (stylesObject == null) {
            throw new StyleLoadingException("Missing 'styles' section in " + fullPath);
        }

        JsonObject componentStyles = stylesObject.getAsJsonObject(component);
        if (componentStyles == null) {
            throw new StyleLoadingException("Missing component styles for " + component + " in " + fullPath);
        }

        Map<String, StyleAttributes> styleMap = parseComponentStyles(componentStyles);
        elementStyles.put(component, styleMap);
    }

    private JsonObject loadJson(String path) throws StyleLoadingException {
        try (InputStream stream = this.getClass().getClassLoader().getResourceAsStream(path)) {
            if (stream == null) {
                throw new StyleLoadingException("Style file not found: " + path);
            }
            InputStreamReader reader = new InputStreamReader(stream, StandardCharsets.UTF_8);
            return gson.fromJson(reader, JsonObject.class);
        } catch (JsonSyntaxException e) {
            throw new StyleLoadingException("Invalid JSON format in " + path, e);
        } catch (Exception e) {
            throw new StyleLoadingException("Error reading JSON file: " + path, e);
        }
    }

    private Map<String, StyleAttributes> parseComponentStyles(JsonObject componentStyles) {
        Map<String, StyleAttributes> styleMap = new HashMap<>();
        for (Map.Entry<String, JsonElement> entry : componentStyles.entrySet()) {
            String styleName = entry.getKey();
            JsonElement styleData = entry.getValue();

            if (styleData.isJsonObject()) {
                styleMap.put(styleName, gson.fromJson(styleData, StyleAttributes.class));
            } else if (styleData.isJsonArray()) {
                parseStyleArray(styleName, styleData.getAsJsonArray(), styleMap);
            } else {
                Engine.getLOGGER().warn("Unexpected JSON type for style: " + styleName);
            }
        }
        return styleMap;
    }

    private void parseStyleArray(String styleName, JsonArray styleArray, Map<String, StyleAttributes> styleMap) {
        int index = 0;
        for (JsonElement element : styleArray) {
            if (element.isJsonObject()) {
                styleMap.put(styleName + "_" + index, gson.fromJson(element, StyleAttributes.class));
                index++;
            } else {
                Engine.getLOGGER().warn("Non-object element in array for style: " + styleName);
            }
        }
    }

    public Map<String, Map<String, StyleAttributes>> getElementStyles() {
        return elementStyles;
    }
}
