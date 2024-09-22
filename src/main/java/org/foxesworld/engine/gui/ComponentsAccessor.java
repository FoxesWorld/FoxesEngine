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
    private final Map<String, List<JComponent>> componentListMap = new HashMap<>();
    private final Map<String, String> formCredentials = new HashMap<>();

    public ComponentsAccessor(GuiBuilder guiBuilder, String panelId, List<Class<?>> componentTypes) {
        this.guiBuilder = Objects.requireNonNull(guiBuilder, "guiBuilder must not be null");
        this.panelId = Objects.requireNonNull(panelId, "panelId must not be null");
        this.componentTypes = Objects.requireNonNull(componentTypes, "componentTypes must not be null");
        collectDataAndSelectComponents();
    }

    private void collectDataAndSelectComponents() {
        collectComponentsFromPanel(panelId);
        checkChildPanels(guiBuilder.getChildParentMap().get(panelId));
    }

    private void checkChildPanels(List<String> childPanels) {
        if (childPanels != null) {
            for (String childPanelId : childPanels) {
                collectComponentsFromPanel(childPanelId);
            }
        }
    }

    private void collectComponentsFromPanel(String panelId) {
        List<JComponent> components = guiBuilder.getComponentsMap().get(panelId);
        if (components != null) {
            List<JComponent> panelComponents = new ArrayList<>();
            for (JComponent component : components) {
                processComponent(component, panelComponents);
            }
            componentListMap.put(panelId, panelComponents);
        }
    }

    private void processComponent(JComponent component, List<JComponent> panelComponents) {
        if (component == null) {
            return;
        }

        String name = component.getName();
        if (name != null && !name.isEmpty() && isComponentType(component)) {
            componentMap.put(name, component);
            panelComponents.add(component);
            formCredentials.put(name, getValue(component));
        }

        if (component instanceof org.foxesworld.engine.gui.components.panel.Panel panel) {
            for (Component child : panel.getComponents()) {
                if (child instanceof JComponent) {
                    processComponent((JComponent) child, panelComponents);
                }
            }
        }
    }

    private boolean isComponentType(JComponent component) {
        return componentTypes.stream().anyMatch(type -> type.isInstance(component));
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
        return Collections.unmodifiableMap(componentMap);
    }

    public Map<String, List<JComponent>> getComponentListMap() {
        return Collections.unmodifiableMap(componentListMap);
    }

    public Map<String, String> getFormCredentials() {
        return Collections.unmodifiableMap(formCredentials);
    }

    public JComponent getComponent(String id) {
        return componentMap.get(id);
    }

    public List<JComponent> getComponentsForPanel(String panelId) {
        return componentListMap.getOrDefault(panelId, Collections.emptyList());
    }
}