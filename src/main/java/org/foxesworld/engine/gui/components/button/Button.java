package org.foxesworld.engine.gui.components.button;

import org.foxesworld.engine.gui.components.ComponentAttributes;
import org.foxesworld.engine.gui.components.ComponentFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;

public class Button extends JButton implements MouseListener, MouseMotionListener {
    private Color hoverColor;
    private boolean entered = false, pressed = false;
    public BufferedImage defaultTX, rolloverTX, pressedTX, lockedTX;
    private final ComponentFactory componentFactory;
    private final ComponentAttributes buttonAttributes;
    private final int hoverShiftY = 1;

    public Button(ComponentFactory componentFactory, String text) {
        this.componentFactory = componentFactory;
        this.buttonAttributes = componentFactory.getComponentAttribute();
        addMouseListener(this);
        addMouseMotionListener(this);
        setText(text);
        setBorderPainted(false);
        setContentAreaFilled(false);
        setFocusPainted(false);
        setOpaque(componentFactory.getStyle().isOpaque());
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    public Button(ComponentFactory componentFactory, ImageIcon icon) {
        this(componentFactory, "");
        setIcon(icon);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        int w = getWidth();
        int h = getHeight();

        BufferedImage imageToDraw = defaultTX;

        if (!isEnabled()) {
            imageToDraw = lockedTX;
        } else if (pressed) {
            imageToDraw = pressedTX;
        } else if (entered) {
            g.setColor(this.hoverColor);
            imageToDraw = rolloverTX;
        }

        g.drawImage(imageToDraw, 0, 0, w, h, null);

        int shiftY = entered ? hoverShiftY : 0;

        if(isEnabled()) {
            if (getText() != null && !getText().isEmpty()) {
                FontMetrics fm = g.getFontMetrics();
                int textX = (w - fm.stringWidth(getText())) / 2;
                int textY = (h + fm.getAscent()) / 2 + shiftY;
                if (isEnabled()) {
                    g.setColor(entered ? this.hoverColor : getForeground());
                }
                g.drawString(getText(), textX, textY);
            }

            if (getIcon() != null) {
                Icon icon = getIcon();
                int iconX = (w - icon.getIconWidth()) / 2;
                int iconY = (h - icon.getIconHeight()) / 2 + shiftY;
                icon.paintIcon(this, g, iconX, iconY);
            }
        }
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        entered = true;
        if (isEnabled()) {
            componentFactory.getEngine().getSOUND().playSound("button", "hover");
        }
        repaint();
    }

    @Override
    public void mouseExited(MouseEvent e) {
        entered = false;
        repaint();
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (isEnabled() && e.getButton() == MouseEvent.BUTTON1) {
            ButtonClick();
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (pressed && e.getButton() == MouseEvent.BUTTON1) {
            pressed = false;
            repaint();
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {
    }

    @Override
    public void mouseDragged(MouseEvent e) {
    }

    @Override
    public void mouseMoved(MouseEvent e) {
    }

    public void ButtonClick() {
        String sound;
        if (this.buttonAttributes.getComponentId().contains("back")) {
            sound = "back";
        } else if (this.buttonAttributes.getComponentId().contains("small")) {
            sound = "clickSmall";
        } else {
            sound = "click";
        }

        componentFactory.getEngine().getSOUND().playSound("button", sound);
        pressed = true;
    }

    public void setPressed(boolean pressed) {
        this.pressed = pressed;
    }

    public void setHoverColor(Color hoverColor) {
        this.hoverColor = hoverColor;
    }
}
