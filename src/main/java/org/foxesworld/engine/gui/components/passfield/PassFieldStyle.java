package org.foxesworld.engine.gui.components.passfield;

import org.foxesworld.engine.gui.components.ComponentFactory;
import org.foxesworld.engine.gui.styles.StyleAttributes;
import org.foxesworld.engine.utils.ImageUtils;

import javax.swing.border.BevelBorder;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import static org.foxesworld.engine.utils.FontUtils.hexToColor;


public class PassFieldStyle {
    private final ComponentFactory componentFactory;
    public String echoChar = "";
    public float fontSize = 1.0f;

    public BufferedImage texture;
    public Color textColor;
    public Color caretColor;
    public Border border;
    private final List<Color> borderColor = new ArrayList<>();
    public int width,height, bevel;
    public String font;

    public PassFieldStyle(ComponentFactory componentFactory) {
        this.componentFactory = componentFactory;
        this.texture = this.componentFactory.getEngine().getImageUtils().getLocalImage(componentFactory.getStyle().getTexture());
        this.setBorder(componentFactory.getStyle());

        this.textColor = hexToColor(componentFactory.getStyle().getColor());
        this.caretColor = hexToColor(componentFactory.getStyle().getCaretColor());
        this.echoChar = "*";
        if(componentFactory.getStyle().getBorderRadius() != 0) {
            this.texture = this.componentFactory.getEngine().getImageUtils().getRoundedImage(this.texture, componentFactory.getStyle().getBorderRadius());
        }
    }

    private void setBorder(StyleAttributes styleAttributes){
        if(styleAttributes.getBorderColor() != null) {
            this.borderColor.add(hexToColor(styleAttributes.getBorderColor().split(",")[1]));
            this.borderColor.add(hexToColor(styleAttributes.getBorderColor().split(",")[2]));
            this.bevel = Integer.parseInt(styleAttributes.getBorderColor().split(",")[0]);
        }
    }

    public void apply(PassField pass) {
        pass.texture = this.texture;
        pass.setPaddingX(this.componentFactory.getStyle().getPaddingX());
        pass.setPaddingY(this.componentFactory.getStyle().getPaddingY());
        pass.setCaretColor(this.caretColor);
        pass.setBackground(this.textColor);
        pass.setForeground(this.textColor);
        if(this.componentFactory.getStyle().getBorderColor() != null) {
            pass.setBorder(new BevelBorder(this.bevel, borderColor.get(0), borderColor.get(1)));
        } else {
            pass.setBorder(null);
        }
    }
}
