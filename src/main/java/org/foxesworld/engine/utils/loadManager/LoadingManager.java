package org.foxesworld.engine.utils.loadManager;

import org.foxesworld.engine.Engine;
import org.foxesworld.engine.utils.BezierCurve;
import org.foxesworld.engine.utils.SpriteAnimation;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Point2D;
import java.awt.geom.RoundRectangle2D;
import java.util.List;

import static org.foxesworld.engine.utils.FontUtils.hexToColor;

public class LoadingManager extends JWindow {
    private final List<LoadManagerAttributes> attributesList;
    private final Engine engine;
    private String loadingText;
    private String loadingTitle;
    private final Timer loadingTimer;
    private final int dotLimit = 4;
    private JLabel loaderText, titleLabel;

    private static final int FRAME_WIDTH = 500;
    private static final int FRAME_HEIGHT = 150;
    private final int ANIMATION_DURATION = 300;
    private final int ANIMATION_SPEED;
    private BezierCurve curve;

    private boolean animating = false;

    public LoadingManager(Engine engine, int index) {
        this.engine = engine;
        this.attributesList = List.of(engine.getEngineData().getLoadManager());
        this.loadingText = engine.getLANG().getString("loading.msg");
        this.loadingTitle = engine.getLANG().getString("loading.title");

        this.loadingTimer = new Timer(500, e -> loaderText.setText(loadingText));
        this.ANIMATION_SPEED = attributesList.get(index).getAnimSpeed();

        initializeLoadingFrame(index);
    }

    private void initializeLoadingFrame(int index) {
        setSize(FRAME_WIDTH, FRAME_HEIGHT);

        LoadManagerAttributes attributes = attributesList.get(index);
        JPanel backgroundPanel = createBackgroundPanel(attributes.getBgPath(), attributes.getBlurColor());
        SpriteAnimation currentLoader = new SpriteAnimation(engine, attributes.getSpritePath(),
                attributes.getRows(), attributes.getCols(), attributes.getDelay(),
                new Rectangle(attributes.getBounds().getX(), attributes.getBounds().getY(),
                        attributes.getBounds().getSize().getWidth(), attributes.getBounds().getSize().getHeight()));

        setContentPane(backgroundPanel);
        currentLoader.setBounds(currentLoader.getSpriteRect());
        backgroundPanel.add(currentLoader);

        titleLabel = createLabel(loadingTitle, 23, new Rectangle(120, 50, 300, 35), backgroundPanel);
        loaderText = createLabel(loadingText, 11, new Rectangle(120, 75, 400, 20), backgroundPanel);
        loaderText.setForeground(hexToColor(attributes.getDescColor()));
        titleLabel.setForeground(hexToColor(attributes.getTitleColor()));

        setAlwaysOnTop(true);
        setLocationRelativeTo(engine.getFrame());
        addFrameComponentListener();
        setShape(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 20, 20));
    }

    private JPanel createBackgroundPanel(String image, String color) {
        JPanel backgroundPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Image bgImg = engine.getImageUtils().getScaledImage(engine.getImageUtils().getLocalImage(image), getWidth(), getHeight());
                g.drawImage(bgImg, 0, 0, getWidth(), getHeight(), this);
                g.setColor(hexToColor(color));
                g.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        backgroundPanel.setLayout(null);
        backgroundPanel.setBounds(0, 0, getWidth(), getHeight());
        return backgroundPanel;
    }

    private JLabel createLabel(String text, int fontSize, Rectangle bounds, JPanel panel) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Arial", Font.BOLD, fontSize));
        label.setBounds(bounds);
        panel.add(label);
        return label;
    }

    private void addFrameComponentListener() {
        engine.getFrame().addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentMoved(java.awt.event.ComponentEvent evt) {
                updateLoadingFramePosition();
            }
        });
    }

    private void updateLoadingFramePosition() {
        SwingUtilities.invokeLater(() -> {
            Point mainFrameCenter = getCenterPoint(engine.getFrame());
            setLocation(mainFrameCenter.x - getWidth() / 2, mainFrameCenter.y - getHeight() / 2);
        });
    }

    private Point getCenterPoint(JFrame frame) {
        int centerX = frame.getX() + frame.getWidth() / 2;
        int centerY = frame.getY() + frame.getHeight() / 2;
        return new Point(centerX, centerY);
    }

    private void animateLoadingWindow(boolean isEntry) {
        if (!animating) {
            animating = true;
            setVisible(true);

            Point mainFrameCenter = getCenterPoint(engine.getFrame());
            int startX = mainFrameCenter.x - this.getWidth() / 2;
            int startY = isEntry ? engine.getFrame().getY() - this.getHeight() : getY();
            int targetY = mainFrameCenter.y - this.getHeight() / 2;
            int endX = isEntry ? startX : engine.getFrame().getWidth();
            float startOpacity = isEntry ? 0.0f : 1.0f;
            float targetOpacity = isEntry ? 1.0f : 0.0f;

            KeyframeAnimation animation = new KeyframeAnimation(this, ANIMATION_DURATION / ANIMATION_SPEED, () -> {
                animating = false;
                if (!isEntry) {
                    setVisible(false);
                }
            });

            addAnimationFrames(animation, startX, endX, startY, targetY, startOpacity, targetOpacity, isEntry);
            animation.start();
        }
    }

    private void addAnimationFrames(KeyframeAnimation animation, int startX, int endX, int startY, int targetY, float startOpacity, float targetOpacity, boolean isEntry) {
        for (int i = 0; i < ANIMATION_SPEED; i++) {
            float t = (float) i / (ANIMATION_SPEED - 1);

            // Calculating current position
            int x = isEntry ? startX : (int) (startX + (endX - startX) * t);
            int y = isEntry ? (int) (startY + (targetY - startY) * t) : targetY;

            // Calculating current opacity
            float opacity = startOpacity + (targetOpacity - startOpacity) * t;

            // Adding keyframe
            animation.addKeyframe(opacity, new Point(x, y), ANIMATION_DURATION / ANIMATION_SPEED);
        }
    }

    public void setLoadingText(String loadingText, String loadingTitle, int sleep) {
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
            animateLoadingWindow(true);
        }
    }

    @SuppressWarnings("unused")
    public Timer getLoadingTimer() {
        return loadingTimer;
    }
}