package org.foxesworld.engine.gui.components.textArea;

import org.foxesworld.engine.gui.components.ComponentFactory;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class TextArea extends JTextArea {

    public TextArea(ComponentFactory componentFactory) {
        if(componentFactory.getComponentAttribute().getLocaleKey() != null) {
            setText(componentFactory.getEngine().getLANG().getString(componentFactory.getComponentAttribute().getLocaleKey()));
        }
        setOpaque(componentFactory.getStyle().isOpaque());
        setPreferredSize(new Dimension((int) componentFactory.getBounds().getWidth(), (int) componentFactory.getBounds().getHeight()));
        if(componentFactory.getComponentAttribute().getBorder() != null) {
            List borders = new List();
            for (String val : componentFactory.getComponentAttribute().getBorder().split(",")) {
                borders.add(val);
            }
            setBorder(new EmptyBorder(
                    Integer.parseInt(borders.getItem(0)),
                    Integer.parseInt(borders.getItem(1)),
                    Integer.parseInt(borders.getItem(2)),
                    Integer.parseInt(borders.getItem(3))));
        }
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
    }
}