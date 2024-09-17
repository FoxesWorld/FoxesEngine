package org.foxesworld.engine.gui.components;

import org.foxesworld.engine.gui.components.frame.OptionGroups;

import java.util.Map;

@SuppressWarnings("unused")
public class ComponentAttributes {

    private int rowNum, colNum, imgCount, fontSize, selectedIndex =0;
    private boolean enabled, opaque, revealButton, repeat;
    private String keyCode, border, initialValue, color, localeKey, imageIcon, readFrom, loadPanel, componentType, componentStyle, componentId,background, thumbImage, trackImage, alignment;
    private int iconWidth, iconHeight, totalFrames, delay, minValue, minorSpacing, majorSpacing, maxValue, borderRadius;
    private Map<String, OptionGroups> panels;
    private Bounds bounds;

    public void setInitialValue(String initialValue) {
        this.initialValue = initialValue;
    }
    public void setSelectedIndex(int selectedIndex) {
        this.selectedIndex = selectedIndex;
    }
    public String getReadFrom() {
        return readFrom;
    }
    public String getBackground() {
        return background;
    }
    public String getComponentType() {
        return componentType;
    }
    public String getComponentStyle() {
        return componentStyle;
    }
    public String getComponentId() {
        return componentId;
    }
    public int getRowNum() {
        return rowNum;
    }
    public int getColNum() {
        return colNum;
    }
    public int getBorderRadius() {
        return borderRadius;
    }
    public String getAlignment() {
        return alignment;
    }
    public String getBorder() {
        return border;
    }
    public int getImgCount() {
        return imgCount;
    }
    public int getFontSize() {
        return fontSize;
    }
    public boolean isEnabled() {
        return enabled;
    }
    public String getKeyCode() {
        return keyCode;
    }
    public String getInitialValue() {
        return initialValue;
    }
    public String getColor() {
        return color;
    }
    public String getLocaleKey() {
        return localeKey;
    }
    public String getImageIcon() {
        return imageIcon;
    }
    public boolean isOpaque() {
        return opaque;
    }
    public boolean isrevealButton() {
        return revealButton;
    }
    public int getIconWidth() {
        return iconWidth;
    }
    public int getIconHeight() {
        return iconHeight;
    }
    public int getTotalFrames() {
        return totalFrames;
    }
    public int getDelay() {
        return delay;
    }
    public Bounds getBounds() {
        return bounds;
    }
    public String getLoadPanel() {
        return loadPanel;
    }
    public Map<String, OptionGroups> getGroups() {
        return panels;
    }
    public int getMinValue() {
        return minValue;
    }
    public int getMaxValue() {
        return maxValue;
    }
    public int getMinorSpacing() {
        return minorSpacing;
    }
    public int getMajorSpacing() {
        return majorSpacing;
    }
    public int getSelectedIndex() {
        return selectedIndex;
    }

    public boolean isRepeat() {
        return repeat;
    }
}
