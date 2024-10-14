package org.foxesworld.engine.gui.components.utils.tooltip;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class CustomTooltip {
    private JWindow tooltip;
    private JLabel label;

    public CustomTooltip(Color backgroundColor, Color textColor, int borderRadius, Font font) {
        tooltip = new JWindow();
        tooltip.setLayout(new BorderLayout());

        RoundedPanel panel = new RoundedPanel(borderRadius);
        panel.setBackground(backgroundColor);
        panel.setLayout(new BorderLayout());

        label = new JLabel("", JLabel.CENTER);
        label.setForeground(textColor);
        label.setFont(font);
        panel.add(label, BorderLayout.CENTER);

        tooltip.add(panel, BorderLayout.CENTER);
        tooltip.setSize(150, 50);
        tooltip.setFocusableWindowState(false);
    }

    public void attachToComponent(JComponent component, String tooltipText) {
        label.setText(tooltipText);
        tooltip.setSize(tooltipText.length() * 10, 50);

        component.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                tooltip.setLocation(e.getLocationOnScreen().x, e.getLocationOnScreen().y + 20);
                tooltip.setVisible(true);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                tooltip.setVisible(false);
            }
        });
    }
}
