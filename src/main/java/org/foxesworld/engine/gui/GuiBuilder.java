package org.foxesworld.engine.gui;

import com.google.gson.Gson;
import org.foxesworld.engine.Engine;
import org.foxesworld.engine.gui.components.ComponentAttributes;
import org.foxesworld.engine.gui.components.ComponentFactory;
import org.foxesworld.engine.gui.components.frame.FrameAttributes;
import org.foxesworld.engine.gui.components.frame.FrameConstructor;
import org.foxesworld.engine.gui.components.frame.OptionGroups;

import javax.swing.*;
import java.awt.*;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("unused")
public class GuiBuilder {
    private final FrameConstructor frameConstructor;
    private final ComponentFactory componentFactory;
    private final  Engine engine;
    private final Map<String, JPanel> panelsMap = new HashMap<>();
    private final Map<String, List<JComponent>> componentsMap = new HashMap<>();
    private final Map<String, List<String>> childsNparents = new HashMap<>();
    private final Map<String, JPanel> loadPanels = new HashMap<>();
    private GuiBuilderListener guiBuilderListener;
    private boolean additionalPanelsBuilt = false;

    public GuiBuilder(Engine engine) {
        this.engine = engine;
        this.frameConstructor = engine.getFrame();
        this.componentFactory = new ComponentFactory(engine);
        Engine.getLOGGER().debug("=== GUI BUILDER ===");
    }

    public void buildGui(String framePath, JPanel parent) {
        FrameAttributes frameAttributes = loadFrameAttributes(framePath);
        buildPanels(frameAttributes.getGroups(), parent);
    }

    private FrameAttributes loadFrameAttributes(String framePath) {
        Gson gson = new Gson();
        return gson.fromJson(new InputStreamReader(GuiBuilder.class.getClassLoader().getResourceAsStream(framePath)), FrameAttributes.class);
    }

    public List<Component> getAllChildComponents(String parentPanel) {
        List<Component> components = new ArrayList<>();
        childsNparents.getOrDefault(parentPanel, new ArrayList<>()).forEach(child -> components.addAll(componentsMap.getOrDefault(child, new ArrayList<>())));
        return components;
    }

    @Deprecated
    public JComponent getComponentById(String id) {
        return componentsMap.values().stream()
                .flatMap(List::stream)
                .filter(component -> component.getName() != null && component.getName().equals(id))
                .findFirst()
                .orElse(null);
    }

    @Deprecated
    public Map<String, JComponent> getPanelsComponents(String panelName) {
        Map<String, JComponent> panelComponents = new HashMap<>();
        componentsMap.getOrDefault(panelName, new ArrayList<>())
                .forEach(component -> panelComponents.put(component.getName(), component));
        return panelComponents;
    }

    private void buildPanels(Map<String, OptionGroups> panels, JPanel parentPanel) {
        if (panels != null) {
            panels.forEach((componentGroup, optionGroups) -> {
                JPanel thisPanel = createPanel(optionGroups, componentGroup);
                processChildComponents(optionGroups.getChildComponents(), thisPanel);
                addChildPanelIfNeeded(panels, parentPanel, thisPanel, componentGroup);
                buildPanels(optionGroups.getGroups(), thisPanel);
                updateChildsNparents(parentPanel, thisPanel);
            });
        }
    }

    private JPanel createPanel(OptionGroups optionGroups, String componentGroup) {
        JPanel thisPanel = frameConstructor.getPanel().createGroupPanel(optionGroups.getPanelOptions(), componentGroup, this.frameConstructor);
        thisPanel.setName(componentGroup);
        thisPanel.setVisible(optionGroups.getPanelOptions().isVisible());
        return thisPanel;
    }

    private void processChildComponents(List<ComponentAttributes> childComponents, JPanel parentPanel) {
        childComponents.forEach(componentAttributes -> {
            if (componentAttributes.getComponentType() != null) {
                addComponentToParent(componentAttributes, parentPanel);
            } else if (componentAttributes.getGroups() != null) {
                buildPanels(componentAttributes.getGroups(), parentPanel);
            } else if (componentAttributes.getReadFrom() != null) {
                processReadFromAttribute(componentAttributes, parentPanel);
            } else if (componentAttributes.getLoadPanel() != null && !componentAttributes.getLoadPanel().isEmpty()) {
                loadPanels.put(componentAttributes.getLoadPanel(), parentPanel);
            }
        });
    }

    private void addComponentToParent(ComponentAttributes componentAttributes, JPanel parentPanel) {
        JComponent component = componentFactory.createComponent(componentAttributes);
        parentPanel.add(component);
        componentsMap.computeIfAbsent(parentPanel.getName(), k -> new ArrayList<>()).add(component);
    }

    private void processReadFromAttribute(ComponentAttributes componentAttributes, JPanel parentPanel) {
        FrameAttributes frameAttributes = loadFrameAttributes(componentAttributes.getReadFrom());
        if (frameAttributes.getGroups() == null && frameAttributes.getChildComponents() != null && !frameAttributes.getChildComponents().isEmpty()) {
            processChildComponents(frameAttributes.getChildComponents(), parentPanel);
        } else {
            buildGui(componentAttributes.getReadFrom(), parentPanel);
        }
    }

    private void addChildPanelIfNeeded(Map<String, OptionGroups> panels, JPanel parentPanel, JPanel childPanel, String componentGroup) {
        if (!panelsMap.containsKey(componentGroup)) {
            addPanelGroup(parentPanel, childPanel);
            guiBuilderListener.onPanelBuild(panels, componentGroup, parentPanel);
        }
    }

    private void updateChildsNparents(JPanel parentPanel, JPanel childPanel) {
        childsNparents.computeIfAbsent(parentPanel.getName(), k -> new ArrayList<>()).add(childPanel.getName());
    }

    public void buildAdditionalPanels() {
        if (!additionalPanelsBuilt) {
            guiBuilderListener.onPanelsBuilt();
            Engine.LOGGER.debug(" == BUILDING ADDITIONAL PANELS ==");

            loadPanels.forEach((key, value) -> {
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
    }

    public Map<String, List<JComponent>> getComponentsMap() {
        return componentsMap;
    }

    @SuppressWarnings("unused")
    public void addPanelToMap(JPanel panel) {
        this.panelsMap.put(panel.getName(), panel);
    }

    public Map<String, JPanel> getPanelsMap() {
        return panelsMap;
    }

    @SuppressWarnings("unused")
    public Map<String, List<String>> getChildsNparents() {
        return childsNparents;
    }

    public void setGuiBuilderListener(GuiBuilderListener guiBuilderListener) {
        this.guiBuilderListener = guiBuilderListener;
    }
    public Engine getEngine() {
        return engine;
    }

    public ComponentFactory getComponentFactory() {
        return componentFactory;
    }
}
