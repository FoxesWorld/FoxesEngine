package org.foxesworld.engine.gui.components;

import org.foxesworld.engine.gui.components.frame.OptionGroups;

import java.util.List;
import java.util.Map;

public abstract class Attributes {
    protected Map<String, OptionGroups> panels;
    protected List<ComponentAttributes> childComponents;

    public Map<String, OptionGroups> getGroups() {
        return panels;
    }
    public List<ComponentAttributes> getChildComponents() {
        return childComponents;
    }

}
