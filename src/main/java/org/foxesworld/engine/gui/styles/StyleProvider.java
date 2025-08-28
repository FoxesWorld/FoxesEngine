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
/**
 * Provider of style attributes for GUI components.
 *
 * <p>StyleProvider loads JSON style files from a resource directory (by default {@code assets/styles/})
 * and provides a map that associates a component name with a set of styles for that component.
 * Expected file format (example):
 * <pre>
 * {
 *   "styles": {
 *     "Button": {
 *       "default": { ... },
 *       "primary": { ... },
 *       "states": [ {...}, {...} ]
 *     }
 *   }
 * }
 * </pre>
 *
 * The structure is parsed into {@link StyleAttributes} — a POJO that contains style fields.
 *
 * <p>The class logs errors when loading individual files and continues loading other styles.</p>
 */
public class StyleProvider {

    /**
     * Default path to the styles directory inside application resources.
     */
    private static final String DEFAULT_STYLE_PATH = "assets/styles/";

    /**
     * Internal map: component name -> (style name -> style attributes).
     */
    private final Map<String, Map<String, StyleAttributes>> elementStyles = new HashMap<>();

    /**
     * Base path to styles (can be overridden via constructor).
     */
    private final String stylePath;

    /**
     * Gson instance used for deserializing JSON style files.
     */
    private final Gson gson;

    /**
     * Creates a style provider and loads the given style names from the default directory.
     *
     * @param styles array of style names (component names) to load (without the .json extension)
     */
    public StyleProvider(String[] styles) {
        this(styles, DEFAULT_STYLE_PATH);
    }

    /**
     * Creates a style provider and loads the given style names from the specified resource directory.
     *
     * @param styles    array of style names (component names) to load (without the .json extension)
     * @param stylePath resource path where JSON style files are located, e.g. {@code "assets/styles/"}.
     */
    public StyleProvider(String[] styles, String stylePath) {
        this.stylePath = stylePath;
        this.gson = new Gson();
        Engine.LOGGER.info("Initializing StyleProvider with path: " + stylePath);
        loadStyles(styles);
    }

    /**
     * Loads a set of styles by name. Any errors loading a single style are logged but do not
     * interrupt loading of the remaining styles.
     *
     * @param styles array of style names (component names)
     */
    private void loadStyles(String[] styles) {
        if (styles == null) return;
        for (String style : styles) {
            try {
                loadStyle(style);
            } catch (StyleLoadingException e) {
                Engine.getLOGGER().error("Failed to load style: " + style, e);
            }
        }
    }

    /**
     * Loads a specific JSON style file and parses it into a map of styles for the component.
     * If the style is already loaded, the method returns without action.
     *
     * @param component component name (for example, "Button").
     * @throws StyleLoadingException if the file is missing or has an invalid format.
     */
    private void loadStyle(String component) throws StyleLoadingException {
        if (component == null || component.isEmpty()) {
            throw new StyleLoadingException("Component name is null or empty");
        }

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

    /**
     * Loads JSON from resources at the specified path.
     *
     * @param path path inside the classpath, e.g. {@code "assets/styles/Button.json"}.
     * @return root {@link JsonObject} of the parsed JSON.
     * @throws StyleLoadingException when the file is missing or parsing fails.
     */
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

    /**
     * Parses the styles section for a specific component and returns a map of style name -> attributes.
     * Supports both objects and arrays (in the latter case array elements are converted
     * to keys named {@code styleName_index} (for example, {@code states_0}, {@code states_1}).
     *
     * @param componentStyles {@link JsonObject} describing component styles.
     * @return map of styleName -> {@link StyleAttributes}
     */
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

    /**
     * Parses an array of styles and adds them to the resulting map with indexed keys.
     *
     * @param styleName name of the style array (JSON key).
     * @param styleArray array of JSON objects with style attributes.
     * @param styleMap resulting map where parsed attributes are added.
     */
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

    /**
     * Returns the full map of loaded styles.
     * Top-level key is the component name, value is a map of styles for that component.
     *
     * @return a (possibly empty) {@link Map} of loaded styles.
     */
    public Map<String, Map<String, StyleAttributes>> getElementStyles() {
        return elementStyles;
    }
}
