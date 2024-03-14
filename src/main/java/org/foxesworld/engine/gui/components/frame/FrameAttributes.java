package org.foxesworld.engine.gui.components.frame;

import org.foxesworld.engine.gui.components.ComponentAttributes;

import java.util.List;
import java.util.Map;

public class FrameAttributes {
    private String appTitle;
    private String appIcon;
    private int width;
    private int height;
    private boolean resizable;
    private String backgroundImage;
    private String springImage;
    private String summerImage;
    private String autumnImage;
    private String winterImage;
    private String backgroundBlur;
    private boolean undecorated;
    private Map<String, OptionGroups> groups;
    private List<ComponentAttributes> childComponents;

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

    public Map<String, OptionGroups> getGroups() {
        return groups;
    }

    public List<ComponentAttributes> getChildComponents() {
        return childComponents;
    }
}