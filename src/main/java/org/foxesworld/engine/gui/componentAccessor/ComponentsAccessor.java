package org.foxesworld.engine.gui.componentAccessor;

import org.foxesworld.engine.gui.GuiBuilder;
import org.foxesworld.engine.gui.components.CompositeComponent;
import org.foxesworld.engine.gui.components.checkbox.Checkbox;
import org.foxesworld.engine.gui.components.compositeSlider.CompositeSlider;
import org.foxesworld.engine.gui.components.dropBox.DropBox;
import org.foxesworld.engine.gui.components.fileSelector.FileSelector;
import org.foxesworld.engine.gui.components.passfield.PassField;
import org.foxesworld.engine.gui.components.slider.Slider;
import org.foxesworld.engine.gui.components.textfield.TextField;

import javax.swing.*;
import java.lang.reflect.Field;
import java.util.*;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

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
        this.initComponents();
    }


    private final Map<Class<?>, Function<JComponent, String>> valueExtractors = Map.of(
            TextField.class, c -> ((TextField) c).getText(),
            PassField.class, c -> new String(((PassField) c).getPassword()),
            Checkbox.class, c -> String.valueOf(((Checkbox) c).isSelected()),
            Slider.class, c -> String.valueOf(((Slider) c).getValue()),
            DropBox.class, c -> String.valueOf(((DropBox) c).getSelectedIndex()),
            FileSelector.class, c -> ((FileSelector)c).getValue(),
            CompositeSlider.class, c -> String.valueOf(((CompositeSlider) c).getValue()),
            CompositeComponent.class, c -> ((CompositeComponent)c).getValue().toString()
    );

    protected void initComponents() {
        Class<?> clazz = this.getClass();
        for (Field field : clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(Component.class)) {
                Component annotation = field.getAnnotation(Component.class);
                String componentId = annotation.value().isEmpty()
                        ? field.getName()
                        : annotation.value();

                JComponent component = componentMap.get(componentId);
                if (component != null) {
                    try {
                        boolean wasAccessible = field.canAccess(this);
                        field.setAccessible(true);
                        field.set(this, component);
                        field.setAccessible(wasAccessible);
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException(
                                "Error injecting component '" + componentId + "' into field " + field.getName(), e
                        );
                    }
                } else {
                    throw new IllegalArgumentException(
                            "Component with ID '" + componentId + "' not found for field " + field.getName()
                    );
                }
            }
        }
    }

    public JPanel getPanel(){
        return this.guiBuilder.getPanelsMap().get(panelId);
    }

    private void collectComponents(String panelId) {
        Optional.ofNullable(guiBuilder.getComponentsMap().get(panelId))
                .ifPresent(components -> {
                    List<JComponent> panelComponents = components.stream()
                            .filter(this::isComponentType)
                            .peek(this::processComponent)
                            .collect(Collectors.toList());
                    panelComponentMap.put(panelId, panelComponents);
                });

        Optional.ofNullable(guiBuilder.getChildParentMap().get(panelId))
                .ifPresent(childPanels -> childPanels.forEach(this::collectComponents));
    }

    private void processComponent(JComponent component) {
        if (component == null) {
            return;
        }

        String name = component.getName();
        if (name != null && !name.isEmpty()) {
            componentMap.put(name, component);
            formCredentials.put(name, getValue(component));

            if (component instanceof org.foxesworld.engine.gui.components.panel.Panel panel) {
                Arrays.stream(panel.getComponents())
                        .filter(JComponent.class::isInstance)
                        .map(JComponent.class::cast)
                        .forEach(this::processComponent);
            }
        }
    }

    private boolean isComponentType(JComponent component) {
        return componentTypes.stream().anyMatch(type -> type.isInstance(component));
    }

    private String getValue(JComponent component) {
        return valueExtractors.getOrDefault(component.getClass(), c -> "").apply(component);
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
        Optional.ofNullable(panelComponentMap.get(panelId))
                .ifPresent(components -> components.forEach(component -> {
                    String name = component.getName();
                    if (name != null && !name.isEmpty()) {
                        credentials.put(name, getValue(component));
                    }
                }));

        Optional.ofNullable(guiBuilder.getChildParentMap().get(panelId))
                .ifPresent(childPanels -> childPanels.forEach(childPanelId -> collectFormCredentials(childPanelId, credentials)));
    }
}