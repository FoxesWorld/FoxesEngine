package org.foxesworld.engine.gui.components.checkbox;

import org.foxesworld.engine.gui.components.ComponentFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;

public class Checkbox extends JCheckBox {

    private CheckBoxListener checkBoxListener;
    public BufferedImage defaultTX;
    public BufferedImage rolloverTX;
    public BufferedImage selectedTX;
    public BufferedImage selectedRolloverTX;
    private final ComponentFactory componentFactory;

    public Checkbox(ComponentFactory componentFactory, String string) {
        super(string);
        this.componentFactory = componentFactory;
        this.setOpaque(false);
        this.setFocusable(false);
        this.listener(this);
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
    }

    public void listener(final JCheckBox checkbox) {
        this.addMouseListener(new MouseListener() {

            @Override
            public void mouseReleased(MouseEvent e) {}

            @Override
            public void mousePressed(MouseEvent e) {
                boolean isSel = checkbox.isSelected();
                if (isEnabled() && e.getButton() == MouseEvent.BUTTON1) {
                    if(checkBoxListener != null) {
                        checkBoxListener.onClick(checkbox);
                    }
                    if (isSel) {
                        componentFactory.engine.getSOUND().playSound("checkbox", "checkboxOff");
                        if(checkBoxListener != null) {
                            checkBoxListener.onActivate(checkbox);
                        }
                    } else {
                        componentFactory.engine.getSOUND().playSound("checkbox", "checkboxOn");
                        if(checkBoxListener != null) {
                            checkBoxListener.onDisable(checkbox);
                        }
                    }
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {}

            @Override
            public void mouseEntered(MouseEvent e) {
                if(checkBoxListener != null) {
                    checkBoxListener.onHover(checkbox);
                }
            }

            @Override
            public void mouseClicked(MouseEvent e) {}
        });
    }

    public void toggleCheckbox() {
        boolean isSel = isSelected();
        if (isSel) {
            componentFactory.engine.getSOUND().playSound("checkbox", "checkboxOff");
        } else {
            componentFactory.engine.getSOUND().playSound("checkbox", "checkboxOn");
        }
    }

    public void setCheckBoxListener(CheckBoxListener checkBoxListener) {
        this.checkBoxListener = checkBoxListener;
    }
}
