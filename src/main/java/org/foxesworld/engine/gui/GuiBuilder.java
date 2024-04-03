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

public class GuiBuilder {
    private final Engine engine;
    private final HashMap<String, List<JComponent>> componentsMap = new HashMap<>();
    private final HashMap<String, JPanel> panelsMap = new HashMap<>();
    private final HashMap<String, List<String>> childsNparents = new HashMap<>();
    private final HashMap<String, JPanel> loadPanels = new HashMap<>();
    private final FrameConstructor frameConstructor;
    private final ComponentFactory componentFactory;
    private GuiBuilderListener guiBuilderListener;
    private boolean additionalPanelsBuilt = false;

    public GuiBuilder(Engine engine) {
        this.engine = engine;
        Engine.getLOGGER().debug("=== GUI BUILDER ===");
        frameConstructor = engine.getFrame();
        componentFactory = new ComponentFactory(engine);
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
        for (String thisChild : childsNparents.get(parentPanel)) {
            components.addAll(componentsMap.get(thisChild));
        }
        return components;
    }

    public JComponent getComponentById(String id) {
        for (Map.Entry<String, List<JComponent>> panelsMapEntry : componentsMap.entrySet()) {
            for (JComponent component : panelsMapEntry.getValue()) {
                if (component.getName() != null && component.getName().equals(id)) {
                    return component;
                }
            }
        }
        return null;
    }

    public void setLabelText(String componentId, String text) {
        JComponent component = getComponentById(componentId);
        if (component instanceof JLabel) {
            ((JLabel) component).setText(text);
        }
    }

    public void setLabelIcon(String componentId, ImageIcon icon) {
        JComponent component = getComponentById(componentId);
        if (component instanceof JLabel) {
            ((JLabel) component).setIcon(icon);
        }
    }
    public void setLabelText(String componentId, String text, boolean html) {
        JComponent component = getComponentById(componentId);
        if (component instanceof JLabel) {
            if (html) {
                text = "<html>" + text + "</html>";
            }
            ((JLabel) component).setText(text);
        }
    }

    private void buildPanels(Map<String, OptionGroups> groups, JPanel parentPanel) {
        if (groups != null) {
            for (Map.Entry<String, OptionGroups> entry : groups.entrySet()) {
                String componentGroup = entry.getKey();
                OptionGroups optionGroups = entry.getValue();
                JPanel thisPanel = frameConstructor.getPanel().createGroupPanel(optionGroups.getPanelOptions(), componentGroup);
                thisPanel.setName(componentGroup);
                thisPanel.setVisible(optionGroups.getPanelOptions().isVisible());
                processChildComponents(optionGroups.getChildComponents(), thisPanel);
                if (!panelsMap.containsKey(componentGroup)) {
                    addPanelGroup(parentPanel, thisPanel);
                    guiBuilderListener.onPanelBuild(groups, componentGroup, parentPanel);
                }
                buildPanels(optionGroups.getGroups(), thisPanel);
                childsNparents.computeIfAbsent(parentPanel.getName(), k -> new ArrayList<>()).add(thisPanel.getName());
            }
        }
    }

    private void processChildComponents(List<ComponentAttributes> childComponents, JPanel parentPanel) {
        for (ComponentAttributes componentAttributes : childComponents) {
            if (componentAttributes.getComponentType() != null) {
                JComponent component = componentFactory.createComponent(componentAttributes);
                parentPanel.add(component);
                componentsMap.computeIfAbsent(parentPanel.getName(), k -> new ArrayList<>()).add(component);
            } else if (componentAttributes.getGroups() != null) {
                buildPanels(componentAttributes.getGroups(), parentPanel);
            } else if (componentAttributes.getReadFrom() != null) {
                FrameAttributes frameAttributes = loadFrameAttributes(componentAttributes.getReadFrom());
                if (frameAttributes.getGroups() == null && frameAttributes.getChildComponents() != null && !frameAttributes.getChildComponents().isEmpty()) {
                    processChildComponents(frameAttributes.getChildComponents(), parentPanel);
                } else {
                    buildGui(componentAttributes.getReadFrom(), parentPanel);
                }
            } else if (componentAttributes.getLoadPanel() != null && !componentAttributes.getLoadPanel().isEmpty()) {
                loadPanels.put(componentAttributes.getLoadPanel(), parentPanel);
            }
        }
    }

    public void buildAdditionalPanels() {
        if (!additionalPanelsBuilt) {
            guiBuilderListener.onPanelsBuilt();
            Engine.LOGGER.debug(" == BUILDING ADDITIONAL PANELS ==");

            for (Map.Entry<String, JPanel> additional : loadPanels.entrySet()) {
                Engine.LOGGER.debug("Processing {}", additional.getKey());
                JPanel loadingPanel = panelsMap.get(additional.getKey());

                if (loadingPanel != null) {
                    addPanelGroup(additional.getValue(), loadingPanel);
                }
            }
            additionalPanelsBuilt = true;
        } else {
            Engine.LOGGER.error("Additional panels are already built!");
        }
    }

    private void addPanelGroup(JPanel parent, JPanel child) {
        parent.add(child);
        panelsMap.put(child.getName(), child);
    }

    public HashMap<String, List<JComponent>> getComponentsMap() {
        return componentsMap;
    }
    public void addPanelToMap(JPanel panel) {
        this.panelsMap.put(panel.getName(), panel);
    }

    public HashMap<String, JPanel> getPanelsMap() {
        return panelsMap;
    }

    public HashMap<String, List<String>> getChildsNparents() {
        return childsNparents;
    }

    public void setGuiBuilderListener(GuiBuilderListener guiBuilderListener) {
        this.guiBuilderListener = guiBuilderListener;
    }

    public ComponentFactory getComponentFactory() {
        return componentFactory;
    }
}
