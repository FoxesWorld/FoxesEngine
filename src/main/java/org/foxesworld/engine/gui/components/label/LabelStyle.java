package org.foxesworld.engine.gui.components.label;

import org.foxesworld.engine.gui.components.ComponentFactory;
import org.foxesworld.engine.gui.styles.StyleAttributes;

import java.awt.*;

import static org.foxesworld.engine.utils.FontUtils.hexToColor;

public class LabelStyle {
	private String fontName;
	private float fontSize;
	private Color idleColor;
	private Color activeColor;

	public LabelStyle(ComponentFactory componentFactory) {
		this(componentFactory.getStyle());
	}

	public LabelStyle(StyleAttributes style) {
		this.fontName = style.getFont();
		this.fontSize = style.getFontSize();
		this.idleColor = hexToColor(style.getColor());
		this.activeColor = hexToColor(style.getColor());
	}

	public void apply(Label label) {
		label.setFont(new Font(fontName, Font.PLAIN, Math.round(fontSize)));
		label.setForeground(idleColor);
	}

	public String getFontName() {
		return fontName;
	}

	public void setFontName(String fontName) {
		this.fontName = fontName;
	}

	public float getFontSize() {
		return fontSize;
	}

	public void setFontSize(float fontSize) {
		this.fontSize = fontSize;
	}

	public Color getIdleColor() {
		return idleColor;
	}

	public void setIdleColor(Color idleColor) {
		this.idleColor = idleColor;
	}

	public Color getActiveColor() {
		return activeColor;
	}

	public void setActiveColor(Color activeColor) {
		this.activeColor = activeColor;
	}
}
