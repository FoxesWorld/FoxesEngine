package org.foxesworld.engine.gui;

import com.google.gson.Gson;
import org.foxesworld.engine.gui.components.Attributes;
import org.foxesworld.engine.gui.components.ComponentAttributes;
import org.foxesworld.engine.gui.components.frame.FrameAttributes;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

class JsonFrameAttributesLoader implements FrameAttributesLoader {
    private final Gson gson = new Gson();

    @Override
    public Attributes load(String framePath) {
        try (InputStream inputStream = JsonFrameAttributesLoader.class.getClassLoader().getResourceAsStream(framePath)) {
            if (inputStream == null) {
                throw new FileNotFoundException("Resource not found: " + framePath);
            }
            try (InputStreamReader reader = new InputStreamReader(inputStream)) {
                return gson.fromJson(reader, ComponentAttributes.class);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to load frame attributes from path: " + framePath, e);
        }
    }

}