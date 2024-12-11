package org.foxesworld.engine.gui.components.checkbox;

import org.foxesworld.engine.gui.components.ComponentFactory;
import org.foxesworld.engine.utils.ImageUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

import static org.foxesworld.engine.utils.FontUtils.hexToColor;

public class CheckboxStyle {
    public String fontName;
    public float fontSize;
    public Color color;
    public BufferedImage texture;
    private final ComponentFactory componentFactory;

    public CheckboxStyle(ComponentFactory componentFactory) {
        this.componentFactory = componentFactory;
        this.fontName = componentFactory.getStyle().getFont();
        this.fontSize = componentFactory.getStyle().getFontSize();
        this.color = hexToColor(componentFactory.getStyle().getColor());
        this.texture = componentFactory.getEngine().getImageUtils().getLocalImage(componentFactory.getStyle().getTexture());
    }

    public void apply(Checkbox checkbox) {
        checkbox.setVisible(true);
        checkbox.setForeground(this.color);
        checkbox.setFont(componentFactory.getEngine().getFONTUTILS().getFont(this.fontName, this.fontSize));
        int i = this.texture.getWidth() / 4;
        checkbox.defaultTX = this.texture.getSubimage(0, 0, i, i);
        checkbox.rolloverTX = this.texture.getSubimage(i, 0, i, i);
        checkbox.selectedTX = this.texture.getSubimage(i * 2, 0, i, i);
        checkbox.selectedRolloverTX = this.texture.getSubimage(i * 3, 0, i, i);
        checkbox.setIcon(new ImageIcon(checkbox.defaultTX));
        checkbox.setRolloverIcon(new ImageIcon(checkbox.rolloverTX));
        checkbox.setSelectedIcon(new ImageIcon(checkbox.selectedTX));
        checkbox.setRolloverSelectedIcon(new ImageIcon(checkbox.selectedRolloverTX));
    }
}

