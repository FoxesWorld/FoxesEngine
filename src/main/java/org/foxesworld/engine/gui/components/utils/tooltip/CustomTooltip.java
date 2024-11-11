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
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
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
            setSize(Math.max(150, tooltipText.length() * 10), 50); // Гарантируем минимальный размер
            activeTooltips.add(new WeakReference<>(this));

            component.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    SwingUtilities.invokeLater(() -> {
                        setLocation(e.getLocationOnScreen().x, e.getLocationOnScreen().y + 20);
                        setVisible(true);
                        startAutoHideTimer(autoHideDelay);  // Используем переданную задержку
                    });
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    cancelAutoHideTimer();
                    setVisible(false);
                    dispose();
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
                setVisible(false);
                dispose();
                activeTooltips.removeIf(ref -> ref.get() == CustomTooltip.this);
            }
        }, delay);
    }

    private void cancelAutoHideTimer() {
        if (tooltipTimer != null) {
            tooltipTimer.cancel();
            tooltipTimer = null;
        }
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
}
