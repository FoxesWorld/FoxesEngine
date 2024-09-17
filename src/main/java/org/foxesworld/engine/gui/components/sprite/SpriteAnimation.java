package org.foxesworld.engine.gui.components.sprite;

import org.foxesworld.engine.gui.components.ComponentAttributes;
import org.foxesworld.engine.gui.components.ComponentFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

@SuppressWarnings("unused")
public class SpriteAnimation extends JComponent {
    private final ComponentFactory componentFactory;
    private BufferedImage spriteSheet;
    private int rows, columns, delay;
    private int currentFrame = 0;
    private boolean playOnce;
    private boolean alreadyPlayed = false;
    private Timer timer;
    private boolean animationStopped = false;
    private float alpha = 1.0f;
    private Dimension frameSize;

    public SpriteAnimation(ComponentFactory componentFactory) {
        this.componentFactory = componentFactory;
        initialize(componentFactory.getComponentAttribute());
        startAnimation(this.playOnce);
    }

    private void initialize(ComponentAttributes componentAttributes) {
        this.spriteSheet = componentFactory.engine.getImageUtils().getLocalImage(componentAttributes.getImageIcon());
        this.setOpaque(componentAttributes.isOpaque());
        this.rows = componentAttributes.getRowNum();
        this.columns = componentAttributes.getColNum();
        this.delay = componentAttributes.getDelay();
        this.playOnce = componentAttributes.isRepeat();
        this.frameSize = new Dimension(spriteSheet.getWidth() / columns, spriteSheet.getHeight() / rows);

        setPreferredSize(frameSize);
    }

    private void startAnimation(boolean repeat) {
        if (playOnce && alreadyPlayed) {
            return;
        }

        int lastFrame = calculateLastFrame();
        timer = new Timer(delay, e -> {
            if (!animationStopped) {
                if (currentFrame < lastFrame - 1) {
                    currentFrame++;
                } else {
                    if (repeat) {
                        currentFrame = 0;
                    } else {
                        stop();
                        alreadyPlayed = true;
                        currentFrame = lastFrame - 1;
                    }
                }
                repaint();
            }
        });
        timer.start();
    }

    private void fadeOut() {
        new Timer(50, e -> {
            if (alpha > 0) {
                alpha -= 0.05f;
                alpha = Math.max(alpha, 0);
                repaint();
            } else {
                ((Timer) e.getSource()).stop();
                stop();
            }
        }).start();
    }

    private int calculateLastFrame() {
        return rows * columns;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        int scaledWidth = 128;
        int scaledHeight = 128;

        int frameWidth = spriteSheet.getWidth() / columns;
        int frameHeight = spriteSheet.getHeight() / rows;

        int row = currentFrame / columns;
        int column = currentFrame % columns;

        g.drawImage(
                spriteSheet.getSubimage(column * frameWidth, row * frameHeight, frameWidth, frameHeight),
                0,
                0,
                scaledWidth,
                scaledHeight,
                this
        );
    }

    public void updateImage(BufferedImage newSpriteSheet, int cols, int rows, int delay, boolean repeat) {
        stop();
        this.spriteSheet = newSpriteSheet;
        this.columns = cols;
        this.rows = rows;
        this.delay = delay;
        this.playOnce = repeat;
        this.alreadyPlayed = false;
        this.alpha = 1.0f;
        this.frameSize = new Dimension(spriteSheet.getWidth() / columns, spriteSheet.getHeight() / rows);
        setPreferredSize(frameSize);
        start(repeat);
    }

    private void stop() {
        if (timer != null) {
            timer.stop();
        }
        animationStopped = true;
    }

    private void start(boolean repeat) {
        currentFrame = 0;
        animationStopped = false;
        startAnimation(repeat);
    }

    @Override
    protected void finalize() throws Throwable {
        stop();
        super.finalize();
    }

    public void setAlpha(float alpha) {
        this.alpha = Math.max(0, Math.min(1, alpha));
        repaint();
    }

    public void resetAnimation() {
        stop();
        currentFrame = 0;
        alpha = 1.0f;
        alreadyPlayed = false;
        start(playOnce);
    }

    public boolean isAnimationStopped() {
        return animationStopped;
    }

    public void setAnimationStopped(boolean animationStopped) {
        this.animationStopped = animationStopped;
    }

    public int getCurrentFrame() {
        return currentFrame;
    }
}