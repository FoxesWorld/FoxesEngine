package org.foxesworld.engine.gui.components.passfield;

import org.foxesworld.engine.gui.components.ComponentFactory;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;

public class PassField extends JPasswordField {
    BufferedImage texture;
    private final ComponentFactory componentFactory;
    private final String placeholder;
    private boolean caretVisible = true;
    private int paddingX;
    private int paddingY;
    private Timer caretTimer;
    private boolean isPasswordVisible = false;
    private JLabel iconLabel;
    private Icon showIcon;
    private Icon hideIcon;

    public PassField(ComponentFactory componentFactory, String placeholder) {
        this.componentFactory = componentFactory;
        this.placeholder = placeholder;

        this.setOpaque(false);

        if (componentFactory.getComponentAttribute().isrevealButton()) {
            this.showIcon = componentFactory.getIconUtils().getVectorIcon(componentFactory.getComponentAttribute().getShowIcon(), 16, 16);
            this.hideIcon = componentFactory.getIconUtils().getVectorIcon(componentFactory.getComponentAttribute().getHideIcon(), 16, 16);

            // Initialize the icon label
            iconLabel = new JLabel(showIcon);
            iconLabel.setBorder(new EmptyBorder(0, 0, 0, 0));
            iconLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            iconLabel.addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    togglePasswordVisibility();
                }
            });


            JPanel iconPanel = new JPanel(new BorderLayout());
            iconPanel.setOpaque(false);
            iconPanel.add(iconLabel, BorderLayout.CENTER);

            setLayout(new BorderLayout());
            add(iconPanel, BorderLayout.EAST);
        }

        this.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                repaint();
                startCaretBlinking();
            }

            @Override
            public void focusLost(FocusEvent e) {
                repaint();
                stopCaretBlinking();
            }
        });
    }


    private void startCaretBlinking() {
        if (caretTimer == null || !caretTimer.isRunning()) {
            caretTimer = new Timer(500, new ActionListener() {
                private boolean caretVisibleState = true;

                @Override
                public void actionPerformed(ActionEvent e) {
                    caretVisible = caretVisibleState;
                    caretVisibleState = !caretVisibleState;
                    repaint();
                }
            });
            caretTimer.start();
        }
    }

    private void stopCaretBlinking() {
        if (caretTimer != null) {
            caretTimer.stop();
        }
        caretVisible = true;
        repaint();
    }

    private void togglePasswordVisibility() {
        isPasswordVisible = !isPasswordVisible;
        setEchoChar(isPasswordVisible ? (char) 0 : '*');
        iconLabel.setIcon(isPasswordVisible ? hideIcon : showIcon);
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.drawImage(this.componentFactory.getEngine().getImageUtils().genButton(getWidth(), getHeight(), texture), 0, 0, getWidth(), getHeight(), null);

        if (!hasFocus() && getPassword().length == 0 && placeholder != null) {
            g2.drawString(placeholder, getInsets().left + paddingX, g.getFontMetrics().getMaxAscent() + getInsets().top + paddingY);
        } else {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Draw the password characters
            char[] password = getPassword();
            String maskedPassword = new String(password).replaceAll("\\.", "*");
            int x = getInsets().left + paddingX;
            int y = g.getFontMetrics().getMaxAscent() + getInsets().top + paddingY;

            g2.drawString(isPasswordVisible ? new String(password) : maskedPassword, x, y);

            if (isFocusOwner() && caretVisible) {
                int caretX = x + g.getFontMetrics().stringWidth(isPasswordVisible ? new String(password).substring(0, getCaretPosition()) : maskedPassword.substring(0, getCaretPosition()));
                g2.drawLine(caretX, y - g.getFontMetrics().getAscent(), caretX, y + g.getFontMetrics().getDescent());
            }
        }
    }

    public void setPaddingX(int paddingX) {
        this.paddingX = paddingX;
    }

    public void setPaddingY(int paddingY) {
        this.paddingY = paddingY;
    }

    public void resetText() {
        setText("");
        repaint();
        revalidate();
    }
}