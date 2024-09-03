package org.foxesworld.engine.gui.components.sprite;

import org.foxesworld.engine.gui.attributes.ComponentAttributes;
import org.foxesworld.engine.utils.ImageUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

import static org.foxesworld.engine.utils.FontUtils.hexToColor;

public class SpriteAnimation extends JComponent {

    private final BufferedImage spriteSheet;
    private final int rows, columns;
    private int currentFrame = 0;
    private final Rectangle spriteRect;

    public SpriteAnimation(ComponentAttributes componentAttributes) {
        this.spriteSheet = ImageUtils.getLocalImage(componentAttributes.imageIcon);
        this.rows = componentAttributes.rowNum;
        this.columns = componentAttributes.colNum;
        this.spriteRect = new Rectangle(30, 30, 64, 64);

        Timer timer = new Timer(componentAttributes.delay, e -> {
            currentFrame = (currentFrame + 1) % (rows * columns);
            repaint();
        });
        timer.start();
    }

    public Rectangle getSpriteRect() {
        return spriteRect;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        int scaledWidth = getWidth();
        int scaledHeight = getHeight();

        int frameWidth = spriteSheet.getWidth() / columns;
        int frameHeight = spriteSheet.getHeight() / rows;

        int row = currentFrame / columns;
        int column = currentFrame % columns;

        g.drawImage(
                spriteSheet.getSubimage(column * frameWidth, row * frameHeight, frameWidth, frameHeight),
                0,
                0,
                scaledWidth,
                scaledHeight,
                this
        );
    }
}
