package org.foxesworld.engine.gui.components.panel;
public class PanelAttributes {
    private boolean opaque = false, visible,focusable, doubleBuffered = true;
    private int cornerRadius;
    private String border = "", listener = "",background = "",backgroundImage,bounds = "", layout;

    public boolean isOpaque() {
        return opaque;
    }

    public boolean isVisible() {
        return visible;
    }

    public boolean isFocusable() {
        return focusable;
    }

    public int getCornerRadius() {
        return cornerRadius;
    }

    public String getBorder() {
        return border;
    }

    public String getListener() {
        return listener;
    }

    public String getLayout() {
        return layout;
    }

    public boolean isDoubleBuffered() {
        return doubleBuffered;
    }

    public String getBackground() {
        return background;
    }

    public String getBackgroundImage() {
        return backgroundImage;
    }

    public String getBounds() {
        return bounds;
    }

    public void setOpaque(boolean opaque) {
        this.opaque = opaque;
    }
}