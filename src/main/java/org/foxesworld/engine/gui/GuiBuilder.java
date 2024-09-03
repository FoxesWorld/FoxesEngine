package org.foxesworld.engine.gui;

import com.google.gson.Gson;
import org.foxesworld.engine.Engine;
import org.foxesworld.engine.gui.attributes.ComponentAttributes;
import org.foxesworld.engine.gui.components.frame.FrameAttributes;
import org.foxesworld.engine.gui.attributes.OptionGroups;
import org.foxesworld.engine.gui.components.ComponentFactory;
import org.foxesworld.engine.gui.components.frame.Frame;

import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.*;

public class GuiBuilder {

    private final HashMap<String, List<Component>> componentsMap = new HashMap<>();
    private final HashMap<String, JComponent> componentsIDs = new HashMap<>();
    private final HashMap<String, JPanel> panelsMap = new HashMap<>();
    private final Frame frame;
    private final ComponentFactory componentFactory;

    public GuiBuilder(Engine engine) {
        engine.getLOGGER().debug("=== GUI BUILDER ===");
        this.frame = engine.getFrame();
        this.componentFactory = new ComponentFactory(engine);
    }

    /*
     * Method for building an interface based on a JSON file
     * Accepts the path to the file and the InputStream flag to specify the data source (resources or file)
     */
    public void buildGui(String framePath, boolean inputStream, JPanel parent) {
        Gson gson = new Gson();
        FrameAttributes frameAttributes;
        if (inputStream) {
            InputStreamReader reader = new InputStreamReader(Objects.requireNonNull(Frame.class.getClassLoader().getResourceAsStream(framePath)), StandardCharsets.UTF_8);
            frameAttributes = gson.fromJson(reader, FrameAttributes.class);
        } else {
            try {
                BufferedReader reader = new BufferedReader(new FileReader(framePath));
                frameAttributes = gson.fromJson(reader, FrameAttributes.class);
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
        // Building component group
        buildComponents(frameAttributes.getGroups(), parent);
    }

    /*
     * Method for building componentFactory based on a JSON structure
     */
    private void buildComponents(Map<String, OptionGroups> groups, JPanel parentPanel) {
        if (groups != null) {
            for (Map.Entry<String, OptionGroups> entry : groups.entrySet()) {
                String componentGroup = entry.getKey();
                this.frame.getAppFrame().getLOGGER().debug("Building group " + componentGroup + " with parent " + parentPanel.getName());
                OptionGroups optionGroups = entry.getValue();
                JPanel thisPanel = frame.getPanel().createGroupPanel(optionGroups.panelOptions, componentGroup);
                thisPanel.setName(componentGroup);
                thisPanel.setVisible(optionGroups.panelOptions.visible);
                this.createComponents(optionGroups.childComponents, thisPanel, thisPanel.getName());
                parentPanel.add(thisPanel);
                panelsMap.put(componentGroup, thisPanel);
                buildComponents(optionGroups.groups, thisPanel); // Recursive call for nested groups
            }
        }
    }

    /*
     * Method for building componentFactory based on a JSON structure
     */
    private void createComponents(List<ComponentAttributes> componentList, JPanel parentPanel, String parentGroupName) {
        for (ComponentAttributes componentAttributes : componentList) {
            if (componentAttributes.componentType != null) {
                JComponent component = this.componentFactory.createComponent(componentAttributes);
                parentPanel.add(component);
                this.addComponentToMap(parentGroupName, component);
                this.componentsIDs.put(component.getName(), component);
            } else if (componentAttributes.groups != null) {
                // Handle nested groups
                buildComponents(componentAttributes.groups, parentPanel);
            } else if (componentAttributes.readFrom != null) {
                // Handle reading from another JSON file
                buildGui(componentAttributes.readFrom, true, parentPanel);
            }
        }
    }

    /*
     * Method for adding a component to the component map
     */
    private void addComponentToMap(String groupId, Component component) {
        if (!componentsMap.containsKey(groupId)) {
            componentsMap.put(groupId, new ArrayList<>());
        }
        componentsMap.get(groupId).add(component);
    }

    /*
     * Getting a list of componentFactory by group name
     */
    public HashMap<String, List<Component>> getComponentsMap() {
        return componentsMap;
    }

    /*
     * Getting a panel map
     */
    public HashMap<String, JPanel> getPanelsMap() {
        return panelsMap;
    }

    public JComponent getComponentByID(String ID) {
        return componentsIDs.get(ID);
    }
}