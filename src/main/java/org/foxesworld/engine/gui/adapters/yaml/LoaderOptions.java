package org.foxesworld.engine.gui.adapters.yaml;

import org.foxesworld.engine.gui.components.ComponentAttributes;

import java.util.Map;

public class LoaderOptions {
    private Map<String, ComponentAttributes> panels;

    public Map<String, ComponentAttributes> getPanels() {
        return panels;
    }

    public void setPanels(Map<String, ComponentAttributes> panels) {
        this.panels = panels;
    }
}
