package org.foxesworld.engine.gui.attributes;

import com.google.gson.annotations.SerializedName;

import java.util.Map;

public class ComponentAttributes {

    @SerializedName("readFrom")
    public String readFrom;

    @SerializedName("componentType")
    public String componentType;

    @SerializedName("componentStyle")
    public String componentStyle;
    private  boolean visible;
    public String componentId;
    public int rowNum;

    public int colNum;

    @SerializedName("initialValue")
    public String initialValue;
    
    private String color;

    @SerializedName("imgCount")
    public int imgCount;

    @SerializedName("fontSize")
    public int fontSize;

    @SerializedName("enabled")
    public boolean enabled;

    @SerializedName("localeKey")
    public String localeKey;

    @SerializedName("imageIcon")
    public String imageIcon;

    @SerializedName("iconWidth")
    public int iconWidth;

    @SerializedName("iconHeight")
    public int iconHeight;

    @SerializedName("totalFrames")
    public int totalFrames;

    @SerializedName("delay")
    public int delay;

    @SerializedName("bounds")
    public String bounds;

    @SerializedName("xPos")
    public int xPos;

    @SerializedName("yPos")
    public int yPos;

    @SerializedName("width")
    public int width;

    @SerializedName("height")
    public int height;

    @SerializedName("groups")
    public Map<String, OptionGroups> groups;
    
    public String getColor() {
        return color;
    }

    public boolean isVisible() {
        return visible;
    }
}