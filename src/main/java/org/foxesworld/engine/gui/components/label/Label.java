package org.foxesworld.engine.gui.components.label;

import org.foxesworld.engine.gui.components.ComponentFactory;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.Serial;



public class Label extends JLabel {
	@Serial
	private static final long serialVersionUID = 1L;

	public Label(ComponentFactory componentFactory) {
		if(componentFactory.getComponentAttribute().getLocaleKey() != null) {
			setText(componentFactory.getLANG().getString(componentFactory.getComponentAttribute().getLocaleKey()));
		}
		setOpaque(componentFactory.style.isOpaque());
		setPreferredSize(new Dimension(componentFactory.getBounds().getWidth(), componentFactory.getBounds().getHeight()));
		if(componentFactory.getComponentAttribute().getAlignment() != null) {
			setHorizontalAlignment(LabelAlignment.fromString(componentFactory.getComponentAttribute().getAlignment()).getType());
		}
		if(componentFactory.getComponentAttribute().getBorder() != null) {
			List borders = new List();
			for (String val : componentFactory.getComponentAttribute().getBorder().split(",")) {
				borders.add(val);
			}
			setBorder(new EmptyBorder(
					Integer.parseInt(borders.getItem(0)),
					Integer.parseInt(borders.getItem(1)),
					Integer.parseInt(borders.getItem(2)),
					Integer.parseInt(borders.getItem(3))));
		}
	}

	public void paintComponent(Graphics g) {
		super.paintComponent(g);
	}
}