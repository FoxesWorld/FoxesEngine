package org.foxesworld.engine.gui.components;

import org.foxesworld.engine.gui.components.frame.OptionGroups;
import org.foxesworld.engine.utils.loadManager.Bounds;

import java.util.Map;

public class ComponentAttributes {
    @SuppressWarnings("unused")
    private int rowNum, colNum, imgCount, fontSize, selectedIndex =0;
    @SuppressWarnings("unused")
    private boolean enabled, opaque;
    @SuppressWarnings("unused")
    private String keyCode, border, initialValue, color, localeKey, imageIcon, readFrom, loadPanel, componentType, componentStyle, componentId,background, thumbImage, trackImage, alignment;
    @SuppressWarnings("unused")
    private int iconWidth, iconHeight, totalFrames, delay, minValue, minorSpacing, majorSpacing, maxValue, borderRadius;
    @SuppressWarnings("unused")
    private Map<String, OptionGroups> panels;
    @SuppressWarnings("unused")
    private Bounds bounds;
    @SuppressWarnings("unused")
    public void setInitialValue(String initialValue) {
        this.initialValue = initialValue;
    }

    @SuppressWarnings("unused")
    public void setSelectedIndex(int selectedIndex) {
        this.selectedIndex = selectedIndex;
    }

    @SuppressWarnings("unused")
    public String getReadFrom() {
        return readFrom;
    }

    @SuppressWarnings("unused")
    public String getBackground() {
        return background;
    }

    @SuppressWarnings("unused")
    public String getComponentType() {
        return componentType;
    }

    @SuppressWarnings("unused")
    public String getComponentStyle() {
        return componentStyle;
    }

    @SuppressWarnings("unused")
    public String getComponentId() {
        return componentId;
    }

    @SuppressWarnings("unused")
    public int getRowNum() {
        return rowNum;
    }

    @SuppressWarnings("unused")
    public int getColNum() {
        return colNum;
    }

    @SuppressWarnings("unused")
    public int getBorderRadius() {
        return borderRadius;
    }

    @SuppressWarnings("unused")
    public String getAlignment() {
        return alignment;
    }

    @SuppressWarnings("unused")
    public String getBorder() {
        return border;
    }

    @SuppressWarnings("unused")
    public int getImgCount() {
        return imgCount;
    }

    @SuppressWarnings("unused")
    public int getFontSize() {
        return fontSize;
    }

    @SuppressWarnings("unused")
    public boolean isEnabled() {
        return enabled;
    }

    @SuppressWarnings("unused")
    public String getKeyCode() {
        return keyCode;
    }

    @SuppressWarnings("unused")
    public String getInitialValue() {
        return initialValue;
    }

    @SuppressWarnings("unused")
    public String getColor() {
        return color;
    }

    @SuppressWarnings("unused")
    public String getLocaleKey() {
        return localeKey;
    }

    @SuppressWarnings("unused")
    public String getImageIcon() {
        return imageIcon;
    }

    @SuppressWarnings("unused")
    public boolean isOpaque() {
        return opaque;
    }

    @SuppressWarnings("unused")
    public int getIconWidth() {
        return iconWidth;
    }

    @SuppressWarnings("unused")
    public int getIconHeight() {
        return iconHeight;
    }

    @SuppressWarnings("unused")
    public int getTotalFrames() {
        return totalFrames;
    }

    @SuppressWarnings("unused")
    public int getDelay() {
        return delay;
    }

    @SuppressWarnings("unused")
    public Bounds getBounds() {
        return bounds;
    }

    @SuppressWarnings("unused")
    public String getLoadPanel() {
        return loadPanel;
    }

    @SuppressWarnings("unused")
    public Map<String, OptionGroups> getGroups() {
        return panels;
    }

    @SuppressWarnings("unused")
    public int getMinValue() {
        return minValue;
    }

    @SuppressWarnings("unused")
    public int getMaxValue() {
        return maxValue;
    }

    @SuppressWarnings("unused")
    public int getMinorSpacing() {
        return minorSpacing;
    }

    @SuppressWarnings("unused")
    public int getMajorSpacing() {
        return majorSpacing;
    }

    @SuppressWarnings("unused")
    public int getSelectedIndex() {
        return selectedIndex;
    }
}
