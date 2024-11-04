package org.foxesworld.engine.gui;

import org.foxesworld.engine.Engine;
import org.foxesworld.engine.gui.components.Attributes;
import org.foxesworld.engine.gui.components.frame.FrameAttributes;
import org.yaml.snakeyaml.Yaml;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

class YamlFrameAttributesLoader implements FrameAttributesLoader {
    private final Yaml yaml = new Yaml();

    @Override
    public Attributes load(String framePath) {
        Engine.LOGGER.warn("EXPERIMENTAL OPTION");
        try (InputStream inputStream = YamlFrameAttributesLoader.class.getClassLoader().getResourceAsStream(framePath)) {
            if (inputStream == null) {
                throw new FileNotFoundException("Resource not found: " + framePath);
            }
            return yaml.loadAs(inputStream, FrameAttributes.class);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load frame attributes from path: " + framePath, e);
        }
    }

}
