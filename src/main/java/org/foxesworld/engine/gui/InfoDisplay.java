package org.foxesworld.engine.gui;

import javax.swing.*;
import java.awt.*;

@SuppressWarnings("unused")
public abstract class InfoDisplay {

    private long sleep = 700;
    private int animationDuration = 500;
    private int animationStepDelay = 15;

    public abstract void setInfo(String title, String desc);

    public abstract void setInfo(String s, String s1, int await);
    protected void animateTextFlyUp(JLabel label, String newText) {
        if (label.getText().equals(newText)) {
            return;
        }

        Timer timer = new Timer(animationStepDelay, null);
        final int totalSteps = animationDuration / animationStepDelay;
        final int[] step = {0};
        final Point startLocation = label.getLocation();
        final int targetY = startLocation.y - 30;
        final float alphaStep = 1.0f / totalSteps;

        timer.addActionListener(e -> {
            float progress = (float) step[0] / totalSteps;

            // Плавное движение вверх
            int currentY = (int) (startLocation.y - (progress * 30));
            label.setLocation(startLocation.x, currentY);

            // Плавное исчезновение
            int alpha = (int) (255 * (1 - progress));
            label.setForeground(new Color(
                    label.getForeground().getRed(),
                    label.getForeground().getGreen(),
                    label.getForeground().getBlue(),
                    Math.max(0, alpha)
            ));

            if (++step[0] >= totalSteps) {
                timer.stop();
                prepareForNextAnimation(label, startLocation);
                SwingUtilities.invokeLater(() -> animateTextFlyDown(label, newText));
            }
        });
        timer.start();
    }

    private void prepareForNextAnimation(JLabel label, Point location) {
        label.setText("");
        label.setLocation(location);
        label.setForeground(new Color(
                label.getForeground().getRed(),
                label.getForeground().getGreen(),
                label.getForeground().getBlue(),
                0));
    }

    protected void animateTextFlyDown(JLabel label, String newText) {
        Timer timer = new Timer(animationStepDelay, null);
        final int totalSteps = animationDuration / animationStepDelay;
        final int[] step = {0};

        final Point targetLocation = label.getLocation();
        final Point startLocation = new Point(targetLocation.x, targetLocation.y - 30);

        label.setText(newText);
        label.setLocation(startLocation);
        label.setForeground(new Color(
                label.getForeground().getRed(),
                label.getForeground().getGreen(),
                label.getForeground().getBlue(),
                0));

        timer.addActionListener(e -> {
            float progress = (float) step[0] / totalSteps;

            // Плавное движение вниз
            int currentY = (int) (startLocation.y + (progress * 30));
            label.setLocation(startLocation.x, currentY);

            // Плавное появление
            int alpha = (int) (255 * progress);
            label.setForeground(new Color(
                    label.getForeground().getRed(),
                    label.getForeground().getGreen(),
                    label.getForeground().getBlue(),
                    Math.min(255, alpha)
            ));

            if (++step[0] >= totalSteps) {
                timer.stop();
                // Финализируем позицию и прозрачность
                label.setLocation(targetLocation);
                label.setForeground(new Color(
                        label.getForeground().getRed(),
                        label.getForeground().getGreen(),
                        label.getForeground().getBlue(),
                        255));
            }
        });
        timer.start();
    }

    public void setSleep(long sleep) {
        this.sleep = sleep;
    }

    public void setAnimationDuration(int animationDuration) {
        this.animationDuration = animationDuration;
    }

    public void setAnimationStepDelay(int animationStepDelay) {
        this.animationStepDelay = animationStepDelay;
    }

    public long getSleep() {
        return sleep;
    }
}