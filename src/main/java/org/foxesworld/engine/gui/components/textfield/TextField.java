package org.foxesworld.engine.gui.components.textfield;

import org.foxesworld.engine.gui.components.ComponentAttributes;
import org.foxesworld.engine.gui.components.ComponentFactory;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.Serial;

import static org.foxesworld.engine.utils.FontUtils.hexToColor;

public class TextField extends JTextField {
	@Serial
	private static final long serialVersionUID = 1L;
	private TextFieldListener textFieldListener;
	private int carretDelay = 500;
	public BufferedImage texture;
	private int paddingX = 0;
	private int paddingY = 0;
	private boolean caretVisible = true;
	private boolean selected = false;
	private Color selectionColor;
	private Color selectedTextColor = Color.white;
	private Timer caretTimer;
	private String placeholder;

	public TextField(ComponentFactory componentFactory) {
		this.placeholder = componentFactory.getLANG().getString(componentFactory.getComponentAttribute().getLocaleKey());
		this.selectionColor = hexToColor(componentFactory.style.getSelectionColor());
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

		addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				super.mousePressed(e);
				selected = false;
				setCaretPosition(viewToModel2D(e.getPoint()));
			}
		});

		addMouseMotionListener(new MouseMotionAdapter() {
			@Override
			public void mouseDragged(MouseEvent e) {
				super.mouseDragged(e);
				selected = true;
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
		g.drawImage(texture, 0, 0, getWidth(), getHeight(), null);

		// Draw the text with padding
		g.setColor(getForeground());
		int x = paddingX;
		int y = paddingY + g.getFontMetrics().getAscent();

		String text = getText();
		if (text != null) { // Добавленная проверка на null
			g.drawString(text, x, y);
		}

		// Draw the caret only when visible and the text field has focus
		if (isFocusOwner() && caretVisible) {
			try {
				int caretX = x + g.getFontMetrics().stringWidth(getText().substring(0, getCaretPosition()));
				int caretY = y - g.getFontMetrics().getAscent();
				g.drawLine(caretX, caretY, caretX, caretY + g.getFontMetrics().getHeight());
			} catch (StringIndexOutOfBoundsException ignored) {}
		}

		// Draw selection highlight
		if (selected) {
			int start = Math.min(getSelectionStart(), getSelectionEnd());
			int end = Math.max(getSelectionStart(), getSelectionEnd());
			g.setColor(selectionColor);

			// Добавленная проверка на null
			String selectedText = getSelectedText();
			if (selectedText != null) {
				int selStart = x + g.getFontMetrics().stringWidth(getText().substring(0, start));
				int selEnd = x + g.getFontMetrics().stringWidth(getText().substring(0, end));
				g.fillRect(selStart, y - g.getFontMetrics().getAscent(), selEnd - selStart, g.getFontMetrics().getHeight());
				g.setColor(selectedTextColor);
				g.drawString(selectedText, selStart, y);
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

	// Метод для установки цвета выделения
	public void setSelectionColor(Color selectionColor) {
		this.selectionColor = selectionColor;
	}

	// Метод для установки цвета текста при выделении
	public void setSelectedTextColor(Color selectedTextColor) {
		this.selectedTextColor = selectedTextColor;
	}

	// Метод для получения цвета выделения
	public Color getSelectionColor() {
		return selectionColor;
	}

	public String getValue(){
		return  this.getText();
	}

	// Метод для получения цвета текста при выделении
	public Color getSelectedTextColor() {
		return selectedTextColor;
	}
}
