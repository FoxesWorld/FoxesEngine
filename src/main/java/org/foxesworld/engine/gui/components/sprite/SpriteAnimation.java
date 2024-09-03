package org.foxesworld.engine.gui.components.sprite;

import org.foxesworld.engine.gui.attributes.ComponentAttributes;
import org.foxesworld.engine.gui.components.ComponentFactory;
import org.foxesworld.engine.utils.ImageUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.logging.Logger;

public class SpriteAnimation extends JComponent {
    private static final Logger logger = Logger.getLogger(SpriteAnimation.class.getName());
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
        initialize(componentFactory.getComponentAttributes());
        startAnimation(this.playOnce);
    }

    private void initialize(ComponentAttributes componentAttributes) {
        this.spriteSheet = ImageUtils.getLocalImage(componentAttributes.imageIcon);
        this.setOpaque(componentAttributes.opaque);
        this.rows = componentAttributes.rowNum;
        this.columns = componentAttributes.colNum;
        this.delay = componentAttributes.delay;
        this.playOnce = componentAttributes.repeat;
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
                        currentFrame = lastFrame - 1;  // Hold the last frame
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