package org.foxesworld.engine.utils.animation;


import org.foxesworld.engine.gui.loadingManager.LoadingManager;

import java.awt.*;
import java.awt.geom.Point2D;

public class AnimationManager {
    private final LoadingManager loadingManager;
    private AnimationStats animationStats;
    private final int animationDuration;
    private final int animationSpeed;

    public AnimationManager(LoadingManager loadingManager, int animationDuration, int animationSpeed) {
        this.loadingManager = loadingManager;
        this.animationDuration = animationDuration;
        this.animationSpeed = animationSpeed;
    }

    public void animate(boolean isEntry) {
        if (!loadingManager.isAnimating()) {
            loadingManager.setAnimating(true);
            animationStats.animationStarted();

            Point mainFrameCenter = loadingManager.getCenterPoint(loadingManager.getEngine().getFrame());
            int startX = mainFrameCenter.x - loadingManager.getWidth() / 2;
            int startY = isEntry ? loadingManager.getEngine().getFrame().getY() - loadingManager.getHeight() : loadingManager.getY();
            int targetY = mainFrameCenter.y - loadingManager.getHeight() / 2;
            int endX = isEntry ? startX : loadingManager.getEngine().getFrame().getWidth();
            float startOpacity = isEntry ? 0.0f : 1.0f;
            float targetOpacity = isEntry ? 1.0f : 0.0f;

            Point2D[] controlPoints = {
                    new Point2D.Double(startX, startY),
                    new Point2D.Double(startX, (startY + targetY) / 2),
                    new Point2D.Double(endX, (startY + targetY) / 2),
                    new Point2D.Double(endX, targetY)
            };

            BezierCurve bezierCurve = new BezierCurve(controlPoints);

            KeyframeAnimation animation = new KeyframeAnimation(loadingManager, animationDuration / animationSpeed, () -> {
                loadingManager.setAnimating(false);
                if (!isEntry) {
                    animationStats.animationFinished();
                }
            });

            addAnimationFrames(animation, bezierCurve, startOpacity, targetOpacity, isEntry);
            animation.start();
        }
    }

    private void addAnimationFrames(KeyframeAnimation animation, BezierCurve bezierCurve, float startOpacity, float targetOpacity, boolean isEntry) {
        for (int i = 0; i < animationSpeed; i++) {
            float t = (float) i / (animationSpeed - 1);

            // Using easing
            float easedT = easeInOut(t);

            // Get current pos using Bezier curve
            Point2D point = bezierCurve.calculatePoint(easedT);
            int x = (int) point.getX();
            int y = (int) point.getY();

            // Get current opacity
            float opacity = startOpacity + (targetOpacity - startOpacity) * easedT;

            // Adding keyframe
            animation.addKeyframe(opacity, new Point(x, y), animationDuration / animationSpeed);
        }
    }

    // Non-linear method for (ease-in-out)
    private float easeInOut(float t) {
        return (float) (-0.5 * (Math.cos(Math.PI * t) - 1));
    }

    public void setAnimationStats(AnimationStats animationStats) {
        this.animationStats = animationStats;
    }
}