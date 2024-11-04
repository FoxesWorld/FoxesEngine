package org.foxesworld.engine.gui.components.frame;

import org.foxesworld.engine.gui.components.Attributes;
import org.foxesworld.engine.gui.components.ComponentAttributes;

import java.util.List;
import java.util.Map;

@SuppressWarnings("unused")
public class FrameAttributes extends Attributes {
    private String appTitle;
    private String appIcon;
    private int width, height, borderRadius;
    private boolean resizable;
    private String backgroundImage;
    private String springImage;
    private String summerImage;
    private String autumnImage;
    private String winterImage;
    private String backgroundBlur;
    private boolean undecorated;

    public String getAppTitle() {
        return appTitle;
    }

    public String getAppIcon() {
        return appIcon;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public boolean isResizable() {
        return resizable;
    }

    public String getBackgroundImage() {
        return backgroundImage;
    }

    public String getSpringImage() {
        return springImage;
    }

    public String getSummerImage() {
        return summerImage;
    }

    public String getAutumnImage() {
        return autumnImage;
    }

    public String getWinterImage() {
        return winterImage;
    }

    public String getBackgroundBlur() {
        return backgroundBlur;
    }

    public boolean isUndecorated() {
        return undecorated;
    }
    public int getBorderRadius() {
        return borderRadius;
    }
}