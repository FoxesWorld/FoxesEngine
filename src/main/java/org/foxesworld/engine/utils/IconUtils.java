package org.foxesworld.engine.utils;

import com.formdev.flatlaf.extras.FlatSVGIcon;
import org.foxesworld.engine.Engine;
import org.foxesworld.engine.gui.components.ComponentAttributes;

import javax.swing.*;

public class IconUtils {

    private final Engine engine;

    public IconUtils(Engine engine) {
        this.engine = engine;
    }

    public ImageIcon getIcon(ComponentAttributes componentAttributes) {
        ImageIcon icon = null;
        if(componentAttributes.getImageIcon() != null) {
            String iconPath = componentAttributes.getImageIcon();
            if (iconPath.endsWith(".png") || iconPath.endsWith(".jpg")) {
                icon = new ImageIcon(this.engine.getImageUtils().getScaledImage(this.engine.getImageUtils().getLocalImage(componentAttributes.getImageIcon()), componentAttributes.getIconWidth(), componentAttributes.getIconHeight()));
                if (componentAttributes.getBorderRadius() != 0) {
                    icon = new ImageIcon(this.engine.getImageUtils().getRoundedImage(icon.getImage(), componentAttributes.getBorderRadius()));
                }
            } else if (iconPath.endsWith(".svg")) {
                icon = this.getVectorIcon(iconPath, componentAttributes.getIconWidth(), componentAttributes.getIconHeight());
            } else if (iconPath.endsWith(".gif")) {
                Engine.LOGGER.error("GIF is not supported yet!");
            }
        }

        return icon;
    }

    public ImageIcon getVectorIcon(String iconPath, float width, float height) {
        ImageIcon icon;
        ImageIcon tmpIcon = new FlatSVGIcon(iconPath);
        float scale = Math.min(height / tmpIcon.getIconHeight(), width / tmpIcon.getIconHeight());
        icon = new FlatSVGIcon(iconPath, scale);
        return icon;
    }
}
