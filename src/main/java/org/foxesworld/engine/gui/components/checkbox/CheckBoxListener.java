package org.foxesworld.engine.gui.components.checkbox;

import javax.swing.*;

public interface CheckBoxListener {
    void onHover(JCheckBox checkbox);
    void onClick(JCheckBox checkbox);
    void onActivate(JCheckBox checkbox);
    void onDisable(JCheckBox checkbox);
}
