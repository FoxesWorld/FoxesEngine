package org.foxesworld.engine.gui.components.dropBox;

import org.foxesworld.engine.gui.components.ComponentFactory;
import org.foxesworld.engine.utils.ImageUtils;

import java.awt.*;
import java.awt.image.BufferedImage;

import static org.foxesworld.engine.utils.FontUtils.hexToColor;


public class DropBoxStyle {

    private ComponentFactory componentFactory;
    public String fontName;
    public float fontSize;
    public Color color;
    public BufferedImage texture;

    public DropBoxStyle(ComponentFactory componentFactory) {
        this.componentFactory = componentFactory;
        this.fontName = componentFactory.style.getFont();
        this.fontSize = componentFactory.style.getFontSize();
        this.color = hexToColor(componentFactory.style.getColor());
        this.texture = componentFactory.engine.getImageUtils().getLocalImage(componentFactory.style.getTexture());
    }

    public void apply(DropBox dropBox) {
        dropBox.setForeground(this.color);
        dropBox.setFont(componentFactory.engine.getFONTUTILS().getFont(this.fontName, this.fontSize));
        int dropBoxH = this.texture.getHeight() / 7;
        int dropBoxW = this.texture.getWidth();
        dropBox.setColor(this.color);
        dropBox.setHoverColor(hexToColor(componentFactory.style.getHoverColor()));
        dropBox.setDefaultTX(getTexture(0, 0, dropBoxW, dropBoxH));
        dropBox.setRolloverTX(getTexture(0, dropBoxH, dropBoxW, dropBoxH));
        dropBox.setOpenedTX(getTexture(0, dropBoxH * 2, dropBoxW, dropBoxH));
        dropBox.setPanelTX(getTexture(0, dropBoxH * 3, dropBoxW - 45, dropBoxH));
        dropBox.setSelectedTX(getTexture(0, dropBoxH * 4, dropBoxW - 45, dropBoxH));
        dropBox.setPoint(this.componentFactory.engine.getImageUtils().getLocalImage("assets/ui/icons/point.png"));
    }

    private BufferedImage getTexture(int x, int y, int width, int height){
        BufferedImage clippedTexture;
       clippedTexture =  this.texture.getSubimage(x, y, width, height);
        if(componentFactory.style.getBorderRadius() != 0) {
            clippedTexture =  this.componentFactory.engine.getImageUtils().getRoundedImage(clippedTexture, componentFactory.style.getBorderRadius());
        }
        return  clippedTexture;
    }
}

