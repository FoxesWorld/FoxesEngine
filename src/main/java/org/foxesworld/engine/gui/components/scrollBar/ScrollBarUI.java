package org.foxesworld.engine.gui.components.scrollBar;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.plaf.basic.BasicScrollBarUI;
import org.foxesworld.engine.utils.ImageUtils;

public class ScrollBarUI extends BasicScrollBarUI {
    private Image thumbImage = ImageUtils.getLocalImage("assets/ui/scrollPane/thumb.png");
    private Image trackImage = ImageUtils.getLocalImage("assets/ui/scrollPane/track.png");

    public ScrollBarUI() {
    }

    @Override
    protected JButton createDecreaseButton(int orientation) {
        JButton button = new JButton();
        button.setPreferredSize(new Dimension(0, 0));
        button.setMinimumSize(new Dimension(0, 0));
        button.setMaximumSize(new Dimension(0, 0));
        return button;
    }

    @Override
    protected JButton createIncreaseButton(int orientation) {
        JButton button = new JButton();
        button.setPreferredSize(new Dimension(0, 0));
        button.setMinimumSize(new Dimension(0, 0));
        button.setMaximumSize(new Dimension(0, 0));
        return button;
    }

    @Override
    protected void paintThumb(Graphics g, JComponent c, Rectangle thumbBounds) {
        if (this.thumbImage != null) {
            g.translate(thumbBounds.x, thumbBounds.y);
            AffineTransform transform = AffineTransform.getScaleInstance(
                    (double)thumbBounds.width / (double)this.thumbImage.getWidth(null),
                    (double)thumbBounds.height / (double)this.thumbImage.getHeight(null)
            );
            ((Graphics2D)g).drawImage(this.thumbImage, transform, null);
            g.translate(-thumbBounds.x, -thumbBounds.y);
        }
    }

    @Override
    protected void paintTrack(Graphics g, JComponent c, Rectangle trackBounds) {
        if (this.trackImage != null) {
            g.translate(trackBounds.x, trackBounds.y);
            AffineTransform transform = AffineTransform.getScaleInstance(
                    (double)trackBounds.width / (double)this.trackImage.getWidth(null),
                    (double)trackBounds.height / (double)this.trackImage.getHeight(null)
            );
            ((Graphics2D)g).drawImage(this.trackImage, transform, null);
            g.translate(-trackBounds.x, -trackBounds.y);
        }
    }

    @Override
    protected void setThumbBounds(int x, int y, int width, int height) {
        super.setThumbBounds(x, y, width, height);
        this.scrollbar.repaint();
    }
}
