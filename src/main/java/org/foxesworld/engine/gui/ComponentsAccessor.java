package org.foxesworld.engine.gui;

import org.foxesworld.engine.Engine;

import javax.swing.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ComponentsAccessor {

    private final GuiBuilder guiBuilder;
    private final String panelId;
    private final Map<String, JComponent> componentMap = new HashMap<>();
    private final List<JComponent> componentList = new ArrayList<>();

    public ComponentsAccessor(GuiBuilder guiBuilder, String panelId){
        this.guiBuilder = guiBuilder;
        this.panelId = panelId;
        this.selectComponents();
    }

    private void selectComponents(){
        for (JComponent component : this.guiBuilder.getComponentsMap().get(panelId)){
            this.componentMap.put(component.getName(), component);
            this.componentList.add(component);
            Engine.LOGGER.debug("Selecting " + component.getName());
        }
    }

    protected Map<String, JComponent> getComponentMap() {
        return componentMap;
    }

    public List<JComponent> getComponentList() {
        return componentList;
    }

    protected JComponent getComponent(String id) {
        return componentMap.get(id);
    }
}
