package org.foxesworld.engine.gui.components.progressBar;

import org.foxesworld.engine.gui.components.ComponentFactory;
import org.foxesworld.engine.gui.styles.StyleAttributes;

import javax.swing.*;

import static org.foxesworld.engine.utils.FontUtils.hexToColor;

public class ProgressBarStyle {
    private String background;
    private  String forgeground;
    private  String border;
    private StyleAttributes styleAttributes;

    public ProgressBarStyle(StyleAttributes styleAttributes) {
        this.styleAttributes = styleAttributes;
        this.background = styleAttributes.backgroundImage;
        this.forgeground = styleAttributes.background;
        this.border = styleAttributes.borderColor;
    }

    public void apply(JProgressBar progressBar) {
        progressBar.setBackground(hexToColor(background));
        progressBar.setForeground(hexToColor(forgeground));
        progressBar.setBorder(BorderFactory.createLineBorder(hexToColor(border)));
        setTexture(progressBar, styleAttributes.texture);
    }

    private void setTexture(JProgressBar progressBar, String imagePath) {
        progressBar.setUI(new TexturedProgressBar(imagePath));
    }
}
