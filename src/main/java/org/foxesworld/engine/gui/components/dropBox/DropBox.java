package org.foxesworld.engine.gui.components.dropBox;

import org.foxesworld.engine.gui.components.ComponentFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;

import static org.foxesworld.engine.utils.FontUtils.hexToColor;

@SuppressWarnings("unused")
public class DropBox extends JComponent implements MouseListener, MouseMotionListener {
    private Color color, hoverColor;
    private boolean loaded = false;
    private final ComponentFactory componentFactory;
    private DropBoxListener dropBoxListener;
    private String[] values;
    private final int initialY;
    private State state = State.CLOSED;
    private int selected = 0;
    private int hover = -1;
    private BufferedImage defaultTX;
    private BufferedImage openedTX;
    private BufferedImage rolloverTX;
    private BufferedImage selectedTX;
    private BufferedImage panelTX;
    private BufferedImage point;

    public DropBox(ComponentFactory componentFactory, String[] values, int initialY) {
        this.componentFactory = componentFactory;
        this.values = values;
        this.initialY = initialY;

        setupListeners();
    }

    public DropBox(ComponentFactory componentFactory, int initialY) {
        this.componentFactory = componentFactory;
        this.initialY = initialY;

        addMouseListener(this);
        addMouseMotionListener(this);
        setFocusable(true);
        addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                state = State.CLOSED;
                hover = selected;
                componentFactory.engine.getFrame().repaint();
                repaint();
            }
        });
    }

    private void setupListeners() {
        addMouseListener(this);
        addMouseMotionListener(this);
        setFocusable(true);

        addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                closeDropBox();
            }
        });
    }

    @Override
    protected void paintComponent(Graphics gmain) {
        Graphics2D g = (Graphics2D) gmain;
        int width = getWidth();
        g.setColor(hexToColor(componentFactory.style.getColor()));

        // Optimize state handling by using a method to handle the drawing
        switch (state) {
            case OPENED -> drawOpenedState(g, width);
            case ROLLOVER -> drawRolloverState(g, width);
            default -> drawDefaultState(g, width);
        }
        g.dispose();

        // Call listener once to notify about component creation
        if (!loaded) {
            dropBoxListener.onScrollBoxCreated(selected);
            loaded = true;
        }
    }

    private void drawOpenedState(Graphics2D g, int width) {
        int height = openedTX.getHeight();
        g.drawImage(this.componentFactory.engine.getImageUtils().genButton(width, height, openedTX), 0, getHeight() - height, width, height, null);
        int rightHeight = height * (values.length + 1);
        int rightY = initialY + height - rightHeight;

        // Move logic out of rendering methods to prevent repetitive checks
        updateComponentSizeAndLocation(rightY, rightHeight);

        for (int i = 0; i < values.length; i++) {
            drawPanel(g, i);
            if (i == selected) {
                g.drawImage(point, 205, panelTX.getHeight() * i + 10, this);
            }
        }
        g.drawString(values[selected], 10, height * (values.length + 1) - g.getFontMetrics().getHeight() / 2 - 5);
    }

    private void drawRolloverState(Graphics2D g, int width) {
        int height = rolloverTX.getHeight();
        updateComponentSizeAndLocation(initialY, height);

        g.drawImage(this.componentFactory.engine.getImageUtils().genButton(width, height, rolloverTX), 0, 0, width, height, null);
        g.setColor(hoverColor);
        g.drawString(values[selected], 10, height - g.getFontMetrics().getHeight() / 2 - 5);
    }

    private void drawDefaultState(Graphics2D g, int width) {
        int height = defaultTX.getHeight();
        updateComponentSizeAndLocation(initialY, height);

        g.drawImage(this.componentFactory.engine.getImageUtils().genButton(width, height, defaultTX), 0, 0, width, height, null);
        g.drawString(values[selected], 10, height - g.getFontMetrics().getHeight() / 2 - 5);
    }

    private void drawPanel(Graphics2D g, int index) {
        BufferedImage currentPanel = (hover == index) ? selectedTX : panelTX;
        g.drawImage(currentPanel, 0, panelTX.getHeight() * index, this);
        g.drawString(values[index], 5, selectedTX.getHeight() * (index + 1) - g.getFontMetrics().getHeight() / 2 - 5);
    }

    private void updateComponentSizeAndLocation(int y, int height) {
        if (getY() != y || getHeight() != height) {
            setLocation(getX(), y);
            setSize(getWidth(), height);
        }
    }

    private void closeDropBox() {
        state = State.CLOSED;
        hover = selected;
        componentFactory.engine.getFrame().repaint();
        dropBoxListener.onScrollBoxClose(selected);
        repaint();
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (e.getButton() != MouseEvent.BUTTON1) {
            return;
        }
        bringToFront();
        grabFocus();

        if (state == State.OPENED && (hover >= 0 && hover < values.length)) {
            selected = hover;
        }

        if (state == State.OPENED) {
            dropBoxListener.onScrollBoxOpen(selected);
            componentFactory.engine.getSOUND().playSound("dropBox", "dropBoxOpen");
        } else {
            dropBoxListener.onScrollBoxClose(selected);
            componentFactory.engine.getSOUND().playSound("dropBox", "dropBoxClose");
        }

        state = (state == State.OPENED) ? State.CLOSED : State.OPENED;
        repaint();
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        if (state != State.OPENED) {
            componentFactory.engine.getSOUND().playSound("button", "hover");
        }
        state = State.ROLLOVER;
        hover = -1;
        repaint();
    }

    @Override
    public void mouseExited(MouseEvent e) {
        closeDropBox();
    }

    @Override
    public void mousePressed(MouseEvent e) {
        grabFocus();
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        // No action needed, handled in mouseClicked
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        // Currently unused, can be removed if unnecessary
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        int newY = e.getY();
        int newHover = (state == State.OPENED) ? (newY / openedTX.getHeight()) : (newY / defaultTX.getHeight());

        // Only update hover if it's changed and within bounds
        if (newHover >= 0 && newHover < values.length && newHover != hover) {
            hover = newHover;
            if (state == State.OPENED) {
                dropBoxListener.onServerHover(newHover);
            }
            repaint();
        }
    }

    public int getSelectedIndex() {
        return selected;
    }

    public int getHoverIndex() {
        return hover;
    }

    public String getValue() {
        return (values.length > selected) ? values[selected] : values[0];
    }

    public void setSelectedIndex(int i) {
        if (i >= 0 && i < values.length) {
            selected = i;
            repaint();
        }
    }

    public void setValues(String[] values) {
        this.values = values;
        repaint();
    }

    public void bringToFront() {
        if (getParent() != null) {
            getParent().setComponentZOrder(this, 0);
        }
    }

    public boolean isOpened() {
        return state == State.OPENED;
    }

    public void setScrollBoxListener(DropBoxListener dropBoxListener) {
        this.dropBoxListener = dropBoxListener;
    }

    public void setLoaded(boolean loaded) {
        this.loaded = loaded;
    }

    public void setDefaultTX(BufferedImage defaultTX) {
        this.defaultTX = defaultTX;
    }

    public void setOpenedTX(BufferedImage openedTX) {
        this.openedTX = openedTX;
    }

    public void setRolloverTX(BufferedImage rolloverTX) {
        this.rolloverTX = rolloverTX;
    }

    public void setSelectedTX(BufferedImage selectedTX) {
        this.selectedTX = selectedTX;
    }

    public void setPanelTX(BufferedImage panelTX) {
        this.panelTX = panelTX;
    }

    public void setPoint(BufferedImage point) {
        this.point = point;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public void setHoverColor(Color hoverColor) {
        this.hoverColor = hoverColor;
    }

    public String[] getValues() {
        return values;
    }

    public BufferedImage getOpenedTX() {
        return openedTX;
    }

    public State getState() {
        return state;
    }
}