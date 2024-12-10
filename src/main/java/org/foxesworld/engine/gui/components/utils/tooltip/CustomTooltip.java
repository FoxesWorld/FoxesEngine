package org.foxesworld.engine.gui.components.utils.tooltip;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.lang.ref.WeakReference;
import java.util.*;
import java.util.List;
import java.util.Timer;

public class CustomTooltip extends JWindow {
    private static final List<WeakReference<CustomTooltip>> activeTooltips = new ArrayList<>();
    private final JLabel label;
    private Timer tooltipTimer;
    private Timer fadeOutTimer;
    private float currentOpacity = 1.0f;

    public CustomTooltip(Color backgroundColor, Color textColor, int borderRadius, Font font) {
        setLayout(new BorderLayout());
        setBackground(new Color(0, 0, 0, 0));

        RoundedPanel panel = new RoundedPanel(borderRadius);
        panel.setBackground(backgroundColor);
        panel.setLayout(new BorderLayout());
        panel.setOpaque(false);

        label = new JLabel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, currentOpacity));
                g2d.dispose();
            }
        };
        label.setForeground(textColor);
        label.setFont(font);
        label.setHorizontalAlignment(JLabel.CENTER);
        panel.add(label, BorderLayout.CENTER);

        add(panel, BorderLayout.CENTER);
        setSize(150, 50);
        setFocusableWindowState(false);
    }

    public void attachToComponent(JComponent component, String tooltipText, int autoHideDelay) {
            if (component.isEnabled()) {
                label.setText(tooltipText);
                setSize(Math.max(150, tooltipText.length() * 10), 50);
                activeTooltips.add(new WeakReference<>(this));

                component.addMouseListener(new MouseAdapter() {
                    private Timer hoverDelayTimer;

                    @Override
                    public void mouseEntered(MouseEvent e) {
                        hoverDelayTimer = new Timer();
                        hoverDelayTimer.schedule(new TimerTask() {
                            @Override
                            public void run() {
                                SwingUtilities.invokeLater(() -> {
                                    Point location = component.getLocationOnScreen();
                                    setLocation(location.x, location.y + component.getHeight() + 5);
                                    setVisible(true);
                                    startAutoHideTimer(autoHideDelay);
                                });
                            }
                        }, 500);
                    }

                    @Override
                    public void mouseExited(MouseEvent e) {
                        if (hoverDelayTimer != null) {
                            hoverDelayTimer.cancel();
                        }
                        cancelAutoHideTimer();
                        fadeOutTooltip();
                    }
                });
            }
    }

    private void startAutoHideTimer(int delay) {
        cancelAutoHideTimer();
        tooltipTimer = new Timer();
        tooltipTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                fadeOutTooltip();
            }
        }, delay);
    }

    private void cancelAutoHideTimer() {
        if (tooltipTimer != null) {
            tooltipTimer.cancel();
            tooltipTimer = null;
        }
    }

    private void fadeOutTooltip() {
        if (fadeOutTimer != null) {
            fadeOutTimer.cancel();
        }

        fadeOutTimer = new Timer();
        fadeOutTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (currentOpacity > 0) {
                    currentOpacity -= 0.05f;
                    currentOpacity = Math.max(0.0f, currentOpacity);
                    repaint();
                } else {
                    setVisible(false);
                    dispose();
                    activeTooltips.removeIf(ref -> ref.get() == CustomTooltip.this);
                    fadeOutTimer.cancel();
                }
            }
        }, 0, 30);
    }

    public void clearAllTooltips() {
        for (WeakReference<CustomTooltip> ref : new ArrayList<>(activeTooltips)) {
            CustomTooltip tooltip = ref.get();
            if (tooltip != null) {
                tooltip.setVisible(false);
                tooltip.dispose();
            }
        }
        activeTooltips.clear();
    }

    private static class RoundedPanel extends JPanel {
        private final int borderRadius;

        public RoundedPanel(int borderRadius) {
            this.borderRadius = borderRadius;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Dimension arcs = new Dimension(borderRadius, borderRadius);
            int width = getWidth();
            int height = getHeight();
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(getBackground());
            g2.fillRoundRect(0, 0, width, height, arcs.width, arcs.height);
            g2.dispose();
        }
    }
}
