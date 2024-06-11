package org.foxesworld.engine.utils;

import com.formdev.flatlaf.extras.FlatSVGIcon;
import org.foxesworld.engine.Engine;
import org.foxesworld.engine.gui.components.ComponentAttributes;

import javax.swing.*;
import java.io.IOException;

public class IconUtils {

    private  Engine engine;

    public  IconUtils(Engine engine){
        this.engine = engine;
    }

    public ImageIcon getIcon(ComponentAttributes componentAttributes){
        ImageIcon icon = null;
        String iconPath = componentAttributes.getImageIcon();
        if(iconPath.endsWith(".png") || iconPath.endsWith(".jpg")) {
            icon = new ImageIcon(this.engine.getImageUtils().getScaledImage(this.engine.getImageUtils().getLocalImage(componentAttributes.getImageIcon()), componentAttributes.getIconWidth(), componentAttributes.getIconHeight()));
            if (componentAttributes.getBorderRadius() != 0) {
                icon = new ImageIcon(this.engine.getImageUtils().getRoundedImage(icon.getImage(), componentAttributes.getBorderRadius()));
            }
        } else if(iconPath.endsWith(".svg")) {
            ImageIcon tmpIcon = new FlatSVGIcon(iconPath);
            float scale = Math.min((float) componentAttributes.getIconHeight() / tmpIcon.getIconHeight(), (float) componentAttributes.getIconWidth() / tmpIcon.getIconHeight());
            icon = new FlatSVGIcon(iconPath, scale);
        } else if(iconPath.endsWith(".gif")) {
            Engine.LOGGER.error("GIF is not supported yet!");
        }

        return icon;
    }
}
