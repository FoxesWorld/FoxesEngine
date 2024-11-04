package org.foxesworld.engine.gui;

import java.util.HashMap;
import java.util.Map;

class FrameAttributesLoaderFactory {
    private static final Map<String, FrameAttributesLoader> loaders = new HashMap<>();

    static {
        loaders.put("json", new JsonFrameAttributesLoader());
        loaders.put("yaml", new YamlFrameAttributesLoader());
    }

    public static FrameAttributesLoader getLoader(String fileType) {
        FrameAttributesLoader loader = loaders.get(fileType);
        if (loader == null) {
            throw new IllegalArgumentException("Unsupported file type: " + fileType);
        }
        return loader;
    }
}