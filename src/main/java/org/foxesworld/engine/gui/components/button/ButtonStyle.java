package org.foxesworld.engine.gui.components.button;

import org.foxesworld.engine.gui.components.Align;
import org.foxesworld.engine.gui.components.ComponentFactory;
import org.foxesworld.engine.utils.ImageUtils;

import java.awt.image.BufferedImage;

import javax.swing.SwingConstants;

import static org.foxesworld.engine.utils.FontUtils.hexToColor;


public class ButtonStyle {
	public boolean visible = true;
	public  int width;
	public int height;
	public String font;
	public String color;
	public float fontSize;
	public Align align;
	public BufferedImage texture;
	private ComponentFactory componentFactory;

	public ButtonStyle(ComponentFactory componentFactory) {
		this.componentFactory = componentFactory;
		this.width = componentFactory.style.width;
		this.height = componentFactory.style.height;
		this.color = componentFactory.style.color;
		this.font = componentFactory.style.font;
		this.fontSize = componentFactory.style.fontSize;
		this.align = Align.valueOf(componentFactory.style.align);
		this.texture = ImageUtils.getLocalImage(componentFactory.style.texture);
	}

	public void apply(Button button) {
		button.setVisible(visible);
		button.setHorizontalAlignment(align == Align.LEFT ? SwingConstants.LEFT : align == Align.CENTER ? SwingConstants.CENTER : SwingConstants.RIGHT);
		button.setFont(componentFactory.engine.getFontUtils().getFont(font, fontSize));
		button.setForeground(hexToColor(color));
		int i = texture.getHeight() / 4;
		button.defaultTX = texture.getSubimage(0, 0, texture.getWidth(), i);
		button.rolloverTX = texture.getSubimage(0, i, texture.getWidth(), i);
		button.pressedTX = texture.getSubimage(0, i * 2, texture.getWidth(), i);
		button.lockedTX = texture.getSubimage(0, i * 3, texture.getWidth(), i);
	}
}