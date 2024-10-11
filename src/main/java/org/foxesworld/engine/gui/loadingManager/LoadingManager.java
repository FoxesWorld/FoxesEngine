package org.foxesworld.engine.gui.loadingManager;

import org.foxesworld.engine.Engine;
import org.foxesworld.engine.utils.animation.AnimationManager;
import org.foxesworld.engine.utils.animation.AnimationStats;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static org.foxesworld.engine.utils.FontUtils.hexToColor;

@SuppressWarnings("unused")
public abstract class LoadingManager extends JWindow implements AnimationStats {
    protected List<LoadManagerAttributes> attributesList;
    protected Engine engine;
    protected String loadingText, loadingTitle, labelFont;
    protected Timer loadingTimer;
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
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                if (isEntry) {
                    animationManager.animate(isEntry);
                } else {
                    performFlyOutAndFade();
                }
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    if (!isEntry) {
                        setVisible(false);
                    }
                } catch (InterruptedException | ExecutionException e) {}
            }
        };
        worker.execute();
    }

    private void performFlyOutAndFade() {
        int targetX = getLocation().x + 100;  // Fly out to the right
        int targetY = getLocation().y;
        float opacity = 1.0f;

        while (getLocation().x < targetX && opacity > 0.0f) {
            SwingUtilities.invokeLater(() -> setLocation(getLocation().x + 1, targetY));
            opacity -= 0.01f;
            float finalOpacity = opacity;
            SwingUtilities.invokeLater(() -> setOpacity(finalOpacity));
            try {
                Thread.sleep(10);  // Adjust sleep for smoother animation
            } catch (InterruptedException ignored) {}
        }
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
        if (isVisible()) {
            animateLoadingWindow(false);
        } else {
            setSize(FRAME_WIDTH, FRAME_HEIGHT);
            setVisible(true);
            animateLoadingWindow(true);
        }
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
