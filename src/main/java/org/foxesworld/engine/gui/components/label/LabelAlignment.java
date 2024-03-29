package org.foxesworld.engine.gui.components.label;

import javax.swing.SwingConstants;
public enum LabelAlignment {
    LEFT(SwingConstants.LEFT),
    CENTER(SwingConstants.CENTER),
    RIGHT(SwingConstants.RIGHT);

    private final int alignment;

    LabelAlignment(int alignment) {
        this.alignment = alignment;
    }

    public int getType() {
        return alignment;
    }

    public static LabelAlignment fromString(String text) {
        switch (text.toUpperCase()) {
            case "LEFT":
                return LEFT;
            case "CENTER":
                return CENTER;
            case "RIGHT":
                return RIGHT;
            default:
                throw new IllegalArgumentException("Invalid alignment value: " + text);
        }
    }
}
