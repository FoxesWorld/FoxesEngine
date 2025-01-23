package org.foxesworld.engine.gui.loadingManager;

import org.foxesworld.engine.Engine;
import org.foxesworld.engine.gui.FloatingWindow;
import org.foxesworld.engine.utils.animation.AnimationStats;

import javax.swing.*;
import java.util.List;

@SuppressWarnings("unused")
public abstract class LoadingManager extends FloatingWindow implements AnimationStats {
    protected List<LoadManagerAttributes> attributesList;
    protected String loadingText, loadingTitle, labelFont;
    protected Timer loadingTimer;
    protected JLabel loaderText, titleLabel;
    protected LoadingManager(Engine engine){
        super(engine);
    }

    protected abstract void initializeLoadingFrame(int index);

    public void setLoadingText(String loadingText, String loadingTitle) {
        this.loadingText = this.engine.getLANG().getString(loadingText);
        this.loadingTitle = this.engine.getLANG().getString(loadingTitle);
        SwingUtilities.invokeLater(() -> {
            loaderText.setText(this.loadingText);
            titleLabel.setText(this.loadingTitle);
        });
    }


    public void setLabelFont(String labelFont) {
        this.labelFont = labelFont;
    }

    public Timer getLoadingTimer() {
        return loadingTimer;
    }
}