package org.foxesworld.engine.gui.components.label;

import org.foxesworld.engine.gui.components.ComponentFactory;
import org.foxesworld.engine.gui.styles.StyleAttributes;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.io.Serial;
import java.util.Arrays;
import java.util.List;

import static org.foxesworld.engine.utils.FontUtils.hexToColor;

public class Label extends JLabel {
	@Serial
	private static final long serialVersionUID = 1L;
	private Color startColor, endColor;
	private boolean isGradientText = false;
	private boolean isVerticalGradient = false;
	private ComponentFactory componentFactory;

	public Label(ComponentFactory componentFactory) {
		this.componentFactory = componentFactory;
		String localeKey = componentFactory.getComponentAttribute().getLocaleKey();
		if (localeKey != null) {
			setText(componentFactory.getEngine().getLANG().getString(localeKey));
		}

		setOpaque(componentFactory.getStyle().isOpaque());

		Dimension preferredSize = new Dimension(
				(int) componentFactory.getBounds().getWidth(),
				(int) componentFactory.getBounds().getHeight()
		);
		setPreferredSize(preferredSize);

		String alignment = componentFactory.getComponentAttribute().getAlignment();
		if (alignment != null) {
			setHorizontalAlignment(LabelAlignment.fromString(alignment).getType());
		}

		String border = componentFactory.getComponentAttribute().getBorder();
		if (border != null) {
			setBorder(parseBorder(border));
		}

		if (componentFactory.getComponentAttribute().getGradient() != null) {
			// Проверяем корректность начальных и конечных цветов
			this.startColor = hexToColor(componentFactory.getComponentAttribute().getGradient().getStartColor());
			this.endColor = hexToColor(componentFactory.getComponentAttribute().getGradient().getEndColor());

			// Проверка на корректность цветов
			if (startColor == null || endColor == null) {
				startColor = Color.BLUE;
				endColor = Color.BLACK;
			}

			// Устанавливаем флаг градиента, если цвета различны
			this.isGradientText = !startColor.equals(endColor);

			// Определяем, вертикальный ли градиент
			this.isVerticalGradient = componentFactory.getComponentAttribute().getGradient().isVertical();
		}
	}

	private EmptyBorder parseBorder(String border) {
		List<Integer> borderValues = Arrays.stream(border.split(","))
				.map(Integer::parseInt)
				.toList();
		if (borderValues.size() != 4) {
			throw new IllegalArgumentException("Border must have exactly 4 values");
		}
		return new EmptyBorder(
				borderValues.get(0),
				borderValues.get(1),
				borderValues.get(2),
				borderValues.get(3)
		);
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);

		// Если текст должен быть с градиентом
		if (isGradientText) {
			Graphics2D g2d = (Graphics2D) g;
			Font font = getFont();
			g2d.setFont(font);
			g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

			FontRenderContext frc = g2d.getFontRenderContext();
			TextLayout layout = new TextLayout(getText(), font, frc);
			float textWidth = layout.getAdvance(); // Ширина текста
			float textHeight = layout.getAscent() + layout.getDescent(); // Высота текста

			// Градиент по горизонтали или вертикали в зависимости от флага
			GradientPaint gradient;
			if (isVerticalGradient) {
				gradient = new GradientPaint(0, 0, startColor, 0, textHeight, endColor);
			} else {
				gradient = new GradientPaint(0, 0, startColor, textWidth, 0, endColor);
			}

			g2d.setPaint(gradient);

			// Рисуем текст с градиентом, учитывая отступы
			float x = getInsets().left;
			float y = getInsets().top + layout.getAscent();
			layout.draw(g2d, x, y);
		} else {
			// Если нет градиента, рисуем обычный текст с одним цветом
			Graphics2D g2d = (Graphics2D) g;
			g2d.setColor(startColor);
			super.paintComponent(g);
		}
	}

	public void setStartColor(Color startColor) {
		this.startColor = startColor;
	}

	public void setEndColor(Color endColor) {
		this.endColor = endColor;
	}

	public void setGradientText(boolean gradientText) {
		isGradientText = gradientText;
	}

	public void setVerticalGradient(boolean verticalGradient) {
		isVerticalGradient = verticalGradient;
	}

	public boolean isGradientText() {
		return isGradientText;
	}

}
