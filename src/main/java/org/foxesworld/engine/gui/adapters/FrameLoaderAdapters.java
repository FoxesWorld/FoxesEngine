package org.foxesworld.engine.gui.adapters;

import org.foxesworld.engine.Engine;
import org.foxesworld.engine.gui.adapters.json.JsonFrameAttributesLoader;
import org.foxesworld.engine.gui.adapters.json5.Json5FrameAttributesLoader;
import org.foxesworld.engine.gui.adapters.xml.XmlFrameAttributesLoader;
import org.foxesworld.engine.gui.adapters.yaml.YamlFrameAttributesLoader;

import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("unused")
public class FrameLoaderAdapters {

    private final Map<String, FrameAttributesLoader> adapters = new HashMap<>();
    private final Map<String, FrameAttributesLoader> defaultAdapters = new HashMap<>();
    private final Engine engine;

    public FrameLoaderAdapters(Engine engine) {
        this.engine = engine;
        registerDefaultAdapters();
        registerEngineConfiguredAdapters();
    }

    private void registerDefaultAdapters() {
        defaultAdapters.put("json", new JsonFrameAttributesLoader());
        defaultAdapters.put("json5", new Json5FrameAttributesLoader());
        //defaultAdapters.put("yaml", new YamlFrameAttributesLoader());
        //defaultAdapters.put("xml", new XmlFrameAttributesLoader());
    }

    private void registerEngineConfiguredAdapters() {
        for (String type : engine.getEngineData().getLoadAdapters()) {
            FrameAttributesLoader adapter = defaultAdapters.get(type);
            if (adapter != null) {
                adapters.put(type, adapter);
                Engine.LOGGER.info("Registering {} adapter...", type);
            } else {
                Engine.LOGGER.warn("No default adapter found for type: {}", type);
            }
        }
        Engine.LOGGER.info("Registered adapters: {}", adapters.keySet());
    }

    public FrameAttributesLoader getLoader(String fileType) {
        FrameAttributesLoader loader = adapters.get(fileType);
        if (loader == null) {
            throw new IllegalArgumentException("No adapter found for: " + fileType);
        }
        return loader;
    }

    public void registerAdapter(String type, FrameAttributesLoader adapter) {
        if (adapters.containsKey(type)) {
            Engine.LOGGER.warn("Adapter for type {} is already registered and will be overwritten.", type);
        }
        adapters.put(type, adapter);
        Engine.LOGGER.info("Registered {} adapter", type);
    }

    public void unregisterAdapter(String type) {
        if (adapters.remove(type) != null) {
            Engine.LOGGER.info("Unregistered {} adapter", type);
        } else {
            Engine.LOGGER.warn("No adapter found to unregister for type: {}", type);
        }
    }

    public boolean isAdapterRegistered(String type) {
        return adapters.containsKey(type);
    }

    public Map<String, FrameAttributesLoader> getRegisteredAdapters() {
        return new HashMap<>(adapters);
    }

    public Map<String, FrameAttributesLoader> getDefaultAdapters() {
        return new HashMap<>(defaultAdapters);
    }
}
