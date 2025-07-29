package org.foxesworld.engine.gui.components.panel;

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
    private final FrameConstructor frameConstructor;
    private BufferedImage texture;

    // Поле прозрачности: 1.0 - полностью непрозрачно, 0.0 - полностью прозрачно.
    private float alpha = 1.0f;

    public Panel(FrameConstructor frameConstructor) {
        this.frameConstructor = frameConstructor;
        // ИСПРАВЛЕНО: Сам главный Panel не должен быть видимым, он служит контейнером/фабрикой.
        // Поэтому его свойство opaque не имеет значения.
    }

    // Геттер прозрачности
    public float getAlpha() {
        return alpha;
    }

    /**
     * Устанавливает уровень прозрачности для панели и всех создаваемых ею дочерних панелей.
     * @param alpha значение от 0.0 (полностью прозрачно) до 1.0 (полностью непрозрачно).
     */
    public void setAlpha(float alpha) {
        if (alpha < 0f) {
            this.alpha = 0f;
        } else if (alpha > 1f) {
            this.alpha = 1f;
        } else {
            this.alpha = alpha;
        }
        // Перерисовываем все компоненты, которые могли быть созданы этим классом.
        // Если rootPanel уже создан, он будет перерисован.
        if (this.getComponentCount() > 0) {
            this.repaint();
        }
    }

    /**
     * Устанавливает текстуру для панели.
     * @param newTexture изображение текстуры.
     */
    public void setTexture(BufferedImage newTexture) {
        this.texture = newTexture;
        repaint(); // Перерисовываем панель с новой текстурой
    }


    public JPanel setRootPanel(FrameAttributes frameAttributes) {
        this.frameAttributes = frameAttributes;

        JPanel rootPanel = new JPanel(null, true) {
            @Override
            protected void paintComponent(Graphics g) {
                // Вызов super.paintComponent(g) здесь не нужен, так как панель непрозрачна (opaque=false)
                // и мы полностью контролируем ее отрисовку.
                Graphics2D g2d = (Graphics2D) g.create();
                try {
                    // Применяем общую прозрачность ко всему, что рисуется на панели
                    g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, Panel.this.alpha));

                    if (texture != null) {
                        g2d.drawImage(texture, 0, 0, getWidth(), getHeight(), this);
                    } else {
                        // Метод drawDarkenedBackground уже сам рисует на Graphics
                        drawDarkenedBackground(g2d);
                    }
                } finally {
                    g2d.dispose();
                }
            }
        };

        // ИСПРАВЛЕНО: Для поддержки прозрачности панель ДОЛЖНА быть непрозрачной.
        rootPanel.setOpaque(false);
        rootPanel.setName("rootPanel");
        return rootPanel;
    }

    public JPanel createGroupPanel(PanelAttributes panelOptions, String groupName, FrameConstructor frameConstructor) {
        JPanel groupPanel = new JPanel(null, panelOptions.isDoubleBuffered()) {
            @Override
            protected void paintComponent(Graphics g) {
                // ИСПРАВЛЕНО: Правильный порядок отрисовки для поддержки прозрачности.
                // Сначала рисуем свой фон, затем позволяем Swing нарисовать дочерние компоненты.
                Graphics2D g2d = (Graphics2D) g.create();
                try {
                    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                    // Устанавливаем общую прозрачность для фона панели
                    g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, Panel.this.alpha));

                    // 1. Отрисовка фона (текстура, изображение или цвет)
                    if (texture != null) {
                        g2d.drawImage(texture, 0, 0, getWidth(), getHeight(), this);
                    } else if (panelOptions.getBackgroundImage() != null) {
                        BufferedImage backgroundImage = frameConstructor.getAppFrame()
                                .getImageUtils()
                                .getLocalImage(panelOptions.getBackgroundImage());
                        g2d.drawImage(applyDarkening(backgroundImage, hexToColor(panelOptions.getBackground())), 0, 0, getWidth(), getHeight(), null);
                    } else {
                        // Рисуем фон цветом, который был установлен для панели
                        g2d.setColor(this.getBackground());
                        if (panelOptions.getCornerRadius() > 0) {
                            // Рисуем скругленный прямоугольник
                            g2d.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), panelOptions.getCornerRadius(), panelOptions.getCornerRadius()));
                        } else {
                            // Рисуем обычный прямоугольник
                            g2d.fillRect(0, 0, getWidth(), getHeight());
                        }
                    }

                    // 2. Отрисовка рамки (если она не задана через setBorder)
                    // Рамка будет рисоваться с той же прозрачностью, что и фон.
                    if (getBorder() == null && panelOptions.getCornerRadius() > 0) {
                        g2d.setColor(getForeground());
                        g2d.draw(new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, panelOptions.getCornerRadius(), panelOptions.getCornerRadius()));
                    }

                } finally {
                    g2d.dispose();
                }

                // 3. Вызываем super.paintComponent ПОСЛЕ отрисовки нашего фона.
                // Это позволит нарисовать дочерние компоненты (кнопки, метки и т.д.) поверх нашего фона.
                super.paintComponent(g);
            }
        };

        groupPanel.setName(groupName);

        // ИСПРАВЛЕНО: Панель с кастомной отрисовкой фона ВСЕГДА должна быть непрозрачной,
        // чтобы родительский компонент (или фон фрейма) мог "просвечивать" сквозь нее.
        groupPanel.setOpaque(false);

        groupPanel.setBackground(hexToColor(panelOptions.getBackground()));
        if (panelOptions.getBorder() != null && !panelOptions.getBorder().isEmpty()) {
            createBorder(groupPanel, panelOptions.getBorder());
        }

        if (panelOptions.getListener() != null && "dragger".equals(panelOptions.getListener())) {
            DragListener dragListener = new DragListener();
            dragListener.addDragListener(groupPanel, frameConstructor);
        }

        if (panelOptions.isFocusable()) {
            groupPanel.setFocusable(true);
            groupPanel.requestFocusInWindow(); // Более надежный способ запроса фокуса
        }

        Bounds bounds = panelOptions.getBounds();
        groupPanel.setBounds(bounds.getX(), bounds.getY(), bounds.getSize().getWidth(), bounds.getSize().getHeight());
        if (panelOptions.getLayout() != null) {
            groupPanel.setLayout(getLayout(panelOptions.getLayout(), groupPanel));
        }

        return groupPanel;
    }

    private void drawDarkenedBackground(Graphics2D g2d) {
        BufferedImage backgroundImage = frameConstructor.getAppFrame()
                .getImageUtils()
                .getLocalImage(getSeasonalBackground());
        // Рисуем затемненное изображение прямо на переданный Graphics2D
        g2d.drawImage(applyDarkening(backgroundImage, hexToColor(frameAttributes.getBackgroundBlur())), 0, 0, null);
    }

    private String getSeasonalBackground() {
        return switch (CurrentMonth.getCurrentMonth()) {
            case DECEMBER, JANUARY, FEBRUARY -> frameAttributes.getWinterImage();
            case MARCH, APRIL, MAY -> frameAttributes.getSpringImage();
            case JUNE, JULY, AUGUST -> frameAttributes.getSummerImage();
            case SEPTEMBER, OCTOBER, NOVEMBER -> frameAttributes.getAutumnImage();
        };
    }

    /**
     * УЛУЧШЕНО: Метод стал более эффективным. Он создает только одно новое изображение
     * и рисует на нем исходное, а затем накладывает полупрозрачный цветной слой.
     */
    private BufferedImage applyDarkening(BufferedImage image, Color darkeningColor) {
        if (image == null) {
            return new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB); // Возвращаем заглушку, если нет изображения
        }
        int width = image.getWidth();
        int height = image.getHeight();
        BufferedImage darkenedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = darkenedImage.createGraphics();

        // 1. Рисуем исходное изображение
        g2d.drawImage(image, 0, 0, null);

        // 2. Накладываем поверх полупрозрачный цветной прямоугольник
        g2d.setColor(darkeningColor); // Цвет уже содержит альфа-компонент из hex-строки
        g2d.fillRect(0, 0, width, height);

        g2d.dispose();
        return darkenedImage;
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
        try {
            String[] borderData = border.split(",");
            int top = Integer.parseInt(borderData[0].trim());
            int left = Integer.parseInt(borderData[1].trim());
            int bottom = Integer.parseInt(borderData[2].trim());
            int right = Integer.parseInt(borderData[3].trim());
            Color borderColor = hexToColor(borderData[4].trim());
            groupPanel.setBorder(new MatteBorder(top, left, bottom, right, borderColor));
        } catch (Exception e) {
            Engine.LOGGER.error("Failed to create border from string: '" + border + "'", e);
        }
    }
}