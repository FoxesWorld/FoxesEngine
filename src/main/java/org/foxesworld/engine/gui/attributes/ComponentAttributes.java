package org.foxesworld.engine.gui.attributes;

import com.google.gson.annotations.SerializedName;

import java.util.Map;

public class ComponentAttributes {
    public String readFrom;
    public String componentType;
    public String componentStyle;
    private  boolean visible;
    public String componentId;
    public int rowNum;
    public int colNum;
    public String initialValue;
    private String color;
    public int imgCount;
    public int fontSize;
    public boolean enabled;
    public String localeKey;
    public String imageIcon;
    public int iconWidth;
    public int iconHeight;
    public int totalFrames;
    public boolean repeat, opaque;
    public int delay;
    public String bounds;
    public int xPos;
    public int yPos;
    public int width;
    public int height;
    public Map<String, OptionGroups> groups;
    
    public String getColor() {
        return color;
    }

    public boolean isVisible() {
        return visible;
    }
}