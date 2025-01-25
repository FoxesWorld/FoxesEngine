package org.foxesworld.engine.gui.loadingManager;

import org.foxesworld.engine.Engine;
import org.foxesworld.engine.gui.FloatingWindow;
import org.foxesworld.engine.utils.animation.AnimationStats;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.Map;

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

    protected void changeComponentStatus(Map<String, JComponent> componentsMap, JPanel panel, boolean enabled) {
        SwingUtilities.invokeLater(() -> {
            for (Map.Entry<String, JComponent> componentMap : componentsMap.entrySet()) {
                componentMap.getValue().setEnabled(enabled);
            }
            panel.revalidate();
            panel.repaint();
        });
    }

    protected JPanel getOverlay() {
        JPanel loadingOverlay = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color background = getBackground();
                if (background != null) {
                    g2d.setColor(background);
                    g2d.fillRect(0, 30, getWidth(), getHeight());
                }
                g2d.dispose();
            }
        };

        loadingOverlay.setName("loadingOverlay");
        loadingOverlay.setBackground(new Color(0, 0, 0, 0));
        loadingOverlay.setOpaque(false);
        loadingOverlay.setLayout(null);
        return loadingOverlay;
    }


    public void setLabelFont(String labelFont) {
        this.labelFont = labelFont;
    }

    public Timer getLoadingTimer() {
        return loadingTimer;
    }
}