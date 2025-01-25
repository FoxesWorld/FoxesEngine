package org.foxesworld.engine.gui;

import org.foxesworld.engine.Engine;
import org.foxesworld.engine.gui.adapters.FrameAttributesLoader;
import org.foxesworld.engine.gui.adapters.FrameLoaderAdapters;
import org.foxesworld.engine.gui.components.Attributes;
import org.foxesworld.engine.gui.components.ComponentAttributes;
import org.foxesworld.engine.gui.components.ComponentFactory;
import org.foxesworld.engine.gui.components.frame.FrameConstructor;
import org.foxesworld.engine.gui.components.frame.OptionGroups;
import org.foxesworld.notification.Notification;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@SuppressWarnings("unused")
public class GuiBuilder {
    private final FrameLoaderAdapters frameLoaderAdapters;
    private final FrameConstructor frameConstructor;
    private final ComponentFactory componentFactory;
    private final Notification notification;
    private final Engine engine;
    private final Map<String, JPanel> panelsMap = new ConcurrentHashMap<>();
    private final Map<String, List<JComponent>> componentsMap = new ConcurrentHashMap<>();
    private final Map<String, List<String>> childParentMap = new ConcurrentHashMap<>();
    private final Map<String, JPanel> loadPanels = new ConcurrentHashMap<>();
    private GuiBuilderListener guiBuilderListener;

    private boolean additionalPanelsBuilt = false;

    public GuiBuilder(Engine engine) {
        this.engine = engine;
        this.frameLoaderAdapters = new FrameLoaderAdapters(engine);
        this.frameConstructor = engine.getFrame();
        this.componentFactory = new ComponentFactory(engine);
        this.notification = new Notification();
        this.notification.setJFrame(this.frameConstructor);
        Engine.getLOGGER().debug("=== GUI BUILDER ===");
    }

    public void buildGui(String framePath, JPanel parent) {
        Attributes frameAttributes = loadFrameAttributes(framePath);
        buildPanels(frameAttributes.getGroups(), parent);
    }


    private Attributes loadFrameAttributes(String framePath) {
        String fileType = getFileType(framePath);
        FrameAttributesLoader loader = this.frameLoaderAdapters.getLoader(fileType);
        return loader.getAttributes(framePath);
    }

    private String getFileType(String framePath) {
        if (framePath.endsWith(".json")) {
            return "json";
        } else if(framePath.endsWith(".json5")) {
            return "json5";
        }
        else if (framePath.endsWith(".yaml") || framePath.endsWith(".yml")) {
            return "yaml";
        } else if (framePath.endsWith(".fxml")) {
            return "fxml";
        }
        return "json";
    }

    public List<Component> getAllChildComponents(String parentPanel) {
        List<Component> components = new ArrayList<>();
        childParentMap.getOrDefault(parentPanel, Collections.emptyList())
                .forEach(child -> components.addAll(componentsMap.getOrDefault(child, Collections.emptyList())));
        return components;
    }

    private void buildPanels(Map<String, OptionGroups> panels, JPanel parentPanel) {
        if (panels != null) {
            panels.forEach((componentGroup, optionGroups) -> {
                JPanel thisPanel = createPanel(optionGroups, componentGroup);
                processChildComponents(optionGroups.getChildComponents(), thisPanel);
                addChildPanelIfNeeded(panels, parentPanel, thisPanel, componentGroup);
                buildPanels(optionGroups.getGroups(), thisPanel);
                updateChildParentMap(parentPanel, thisPanel);
            });
        }
    }

    private JPanel createPanel(OptionGroups optionGroups, String componentGroup) {
        JPanel panel = frameConstructor.getPanel().createGroupPanel(optionGroups.getPanelOptions(), componentGroup, frameConstructor);
        panel.setName(componentGroup);
        panel.setVisible(optionGroups.getPanelOptions().isVisible());
        return panel;
    }

