package org.foxesworld.engine.gui;

import org.foxesworld.engine.gui.components.frame.OptionGroups;

import javax.swing.*;
import java.util.Map;

public interface GuiBuilderListener {
    void onGuiBuilt();
    void onPanelBuild(Map<String, OptionGroups> panels, String componentGroup, JPanel parentPanel);
    void onPanelsBuilt();
    void onAdditionalPanelBuild(JPanel panel);
}
