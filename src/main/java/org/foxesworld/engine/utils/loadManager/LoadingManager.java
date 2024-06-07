package org.foxesworld.engine.utils.loadManager;

import org.foxesworld.engine.Engine;
import org.foxesworld.engine.utils.ImageUtils;
import org.foxesworld.engine.utils.SpriteAnimation;

import javax.swing.*;
import java.util.List;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;

import static org.foxesworld.engine.utils.FontUtils.hexToColor;

public class LoadingManager extends JFrame {

    private List<SpriteAnimation> spriteAnimation = new ArrayList<>();
    private  List<String> descColor = new ArrayList<>(), titleColor= new ArrayList<>();
    private List<JPanel> backgrondPanel = new ArrayList<>();
    private final Engine engine;
    private String loadingText;
    private String loadingTitle;
    private final Timer loadingTimer;
    private final int dotLimit = 4;
    private JLabel loaderText, titleLabel;
    private final int animationSpeed = 5;
    private boolean isAnimating = false;

    private static final int FRAME_WIDTH = 500;
    private static final int FRAME_HEIGHT = 150;

    public LoadingManager(Engine engine, int index) {
        this.engine = engine;
        loadingText = engine.getLANG().getString("loading.msg");
        loadingTitle = engine.getLANG().getString("loading.title");
        for(LoadManagerAttributes attributes: engine.getEngineData().getLoadManager()){
            this.spriteAnimation.add(new SpriteAnimation(engine, attributes.getSpritePath(), attributes.getRows(), attributes.getCols(), attributes.getDelay(), new Rectangle(attributes.getBounds().getX(), attributes.getBounds().getY(), attributes.getBounds().getWidth(), attributes.getBounds().getHeight())));
            this.backgrondPanel.add(createBackgroundPanel(attributes.getBgPath(), attributes.getBlurColor(), this.engine));
            descColor.add(attributes.getDescColor());
            titleColor.add(attributes.getTitleColor());
        }

        this.loadingTimer = new Timer(500, e -> loaderText.setText(loadingText));

        initializeLoadingFrame(index);
    }

    private void initializeLoadingFrame(int index) {
        setUndecorated(true);
        setSize(FRAME_WIDTH, FRAME_HEIGHT);
        SpriteAnimation currentLoader = spriteAnimation.get(index);
        JPanel backgroundPanel = this.backgrondPanel.get(index);
        setContentPane(backgroundPanel);

        currentLoader.setBounds(currentLoader.getSpriteRect());
        backgroundPanel.add(currentLoader);

        titleLabel = createLabel(loadingTitle, 23, new Rectangle(120, 50, 300, 20), backgroundPanel);
        loaderText = createLabel(loadingText, 11, new Rectangle(120, 70, 400, 20), backgroundPanel);
        loaderText.setForeground(hexToColor(descColor.get(index)));
        titleLabel.setForeground(hexToColor(titleColor.get(index)));
        setAlwaysOnTop(true);

        addFrameComponentListener();

        setShape(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 20, 20));
    }

    private JPanel createBackgroundPanel(String image, String color, Engine engine) {
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

    public void startLoading() {
        setVisible(true);
        setSize(FRAME_WIDTH, FRAME_HEIGHT);
        if (!isAnimating) {
            startAnimation();
        }
    }

    private void startAnimation() {
        isAnimating = true;
        animateDown();
    }

    private void animateDown() {
        loadingTimer.start();
        updateLoadingFramePosition();

        int targetY = engine.getFrame().getY() + engine.getFrame().getHeight() / 2 - getHeight() / 2;
        int startY = engine.getFrame().getY();

        Timer downTimer = new Timer(animationSpeed, new ActionListener() {
            int currentY = startY;
            final double acceleration = 0.12;

            @Override
            public void actionPerformed(ActionEvent e) {
                if (currentY < targetY) {
                    setLocation(getX(), currentY);
                    currentY += acceleration * (targetY - currentY);

                    float opacity = (currentY - startY) / (float) (targetY - startY);
                    setOpacity(opacity);
                } else {
                    setLocation(getX(), targetY);
                    setOpacity(1.0f);
                    ((Timer) e.getSource()).stop();
                    if (!loadingTimer.isRunning()) {
                        oscillate();
                    }
                }
            }
        });
        downTimer.start();
    }

    private void oscillate() {
        int startX = getX();
        int deltaY = 5;
        int oscillationSpeed = 50;

        Timer oscillationTimer = new Timer(oscillationSpeed, new ActionListener() {
            int direction = 1;

            @Override
            public void actionPerformed(ActionEvent e) {
                setLocation(startX, getY() + direction * deltaY);
                direction *= -1;

                if (direction == 1) {
                    setOpacity(1.0f);
                }
            }
        });
        oscillationTimer.setRepeats(false);
        oscillationTimer.start();
    }

    private void animateUp() {
        loadingTimer.start();
        updateLoadingFramePosition();

        Timer upTimer = new Timer(animationSpeed, new ActionListener() {
            int targetHeight = 0;
            float targetOpacity = 0.0f;

            @Override
            public void actionPerformed(ActionEvent e) {
                if (getHeight() > targetHeight || getOpacity() > targetOpacity) {
                    int newHeight = (int) (getHeight() * (1 - 0.12));
                    float newOpacity = Math.max(0.0f, getOpacity() - 0.12f);

                    setBounds(getX(), getY() - (getHeight() - newHeight), getWidth(), newHeight);
                    setOpacity(newOpacity);
                } else {
                    ((Timer) e.getSource()).stop();
                    stopLoading();
                    setVisible(false);
                }
            }
        });

        upTimer.start();
    }


    public void setOpacity(float opacity) {
        float clampedOpacity = Math.max(0.0f, Math.min(1.0f, opacity));
        super.setOpacity(clampedOpacity);
        repaint();
    }

    public void setLoadingText(String loadingText, String loadingTitle, int sleep) {
        this.loadingText = engine.getLANG().getString(loadingText);
        this.loadingTitle = engine.getLANG().getString(loadingTitle);
        loaderText.setText(this.loadingText);

        Timer dotsTimer = new Timer(1000, new ActionListener() {
            int dotCount = 0;

            @Override
            public void actionPerformed(ActionEvent e) {
                dotCount++;
                titleLabel.setText(loadingTitle + ".".repeat(dotCount));
                if (dotCount >= dotLimit) {
                    ((Timer) e.getSource()).stop();
                }
            }
        });

        dotsTimer.start();

        Timer sleepTimer = new Timer(sleep, e -> dotsTimer.stop());
        sleepTimer.setRepeats(true);
        sleepTimer.start();
    }


    public void stopLoading() {
        if (isAnimating) {
            animateUp();
            loadingTimer.stop();
            isAnimating = false;
        }
    }

    public void toggleLoader() {
        if (loadingTimer.isRunning()) {
            stopLoading();
            this.dispose();
        } else {
            startLoading();
        }
    }

    public Timer getLoadingTimer() {
        return loadingTimer;
    }
}