    private void processChildComponents(List<ComponentAttributes> childComponents, JPanel parentPanel) {
        for (ComponentAttributes componentAttributes : childComponents) {
            if (componentAttributes.getComponentType() != null) {
                addComponentToParent(componentAttributes, parentPanel);
            } else if (componentAttributes.getGroups() != null) {
                buildPanels(componentAttributes.getGroups(), parentPanel);
            } else if (componentAttributes.getReadFrom() != null) {
                processReadFromAttribute(componentAttributes, parentPanel);
            } else if (componentAttributes.getLoadPanel() != null && !componentAttributes.getLoadPanel().isEmpty()) {
                loadPanels.put(componentAttributes.getLoadPanel(), parentPanel);
            }
        }

    }

    private void addComponentToParent(ComponentAttributes componentAttributes, JPanel parentPanel) {
        JComponent component = componentFactory.createComponent(componentAttributes);
        if (component instanceof JPanel) {
            addPanelGroup(parentPanel, (JPanel) component);
            panelsMap.put(component.getName(), (JPanel) component);
        } else {
            parentPanel.add(component);
        }
        componentsMap.computeIfAbsent(parentPanel.getName(), k -> new ArrayList<>()).add(component);
    }

    private void processReadFromAttribute(ComponentAttributes componentAttributes, JPanel parentPanel) {
        Attributes frameAttributes = loadFrameAttributes(componentAttributes.getReadFrom());
        if (frameAttributes.getGroups() == null && frameAttributes.getChildComponents() != null) {
            processChildComponents(frameAttributes.getChildComponents(), parentPanel);
        } else {
            buildGui(componentAttributes.getReadFrom(), parentPanel);
        }
    }

    private void addChildPanelIfNeeded(Map<String, OptionGroups> panels, JPanel parentPanel, JPanel childPanel, String componentGroup) {
        if (!panelsMap.containsKey(componentGroup)) {
            addPanelGroup(parentPanel, childPanel);
            if (guiBuilderListener != null) {
                guiBuilderListener.onPanelBuild(panels, componentGroup, parentPanel);
            }
        }
    }

    private void updateChildParentMap(JPanel parentPanel, JPanel childPanel) {
        childParentMap.computeIfAbsent(parentPanel.getName(), k -> new ArrayList<>()).add(childPanel.getName());
    }

    public void buildAdditionalPanels() {
        if (!additionalPanelsBuilt) {
            if (guiBuilderListener != null) {
                guiBuilderListener.onPanelsBuilt();
            }
            Engine.LOGGER.debug(" == BUILDING ADDITIONAL PANELS ==");
            loadPanels.forEach((key, value) -> {
                guiBuilderListener.onAdditionalPanelBuild(value);
                Engine.LOGGER.debug("Processing {}", key);
                JPanel loadingPanel = panelsMap.get(key);
                if (loadingPanel != null) {
                    addPanelGroup(value, loadingPanel);
                }
            });
            additionalPanelsBuilt = true;
        } else {
            Engine.LOGGER.error("Additional panels are already built!");
        }
    }

    private void addPanelGroup(JPanel parent, JPanel child) {
        parent.add(child);
        panelsMap.put(child.getName(), child);
        updateChildParentMap(parent, child);
    }

    public Map<String, List<JComponent>> getComponentsMap() {
        return componentsMap;
    }

    public void addPanelToMap(JPanel panel) {
        this.panelsMap.put(panel.getName(), panel);
    }

    public Map<String, JPanel> getPanelsMap() {
        return panelsMap;
    }

    public Map<String, List<String>> getChildParentMap() {
        return childParentMap;
    }

    public void setGuiBuilderListener(GuiBuilderListener guiBuilderListener) {
        this.guiBuilderListener = guiBuilderListener;
    }

    public Engine getEngine() {
        return engine;
    }

    public FrameLoaderAdapters getFrameLoaderAdapters() {
        return frameLoaderAdapters;
    }

    public Notification getNotification() {
        return notification;
    }

    public ComponentFactory getComponentFactory() {
        return componentFactory;
    }
}