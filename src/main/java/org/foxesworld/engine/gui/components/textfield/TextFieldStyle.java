package org.foxesworld.engine.gui.components.textfield;

import org.foxesworld.engine.gui.components.ComponentFactory;
import org.foxesworld.engine.gui.styles.StyleAttributes;
import org.foxesworld.engine.utils.ImageUtils;

import javax.swing.border.BevelBorder;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import static org.foxesworld.engine.utils.FontUtils.hexToColor;

public class TextFieldStyle {
	public Color foregroundColor, backgroundColor, caretColor;
	private final List<Color> borderColor = new ArrayList();
	public int width,height, bevel;
	public String font;
	public float fontSize;
	public BufferedImage texture;
	private ComponentFactory componentFactory;

	public TextFieldStyle(ComponentFactory componentFactory) {
		this.componentFactory = componentFactory;
		this.foregroundColor = hexToColor(componentFactory.style.getColor());
		this.backgroundColor = hexToColor(componentFactory.style.getBackground());
		this.setBorder(componentFactory.style);
		this.caretColor = hexToColor(componentFactory.style.getCaretColor());
		this.width = componentFactory.style.getWidth();
		this.height = componentFactory.style.getHeight();
		this.font = componentFactory.style.getFont();
		this.fontSize = componentFactory.style.getFontSize();
		this.texture = ImageUtils.getLocalImage(componentFactory.style.getTexture());
	}

	private void setBorder(StyleAttributes styleAttributes){
		if(styleAttributes.getBorderColor() != null) {
			this.borderColor.add(hexToColor(styleAttributes.getBorderColor().split(",")[1]));
			this.borderColor.add(hexToColor(styleAttributes.getBorderColor().split(",")[2]));
			this.bevel = Integer.parseInt(styleAttributes.getBorderColor().split(",")[0]);
		}
	}

	public void apply(TextField text) {
		text.texture = texture;
		text.setPaddingX(componentFactory.style.getPaddingX());
		text.setPaddingY(componentFactory.style.getPaddingY());
		text.setCaretColor(caretColor);
		text.setBackground(backgroundColor);
		text.setForeground(foregroundColor);
		if(this.componentFactory.style.getBorderColor() != null) {
			text.setBorder(new BevelBorder(this.bevel, borderColor.get(0), borderColor.get(1)));
		} else {
			text.setBorder(null);
		}
		text.setFont(componentFactory.engine.getFONTUTILS().getFont(font, fontSize));
	}
}