package org.foxesworld.engine.gui.components.textfield;

import org.foxesworld.engine.utils.ImageUtils;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.Serial;

public class TextField extends JTextField {
	@Serial
	private static final long serialVersionUID = 1L;
	private  TextFieldListener textFieldListener;
	private int carretDelay = 500;
	public BufferedImage texture;
	private int paddingX = 0;
	private int paddingY = 0;
	private boolean caretVisible = true;
	private Timer caretTimer;
	private String placeholder;

	public TextField(String placeholder) {
		this.placeholder = placeholder;
		setOpaque(false);
		setText(this.placeholder);

		addFocusListener(new FocusAdapter() {
			@Override
			public void focusGained(FocusEvent e) {
				if (getText().equals(placeholder)) {
					setText("");
					repaint();
					revalidate();
				}
				startCaretBlinking();
			}

			@Override
			public void focusLost(FocusEvent e) {
				if (getText().isEmpty()) {
					setText(placeholder);
				}
				stopCaretBlinking();
			}
		});

		addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				startCaretBlinking();
			}
		});
	}

	private void startCaretBlinking() {
		if (caretTimer == null || !caretTimer.isRunning()) {
			caretTimer = new Timer(carretDelay, new ActionListener() {
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

	@Override
	protected void paintComponent(Graphics maing) {
		Graphics2D g = (Graphics2D) maing.create();
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		// Draw the texture
		g.drawImage(ImageUtils.genButton(getWidth(), getHeight(), texture), 0, 0, getWidth(), getHeight(), null);

		// Draw the text with padding
		g.setColor(getForeground());
		int x = paddingX;
		int y = paddingY + g.getFontMetrics().getAscent();
		g.drawString(getText(), x, y);

		// Draw the caret only when visible and the text field has focus
		if (isFocusOwner() && caretVisible) {
			try {
				int caretX = x + g.getFontMetrics().stringWidth(getText().substring(0, getCaretPosition()));
				int caretY = y - g.getFontMetrics().getAscent();
				g.drawLine(caretX, caretY, caretX, caretY + g.getFontMetrics().getHeight());
			} catch (StringIndexOutOfBoundsException ignored) {
				// Ignore the exception that may occur if getCaretPosition() is greater than the text length
			}
		}

		g.dispose();
	}

	public void setTextFieldListener(TextFieldListener textFieldListener) {
		this.textFieldListener = textFieldListener;
		getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void insertUpdate(DocumentEvent e) {
				checkText();
			}

			@Override
			public void removeUpdate(DocumentEvent e) {
				checkText();
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
				checkText();
			}

			private void checkText() {
				if (!getText().equals(placeholder)) {
					textFieldListener.onTextChange(TextField.this);
				}
			}
		});
	}

	public void setPaddingX(int paddingX) {
		this.paddingX = paddingX;
	}

	public void setPaddingY(int paddingY) {
		this.paddingY = paddingY;
	}
}
