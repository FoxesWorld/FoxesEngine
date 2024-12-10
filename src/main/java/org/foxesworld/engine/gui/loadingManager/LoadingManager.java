package org.foxesworld.engine.gui.loadingManager;

import org.foxesworld.engine.Engine;
import org.foxesworld.engine.utils.animation.AnimationManager;
import org.foxesworld.engine.utils.animation.AnimationStats;

import javax.swing.*;
import java.awt.*;
import java.util.List;

import static org.foxesworld.engine.utils.FontUtils.hexToColor;

@SuppressWarnings("unused")
public abstract class LoadingManager extends JWindow implements AnimationStats {
    protected List<LoadManagerAttributes> attributesList;
    protected Engine engine;
    protected String loadingText, loadingTitle, labelFont;
    protected Timer loadingTimer;
    @Deprecated
    private final int dotLimit = 4;
    protected JLabel loaderText, titleLabel;
    protected boolean animating;
    protected int FRAME_WIDTH = 500;
    protected int FRAME_HEIGHT = 150;
    protected int ANIMATION_DURATION = 300;
    protected int ANIMATION_SPEED;
    protected AnimationManager animationManager;

    protected abstract void initializeLoadingFrame(int index);

    protected JPanel createBackgroundPanel(JPanel basePanel, String image, String color) {
        JPanel backgroundPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Image bgImg = engine.getImageUtils().getScaledImage(engine.getImageUtils().getLocalImage(image), basePanel.getWidth(), basePanel.getHeight());
                g.drawImage(bgImg, 0, 0, basePanel.getWidth(), basePanel.getHeight(), this);
                g.setColor(hexToColor(color));
                g.fillRect(0, 0, basePanel.getWidth(), basePanel.getHeight());
            }
        };
        backgroundPanel.setLayout(basePanel.getLayout());
        backgroundPanel.setBounds(basePanel.getX(), basePanel.getY(), basePanel.getWidth(), basePanel.getHeight());
        backgroundPanel.setName(basePanel.getName());
        for (Component component : basePanel.getComponents()) {
            backgroundPanel.add(component);
        }
        return backgroundPanel;
    }

    protected void addFrameComponentListener() {
        engine.getFrame().addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentMoved(java.awt.event.ComponentEvent evt) {
                updateLoadingFramePosition();
            }
        });
    }

    protected void updateLoadingFramePosition() {
        SwingUtilities.invokeLater(() -> {
            Point mainFrameCenter = getCenterPoint(engine.getFrame());
            setLocation(mainFrameCenter.x - getWidth() / 2, mainFrameCenter.y - getHeight() / 2);
        });
    }

    public Point getCenterPoint(JFrame frame) {
        int centerX = frame.getX() + frame.getWidth() / 2;
        int centerY = frame.getY() + frame.getHeight() / 2;
        return new Point(centerX, centerY);
    }

    public void animateLoadingWindow(boolean isEntry) {
        animationManager.animate(isEntry);
    }

    public void setLoadingText(String loadingText, String loadingTitle) {
        this.loadingText = engine.getLANG().getString(loadingText);
        this.loadingTitle = engine.getLANG().getString(loadingTitle);
        SwingUtilities.invokeLater(() -> {
            loaderText.setText(this.loadingText);
            titleLabel.setText(this.loadingTitle);
        });
    }

    public void toggleLoader() {
        this.engine.getExecutorServiceProvider().submitTask(() -> {
            if (isVisible()) {
                animateLoadingWindow(false);
            } else {
                setSize(FRAME_WIDTH, FRAME_HEIGHT);
                animateLoadingWindow(true);
            }
        }, "loaderAnimation");
    }

    public void setLabelFont(String labelFont) {
        this.labelFont = labelFont;
    }

    public Timer getLoadingTimer() {
        return loadingTimer;
    }

    public Engine getEngine() {
        return engine;
    }

    public boolean isAnimating() {
        return animating;
    }

    public void setAnimating(boolean animating) {
        this.animating = animating;
    }

    protected int getANIMATION_DURATION() {
        return ANIMATION_DURATION;
    }

    protected int getANIMATION_SPEED() {
        return ANIMATION_SPEED;
    }

    public void setFrameWidth(int frameWidth) {
        FRAME_WIDTH = frameWidth;
    }

    public void setFrameHeight(int frameHeight) {
        FRAME_HEIGHT = frameHeight;
    }

    public void setANIMATION_DURATION(int ANIMATION_DURATION) {
        this.ANIMATION_DURATION = ANIMATION_DURATION;
    }

    public void setANIMATION_SPEED(int ANIMATION_SPEED) {
        this.ANIMATION_SPEED = ANIMATION_SPEED;
    }

    protected int getFrameWidth() {
        return this.FRAME_WIDTH;
    }

    protected int getFrameHeight() {
        return this.FRAME_HEIGHT;
    }

    @Override
    public void animationStarted() {
        this.setVisible(true);
    }

    @Override
    public void animationFinished() {
        this.setVisible(false);
    }
}