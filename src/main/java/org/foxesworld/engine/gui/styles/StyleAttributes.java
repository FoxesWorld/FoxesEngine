package org.foxesworld.engine.gui.styles;

import java.util.Objects;

@SuppressWarnings("unused")
public class StyleAttributes {
    private String name,backgroundImage,background,color,hoverColor,caretColor,align,borderColor,trackImage, thumbImage,texture,font,selectionColor;
    private int width,height,paddingX,paddingY,fontSize, borderRadius, iconWidth, iconHeight;
    private boolean opaque;

    public String getName() {
        return name;
    }

    public String getBackgroundImage() {
        return backgroundImage;
    }

    public String getBackground() {
        return background;
    }

    public String getColor() {
        return color;
    }

    public String getHoverColor() {
        return hoverColor;
    }

    public String getCaretColor() {
        return caretColor;
    }

    public String getAlign() {
        return align;
    }

    public String getSelectionColor() {
        return selectionColor;
    }

    public String getBorderColor() {
        return borderColor;
    }

    public int getBorderRadius() {
        return borderRadius;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public String getFont() {
        return font;
    }

    public int getFontSize() {
        return fontSize;
    }

    public String getTexture() {
        return texture;
    }

    public boolean isOpaque() {
        return opaque;
    }

    public String getTrackImage() {
        return trackImage;
    }

    public int getPaddingX() {
        return paddingX;
    }

    public int getPaddingY() {
        return paddingY;
    }

    public String getThumbImage() {
        return thumbImage;
    }

    public boolean hasTransparentBackground() {
        return "transparent".equalsIgnoreCase(background);
    }

    public boolean hasBackgroundImage() {
        return backgroundImage != null && !backgroundImage.isEmpty();
    }

    public boolean isValidCSSColor(String color) {
        return color != null && color.matches("^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})$|^[a-zA-Z]+$");
    }

    public int getIconWidth() {
        return iconWidth;
    }

    public int getIconHeight() {
        return iconHeight;
    }

    @Override
    public String toString() {
        return "StyleAttributes{" +
                "name='" + name + '\'' +
                ", background='" + background + '\'' +
                ", color='" + color + '\'' +
                ", width=" + width +
                ", height=" + height +
                ", opaque=" + opaque +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StyleAttributes that = (StyleAttributes) o;
        return width == that.width &&
                height == that.height &&
                opaque == that.opaque &&
                Objects.equals(name, that.name) &&
                Objects.equals(background, that.background) &&
                Objects.equals(color, that.color);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, background, color, width, height, opaque);
    }
}