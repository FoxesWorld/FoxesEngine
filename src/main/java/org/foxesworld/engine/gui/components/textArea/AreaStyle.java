package org.foxesworld.engine.gui.components.textArea;

import org.foxesworld.engine.gui.components.ComponentFactory;

import java.awt.*;

import static org.foxesworld.engine.utils.FontUtils.hexToColor;

public class AreaStyle {
	public String fontName;
	public float fontSize;
	public Color idleColor;
	public Color activeColor;

	public AreaStyle(ComponentFactory componentFactory) {
		this.fontName = componentFactory.getStyle().getFont();
		this.fontSize = componentFactory.getStyle().getFontSize();
		this.idleColor = hexToColor(componentFactory.getStyle().getColor());
		this.activeColor = hexToColor(componentFactory.getStyle().getColor());
	}

	public void apply(TextArea textArea) {
		textArea.setForeground(activeColor);
	}
}