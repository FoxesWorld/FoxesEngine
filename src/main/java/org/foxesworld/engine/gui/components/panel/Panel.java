package org.foxesworld.engine.gui.components.panel;

import com.formdev.flatlaf.ui.FlatDesktopPaneUI;
import com.formdev.flatlaf.ui.FlatRootPaneUI;
import org.foxesworld.engine.Engine;
import org.foxesworld.engine.gui.components.Bounds;
import org.foxesworld.engine.gui.components.frame.FrameAttributes;
import org.foxesworld.engine.gui.components.frame.FrameConstructor;
import org.foxesworld.engine.utils.CurrentMonth;
import org.foxesworld.engine.utils.DragListener;

import javax.swing.*;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;

import static org.foxesworld.engine.utils.FontUtils.hexToColor;

public class Panel extends JPanel {
    private FrameAttributes frameAttributes;
    private JPanel groupPanel;
    private final FrameConstructor frameConstructor;

    public Panel(FrameConstructor frameConstructor) {
        this.frameConstructor = frameConstructor;
    }

    public JPanel setRootPanel(FrameAttributes frameAttributes) {
        this.frameAttributes = frameAttributes;
        JPanel rootPanel = new JPanel(null, true) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                drawDarkenedBackground(g);
            }
        };
        rootPanel.setOpaque(false);
        rootPanel.setName("rootPanel");
        return rootPanel;
    }

    private void drawDarkenedBackground(Graphics g) {
        BufferedImage backgroundImage = frameConstructor.getAppFrame()
                .getImageUtils()
                .getLocalImage(getSeasonalBackground());
        g.drawImage(applyDarkening(backgroundImage, hexToColor(frameAttributes.getBackgroundBlur())), 0, 0, null);
    }

    private String getSeasonalBackground() {
        return switch (CurrentMonth.getCurrentMonth()) {
            case DECEMBER, JANUARY, FEBRUARY -> frameAttributes.getWinterImage();
            case MARCH, APRIL, MAY -> frameAttributes.getSpringImage();
            case JUNE, JULY, AUGUST -> frameAttributes.getSummerImage();
            case SEPTEMBER, OCTOBER, NOVEMBER -> frameAttributes.getAutumnImage();
        };
    }

    private BufferedImage applyDarkening(BufferedImage image, Color darkeningColor) {
        int width = image.getWidth();
        int height = image.getHeight();
        BufferedImage darkenedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = darkenedImage.createGraphics();

        g2d.drawImage(image, 0, 0, null);

        BufferedImage alphaImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D gAlpha = alphaImage.createGraphics();

        gAlpha.setColor(new Color(0, 0, 0, 0));
        gAlpha.fillRect(0, 0, width, height);

        gAlpha.setColor(darkeningColor);
        gAlpha.setComposite(AlphaComposite.SrcOver.derive(0.5f));
        gAlpha.fillRect(0, 0, width, height);

        g2d.drawImage(alphaImage, 0, 0, null);

        g2d.dispose();
        gAlpha.dispose();

        return darkenedImage;
    }

    public JPanel createGroupPanel(PanelAttributes panelOptions, String groupName, FrameConstructor frameConstructor) {
        groupPanel = new JPanel(null, panelOptions.isDoubleBuffered()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                if (panelOptions.getBackgroundImage() != null) {
                    BufferedImage backgroundImage = frameConstructor.getAppFrame()
                            .getImageUtils()
                            .getLocalImage(panelOptions.getBackgroundImage());
                    g.drawImage(applyDarkening(backgroundImage, hexToColor(panelOptions.getBackground())), 0, 0, null);
                }

                if (panelOptions.getCornerRadius() != 0) {
                    int cornerRadius = panelOptions.getCornerRadius();
                    RoundRectangle2D roundedRectangle = new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), cornerRadius, cornerRadius);
                    g2d.setColor(getBackground());
                    g2d.fill(roundedRectangle);
                    g2d.setColor(getForeground());
                    g2d.draw(roundedRectangle);
                    g2d.dispose();
                }
            }

            @Override
            protected void paintBorder(Graphics g) {
                if (panelOptions.getCornerRadius() != 0) {
                    Graphics2D g2d = (Graphics2D) g.create();
                    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    int cornerRadius = panelOptions.getCornerRadius();
                    RoundRectangle2D roundedRectangle = new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), cornerRadius, cornerRadius);
                    g2d.setColor(getForeground());
                    g2d.draw(roundedRectangle);
                    g2d.dispose();
                }
            }
        };

        groupPanel.setName(groupName);
        groupPanel.setOpaque(panelOptions.getCornerRadius() == 0 && panelOptions.isOpaque());
        groupPanel.setBackground(hexToColor(panelOptions.getBackground()));
        if (panelOptions.getBorder() != null && !panelOptions.getBorder().isEmpty()) {
            createBorder(groupPanel, panelOptions.getBorder());
        }

        if (panelOptions.getListener() != null) {
            DragListener dragListener = new DragListener();
            if ("dragger".equals(panelOptions.getListener())) {
                dragListener.addDragListener(groupPanel, frameConstructor);
            }
        }

        if (panelOptions.isFocusable()) {
            groupPanel.setFocusable(true);
            groupPanel.requestFocus();
        }

        Bounds bounds = panelOptions.getBounds();
        groupPanel.setBounds(bounds.getX(), bounds.getY(), bounds.getSize().getWidth(), bounds.getSize().getHeight());
        if (panelOptions.getLayout() != null) {
            groupPanel.setLayout(getLayout(panelOptions.getLayout(), groupPanel));
        }

        return groupPanel;
    }

    private LayoutManager getLayout(String layout, JPanel panel) {
        return switch (layout.toLowerCase()) {
            case "flow" -> new FlowLayout();
            case "border" -> new BorderLayout();
            case "grid" -> new GridLayout();
            case "gridbag" -> new GridBagLayout();
            case "box" -> new BoxLayout(panel, BoxLayout.X_AXIS);
            default -> {
                Engine.LOGGER.error("Invalid layout type: " + layout);
                yield null;
            }
        };
    }

    private void createBorder(JPanel groupPanel, String border) {
        String[] borderData = border.split(",");
        int top = Integer.parseInt(borderData[0]);
        int left = Integer.parseInt(borderData[1]);
        int bottom = Integer.parseInt(borderData[2]);
        int right = Integer.parseInt(borderData[3]);
        Color borderColor = hexToColor(borderData[4]);
        groupPanel.setBorder(new MatteBorder(top, left, bottom, right, borderColor));
    }
}