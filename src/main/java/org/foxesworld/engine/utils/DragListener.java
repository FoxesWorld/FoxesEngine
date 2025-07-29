package org.foxesworld.engine.utils;

import javax.swing.SwingUtilities;
import java.awt.Component;
import java.awt.Point;
import java.awt.Window;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * An improved listener for dragging a window (like a JFrame or JDialog).
 * This class encapsulates the dragging logic and state, providing a clean,
 * object-oriented way to make windows draggable by any inner component.
 *
 * @author FoxesWorld Team
 */
public class DragListener extends MouseAdapter {

    private final Window targetWindow;
    private Point startPoint;

    /**
     * Private constructor to be used by the static factory methods.
     * @param targetWindow The window (e.g., JFrame, JDialog) to be moved.
     */
    public DragListener(Window targetWindow) {
        this.targetWindow = targetWindow;
    }

    /**
     * Creates and attaches a drag listener to the specified component.
     * The listener will automatically find the parent window to move.
     *
     * @param component The component that will act as the "handle" for dragging (e.g., a JPanel).
     */
    public void apply(Component component) {
        Window window = SwingUtilities.getWindowAncestor(component);
        if (window != null) {
            component.addMouseListener(this);
            component.addMouseMotionListener(this);
        }
    }

    /**
     * Creates and attaches a drag listener, explicitly specifying the window to move.
     * This is useful if the component is in a complex hierarchy or if the window to be
     * moved is not the direct ancestor.
     *
     * @param component    The component that will act as the "handle" for dragging.
     * @param targetWindow The window to be moved.
     */
    public void apply(Component component, Window targetWindow) {
        if (targetWindow != null) {
            component.addMouseListener(this);
            component.addMouseMotionListener(this);
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {
        startPoint = e.getPoint();
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        startPoint = null;
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        if (startPoint == null) {
            return;
        }

        Point currentLocation = e.getLocationOnScreen();
        int newX = currentLocation.x - startPoint.x;
        int newY = currentLocation.y - startPoint.y;

        targetWindow.setLocation(newX, newY);
    }
}