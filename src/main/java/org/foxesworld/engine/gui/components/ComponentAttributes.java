package org.foxesworld.engine.gui.components;

import java.awt.*;
import java.util.ArrayList;

@SuppressWarnings("unused")
public class ComponentAttributes extends Attributes {

    public ComponentAttributes() {
        this.childComponents = new ArrayList<>();
    }
    private int rowNum, colNum, imgCount, fontSize, selectedIndex =0;
    private boolean enabled, opaque, revealButton, repeat, lineWrap, visible;
    private  Object initialValue;
    private String keyCode, tooltipStyle, border, color, localeKey, imageIcon, readFrom, loadPanel, type, style, id,background, thumbImage, trackImage, alignment, toolTip;
    private int iconWidth, iconHeight, totalFrames, delay, minValue, minorSpacing, majorSpacing, maxValue, borderRadius;
    private Bounds bounds;
    public void setInitialValue(Object initialValue) {
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
        return type;
    }
    public String getComponentStyle() {
        return style;
    }
    public String getComponentId() {
        return id;
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
    public Object getInitialValue() {
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
    public Rectangle getBounds() {
        return new Rectangle(bounds.getX(), bounds.getY(), bounds.getSize().getWidth(), bounds.getSize().getHeight());
    }
    public String getLoadPanel() {
        return loadPanel;
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
    public String getToolTip() {
        return toolTip;
    }
    public boolean isVisible() {
        return visible;
    }
    public boolean isLineWrap() {
        return lineWrap;
    }
    public boolean isRepeat() {
        return repeat;
    }
    public String getTooltipStyle() {
        return tooltipStyle;
    }
}