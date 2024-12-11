package org.foxesworld.engine.gui.components;

import java.awt.*;
import java.util.ArrayList;
import java.util.Map;

@SuppressWarnings("unused")
public class ComponentAttributes extends Attributes {

    public ComponentAttributes() {
        this.childComponents = new ArrayList<>();
    }
    private int rowNum, colNum, imgCount, fontSize, selectedIndex =0;
    private boolean enabled, opaque, revealButton, repeat, lineWrap, visible;
    private  Object initialValue;
    private Map<String, String> styles;
    private LayoutConfig layoutConfig;
    private String keyCode, tooltipStyle, border, color, localeKey, imageIcon, readFrom, loadPanel, type, style, id,background, thumbImage, trackImage, alignment, toolTip, showIcon, hideIcon;
    private int iconWidth, iconHeight, totalFrames, delay, minValue, minorSpacing, majorSpacing, maxValue, borderRadius, stepSize;
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
    public String getShowIcon() {
        return showIcon;
    }
    public String getHideIcon() {
        return hideIcon;
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
    public Map<String, String> getStyles() {
        return styles;
    }
    public int getStepSize() {
        return stepSize;
    }

    public LayoutConfig getLayoutConfig() {
        return layoutConfig;
    }

    public static class LayoutConfig {
        private ComponentConfig label;
        private ComponentConfig slider;
        private ComponentConfig spinner;

        public ComponentConfig getLabel() {
            return label;
        }

        public ComponentConfig getSlider() {
            return slider;
        }

        public ComponentConfig getSpinner() {
            return spinner;
        }
    }

    public static class ComponentConfig {
        private int x;
        private int y;
        private int width;
        private int height;

        public int getX() {
            return x;
        }

        public void setX(int x) {
            this.x = x;
        }

        public int getY() {
            return y;
        }

        public void setY(int y) {
            this.y = y;
        }

        public int getWidth() {
            return width;
        }

        public void setWidth(int width) {
            this.width = width;
        }

        public int getHeight() {
            return height;
        }

        public void setHeight(int height) {
            this.height = height;
        }
    }

}