package org.foxesworld.engine.utils.loadManager;

import org.foxesworld.engine.Engine;
import org.foxesworld.engine.utils.SpriteAnimation;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.util.List;

import static org.foxesworld.engine.utils.FontUtils.hexToColor;

public class LoadingManager extends JWindow implements AnimationStats {
    private final List<LoadManagerAttributes> attributesList;
    private final Engine engine;
    private String loadingText, loadingTitle, labelFont;
    private final Timer loadingTimer;
    private final int dotLimit = 4;
    private JLabel loaderText, titleLabel;
    private boolean animating;

    private static final int FRAME_WIDTH = 500;
    private static final int FRAME_HEIGHT = 150;
    private final int ANIMATION_DURATION = 300;
    private final int ANIMATION_SPEED;
    private final AnimationManager animationManager;

    public LoadingManager(Engine engine, int index) {
        this.engine = engine;
        this.attributesList = List.of(engine.getEngineData().getLoadManager());
        this.loadingText = engine.getLANG().getString("loading.msg");
        this.loadingTitle = engine.getLANG().getString("loading.title");

        this.loadingTimer = new Timer(500, e -> loaderText.setText(loadingText));
        this.ANIMATION_SPEED = attributesList.get(index).getAnimSpeed();

        this.animationManager = new AnimationManager(this, ANIMATION_DURATION, ANIMATION_SPEED);
        this.animationManager.setAnimationStats(this);
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
        this.labelFont = attributes.getFont();
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
        Font font = this.engine.getFONTUTILS().getFont(labelFont, fontSize);
        label.setFont(font);
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

    public Point getCenterPoint(JFrame frame) {
        int centerX = frame.getX() + frame.getWidth() / 2;
        int centerY = frame.getY() + frame.getHeight() / 2;
        return new Point(centerX, centerY);
    }

    public void animateLoadingWindow(boolean isEntry) {
        animationManager.animate(isEntry);
    }

    public void setLoadingText(String loadingText, String loadingTitle) {
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

    public void setLabelFont(String labelFont) {
        this.labelFont = labelFont;
    }

    @SuppressWarnings("unused")
    public Timer getLoadingTimer() {
        return loadingTimer;
    }

    public Engine getEngine() {
        return engine;
    }

    public boolean isAnimating() {
        return animating;
    }

    public void setAnimating(boolean animating) {
        this.animating = animating;
    }

    @Override
    public void animationStarted() {
        this.setVisible(true);
    }

    @Override
    public void animationFinished() {
        this.setVisible(false);
    }
}