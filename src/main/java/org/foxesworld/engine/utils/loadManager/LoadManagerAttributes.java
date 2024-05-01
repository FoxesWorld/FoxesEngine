package org.foxesworld.engine.utils.loadManager;

public class LoadManagerAttributes {
    private String spritePath, bgPath, blurColor, titleColor, descColor;
    private int rows, cols,delay;
    private Bounds bounds;
    public String getSpritePath() {
        return spritePath;
    }
    public String getBgPath() {
        return bgPath;
    }
    public String getBlurColor() {
        return blurColor;
    }
    public int getRows() {
        return rows;
    }
    public int getCols() {
        return cols;
    }
    public int getDelay() {
        return delay;
    }
    public Bounds getBounds() {
        return bounds;
    }
    public String getTitleColor() {
        return titleColor;
    }
    public String getDescColor() {
        return descColor;
    }
}
