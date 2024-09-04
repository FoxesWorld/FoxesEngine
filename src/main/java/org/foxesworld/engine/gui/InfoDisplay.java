package org.foxesworld.engine.gui;

import javax.swing.*;
import java.awt.*;

@SuppressWarnings("unused")
public abstract class InfoDisplay {

    private long sleep = 700;
    private int animationDuration = 500;
    private int animationStepDelay = 15;

    public abstract void setInfo(String title, String desc);

    protected void animateTextFlyUp(JLabel label, String newText) {
        if (label.getText().equals(newText)) {
            return;
        }

        Timer timer = new Timer(animationStepDelay, null);
        final int totalSteps = animationDuration / animationStepDelay;
        final int[] step = {0};
        final Point startLocation = label.getLocation();
        final Point endLocation = new Point(startLocation.x, startLocation.y - 30);
        final float alphaStep = 1.0f / totalSteps;

        timer.addActionListener(e -> {
            float progress = (float) step[0] / totalSteps;
            int yOffset = (int) (startLocation.y - (progress * (startLocation.y - endLocation.y)));
            label.setLocation(startLocation.x, yOffset);

            label.setForeground(new Color(
                    label.getForeground().getRed(),
                    label.getForeground().getGreen(),
                    label.getForeground().getBlue(),
                    Math.max(0, (int) (255 * (1 - progress)))
            ));

            step[0]++;
            if (step[0] >= totalSteps) {
                timer.stop();
                label.setText("");
                label.setLocation(startLocation);
                label.setForeground(new Color(
                        label.getForeground().getRed(),
                        label.getForeground().getGreen(),
                        label.getForeground().getBlue(),
                        0));
                SwingUtilities.invokeLater(() -> animateTextFlyDown(label, newText));
            }
        });

        timer.start();
    }

    protected void animateTextFlyDown(JLabel label, String newText) {
        Timer timer = new Timer(animationStepDelay, null);
        final int totalSteps = animationDuration / animationStepDelay;
        final int[] step = {0};
        label.setText(newText);

        final Point startLocation = label.getLocation();
        final Point endLocation = new Point(startLocation.x, startLocation.y + 30);

        timer.addActionListener(e -> {
            float progress = (float) step[0] / totalSteps;
            int yOffset = (int) (endLocation.y - (progress * (endLocation.y - startLocation.y)));
            label.setLocation(startLocation.x, yOffset);

            label.setForeground(new Color(
                    label.getForeground().getRed(),
                    label.getForeground().getGreen(),
                    label.getForeground().getBlue(),
                    Math.min(255, (int) (255 * progress))
            ));

            step[0]++;
            if (step[0] >= totalSteps) {
                timer.stop();
                label.setLocation(startLocation);
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