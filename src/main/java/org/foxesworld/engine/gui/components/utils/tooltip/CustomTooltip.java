package org.foxesworld.engine.gui.components.utils.tooltip;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

public class CustomTooltip extends JWindow {
    private final List<CustomTooltip> activeTooltips = new ArrayList<>();
    private final JLabel label;

    public CustomTooltip(Color backgroundColor, Color textColor, int borderRadius, Font font) {
        setLayout(new BorderLayout());
        setBackground(new Color(0, 0, 0, 0));

        RoundedPanel panel = new RoundedPanel(borderRadius);
        panel.setBackground(backgroundColor);
        panel.setLayout(new BorderLayout());
        panel.setOpaque(false);
        label = new JLabel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
                g2d.dispose();
            }
        };
        label.setForeground(textColor);
        label.setFont(font);
        label.setHorizontalAlignment(JLabel.CENTER);
        panel.add(label, BorderLayout.CENTER);

        add(panel, BorderLayout.CENTER);
        setSize(150, 50);
        setFocusableWindowState(false);
    }

    public void attachToComponent(JComponent component, String tooltipText) {
        label.setText(tooltipText);
        setSize(tooltipText.length() * 10, 50);
        activeTooltips.add(this);

        component.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                setLocation(e.getLocationOnScreen().x, e.getLocationOnScreen().y + 20);
                setVisible(true);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                setVisible(false);
                dispose();
                if(!isVisible()) {
                    activeTooltips.remove(CustomTooltip.this);
                }
            }
        });
    }
    public void clearAllTooltips() {
        for (CustomTooltip tooltip : new ArrayList<>(activeTooltips)) {
            tooltip.setVisible(false);
            tooltip.dispose();
        }
        activeTooltips.clear();
    }
}
