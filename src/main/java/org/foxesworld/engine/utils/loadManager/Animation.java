package org.foxesworld.engine.utils.loadManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

public class Animation {
    private final Component component;
    private final Timer timer;
    private final List<KeyFrame> keyFrames = new ArrayList<>();
    private int duration;

    public Animation(Component component, int delay) {
        this.component = component;
        this.timer = new Timer(delay, new ActionListener() {
            long startTime = -1;

            @Override
            public void actionPerformed(ActionEvent e) {
                if (startTime == -1) {
                    startTime = System.currentTimeMillis();
                }
                long elapsed = System.currentTimeMillis() - startTime;
                float progress = Math.min(1.0f, (float) elapsed / duration);
                System.out.println("Animating with progress: " + progress);
                animate(progress);
                if (progress >= 1.0f) {
                    System.out.println("Animation complete.");
                    ((Timer) e.getSource()).stop();
                }
            }
        });
    }

    public void addKeyFrame(int time, Point location, float opacity) {
        keyFrames.add(new KeyFrame(time, location, opacity));
        duration = Math.max(duration, time);
    }

    public void start() {
        System.out.println("Starting animation.");
        timer.start();
    }

    private void animate(float progress) {
        if (keyFrames.isEmpty()) {
            return;
        }

        KeyFrame startFrame = keyFrames.get(0);
        KeyFrame endFrame = keyFrames.get(keyFrames.size() - 1);

        for (int i = 1; i < keyFrames.size(); i++) {
            if (keyFrames.get(i).time >= progress * duration) {
                endFrame = keyFrames.get(i);
                startFrame = keyFrames.get(i - 1);
                break;
            }
        }

        float frameProgress = (progress * duration - startFrame.time) / (float) (endFrame.time - startFrame.time);
        int x = interpolate(startFrame.location.x, endFrame.location.x, frameProgress);
        int y = interpolate(startFrame.location.y, endFrame.location.y, frameProgress);
        float opacity = interpolate(startFrame.opacity, endFrame.opacity, frameProgress);

        System.out.println("Setting location to: (" + x + ", " + y + ")");
        component.setLocation(x, y);
        setComponentOpacity(opacity);
    }

    private int interpolate(int start, int end, float progress) {
        return (int) (start + (end - start) * progress);
    }

    private float interpolate(float start, float end, float progress) {
        return start + (end - start) * progress;
    }

    private void setComponentOpacity(float opacity) {
        opacity = clampOpacity(opacity);
        System.out.println("Setting opacity to: " + opacity);
        if (component instanceof Window) {
            ((Window) component).setOpacity(opacity);
        }
    }

    private float clampOpacity(float opacity) {
        return Math.max(0.0f, Math.min(1.0f, opacity));
    }

    private static class KeyFrame {
        int time;
        Point location;
        float opacity;

        KeyFrame(int time, Point location, float opacity) {
            this.time = time;
            this.location = location;
            this.opacity = opacity;
        }
    }
}
