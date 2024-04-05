package org.foxesworld.engine.gui;

import org.foxesworld.engine.gui.components.slider.Slider;

import javax.swing.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
@SuppressWarnings("unused")
public class ComponentsAccessor {

    private final GuiBuilder guiBuilder;
    private final String panelId;
    private final Map<String, JComponent> componentMap = new HashMap<>();
    private final List<JComponent> componentList = new ArrayList<>();
    private final Map<String, String> formCredentials = new HashMap<>();
    @SuppressWarnings("unused")
    public ComponentsAccessor(GuiBuilder guiBuilder, String panelId) {
        this.guiBuilder = guiBuilder;
        this.panelId = panelId;
        collectDataAndSelectComponents();
    }

    private void collectDataAndSelectComponents() {
        List<JComponent> components = guiBuilder.getComponentsMap().get(panelId);
        if (components != null) {
            for (JComponent component : components) {
                String name = component.getName();
                if (name != null && !name.isEmpty()) {
                    componentMap.put(name, component);
                    componentList.add(component);

                    String value = "";
                    if (component instanceof JTextField) {
                        value = ((JTextField) component).getText();
                    } else if (component instanceof JCheckBox) {
                        value = String.valueOf(((JCheckBox) component).isSelected());
                    } else if(component instanceof Slider){
                        value = String.valueOf(((Slider) component).getValue());
                    }
                    formCredentials.put(name, value);
                }
            }
        }
    }
    @SuppressWarnings("unused")
    public Map<String, JComponent> getComponentMap() {
        return componentMap;
    }
    @SuppressWarnings("unused")
    public List<JComponent> getComponentList() {
        return componentList;
    }
    @SuppressWarnings("unused")
    public Map<String, String> getFormCredentials() {
        return formCredentials;
    }
    @SuppressWarnings("unused")
    public JComponent getComponent(String id) {
        return componentMap.get(id);
    }
}
