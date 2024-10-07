package org.foxesworld.engine.gui.componentAccessor;

import org.foxesworld.engine.gui.GuiBuilder;
import org.foxesworld.engine.gui.components.dropBox.DropBox;
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
    private final Map<String, List<JComponent>> panelComponentMap = new HashMap<>();
    private final Map<String, Object> formCredentials = new HashMap<>();

    public ComponentsAccessor(GuiBuilder guiBuilder, String panelId, List<Class<?>> componentTypes) {
        this.guiBuilder = Objects.requireNonNull(guiBuilder, "guiBuilder must not be null");
        this.panelId = Objects.requireNonNull(panelId, "panelId must not be null");
        this.componentTypes = Objects.requireNonNull(componentTypes, "componentTypes must not be null");
        collectComponents(panelId);
    }

    private void collectComponents(String panelId) {
        List<JComponent> components = guiBuilder.getComponentsMap().get(panelId);
        if (components != null) {
            List<JComponent> panelComponents = new ArrayList<>();
            for (JComponent component : components) {
                processComponent(component, panelComponents);
            }
            panelComponentMap.put(panelId, panelComponents);
        }

        List<String> childPanels = guiBuilder.getChildParentMap().get(panelId);
        if (childPanels != null) {
            for (String childPanelId : childPanels) {
                collectComponents(childPanelId);
            }
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
        } else if(component instanceof DropBox) {
            return String.valueOf(((DropBox) component).getSelectedIndex());
        }
        return "";
    }

    public Map<String, JComponent> getComponentMap() {
        return Collections.unmodifiableMap(componentMap);
    }

    public Map<String, List<JComponent>> getPanelComponentMap() {
        return Collections.unmodifiableMap(panelComponentMap);
    }

    public Map<String, Object> getFormCredentials() {
        return formCredentials;
    }

    public JComponent getComponent(String id) {
        return componentMap.get(id);
    }

    public List<JComponent> getComponentsForPanel(String panelId) {
        return panelComponentMap.getOrDefault(panelId, Collections.emptyList());
    }

    public Map<String, Object> collectFormCredentialsForPanel(String panelId) {
        Map<String, Object> credentials = new HashMap<>();
        collectFormCredentials(panelId, credentials);
        return credentials;
    }

    private void collectFormCredentials(String panelId, Map<String, Object> credentials) {
        List<JComponent> components = panelComponentMap.get(panelId);
        if (components != null) {
            for (JComponent component : components) {
                String name = component.getName();
                if (name != null && !name.isEmpty()) {
                    credentials.put(name, getValue(component));
                }
            }
        }

        List<String> childPanels = guiBuilder.getChildParentMap().get(panelId);
        if (childPanels != null) {
            for (String childPanelId : childPanels) {
                collectFormCredentials(childPanelId, credentials);
            }
        }
    }
}