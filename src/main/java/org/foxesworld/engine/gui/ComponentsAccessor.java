package org.foxesworld.engine.gui;

import org.foxesworld.engine.gui.components.slider.Slider;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

@SuppressWarnings("unused")
public class ComponentsAccessor {

    private final GuiBuilder guiBuilder;
    private final String panelId;
    private final List<Class<?>> componentTypes;
    private final Map<String, JComponent> componentMap = new HashMap<>();
    private final List<JComponent> componentList = new ArrayList<>();
    private final Map<String, String> formCredentials = new HashMap<>();

    public ComponentsAccessor(GuiBuilder guiBuilder, String panelId, List<Class<?>> componentTypes) {
        this.guiBuilder = guiBuilder;
        this.panelId = panelId;
        this.componentTypes = componentTypes;
        collectDataAndSelectComponents();
    }

    private void collectDataAndSelectComponents() {
        List<JComponent> components = guiBuilder.getComponentsMap().get(panelId);
        if (components != null) {
            for (JComponent component : components) {
                processComponent(component);
            }
        }
    }

    private void processComponent(JComponent component) {
        if (component == null) {
            return;
        }

        String name = component.getName();
        if (name != null && !name.isEmpty()) {
            if (isComponentType(component)) {
                componentMap.put(name, component);
                componentList.add(component);
                formCredentials.put(name, getValue(component));
            }
        }

        if (component instanceof org.foxesworld.engine.gui.components.panel.Panel panel) {
            for (Component child : panel.getComponents()) {
                if (child instanceof JComponent) {
                    processComponent((JComponent) child);
                }
            }
        }
    }

    private boolean isComponentType(JComponent component) {
        for (Class<?> compType : this.componentTypes) {
            if (compType.isInstance(component)) {
                return true;
            }
        }
        return false;
    }

    private String getValue(JComponent component) {
        if (component instanceof JTextField) {
            return ((JTextField) component).getText();
        } else if (component instanceof JCheckBox) {
            return String.valueOf(((JCheckBox) component).isSelected());
        } else if (component instanceof Slider) {
            return String.valueOf(((Slider) component).getValue());
        }
        return "";
    }

    public Map<String, JComponent> getComponentMap() {
        return componentMap;
    }

    public List<JComponent> getComponentList() {
        return componentList;
    }

    public Map<String, String> getFormCredentials() {
        return formCredentials;
    }

    public JComponent getComponent(String id) {
        return componentMap.get(id);
    }
}