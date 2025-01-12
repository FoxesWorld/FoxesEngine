package org.foxesworld.engine.gui.components.fileSelector;

import org.foxesworld.engine.Engine;
import org.foxesworld.engine.gui.components.ComponentFactory;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;

public class FileSelector extends JComponent {

    public enum SelectionMode {
        FILES_ONLY,
        DIRECTORIES_ONLY
    }

    private final ComponentFactory componentFactory;
    private final JTextField filePathField;
    private final JButton browseButton;
    private JFileChooser fileChooser;

    public FileSelector(ComponentFactory componentFactory, SelectionMode selectionMode) {
        this.componentFactory = componentFactory;
        List<String> fileExtensions = componentFactory.getComponentAttribute().getFileExtensions();

        setLayout(new BorderLayout());
        filePathField = createStyledTextFieldWithTexture();
        filePathField.setEditable(false);
        browseButton = new JButton("...");
        browseButton.addActionListener(new BrowseButtonListener());

        fileChooser = new JFileChooser();
        configureFileChooser(fileExtensions, selectionMode);

        add(filePathField, BorderLayout.CENTER);
        add(browseButton, BorderLayout.EAST);
    }

    /**
     * Создает JTextField с текстурой фона.
     */
    private JTextField createStyledTextFieldWithTexture() {
        BufferedImage image = componentFactory.getEngine().getImageUtils().getLocalImage(componentFactory.getComponentAttribute().getBackground());
        return new JTextField() {
            private final Image backgroundImage = image;

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;

                // Рисуем текстуру
                if (backgroundImage != null) {
                    g2d.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
                }

                // Рисуем текст поверх текстуры
                g2d.setComposite(AlphaComposite.SrcOver);
                g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                g2d.setColor(getForeground());
                g2d.setFont(getFont());
                FontMetrics fm = g2d.getFontMetrics();
                int textX = getInsets().left;
                int textY = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2d.drawString(getText(), textX, textY);
            }

            @Override
            protected void paintBorder(Graphics g) {
                super.paintBorder(g);
            }

            @Override
            public void setOpaque(boolean isOpaque) {
                super.setOpaque(false);
            }


            @Override
            public void setFocusable(boolean isFocusable) {
                super.setFocusable(false);
            }

            @Override
            public void setEditable(boolean isEditable) {
                super.setEditable(false);
            }

            @Override
            public void setSelectionColor(Color c) {
                super.setSelectionColor(getBackground());
            }

            @Override
            public void setSelectionStart(int selectionStart) {
            }

            @Override
            public void setSelectionEnd(int selectionEnd) {
            }

            @Override
            public void select(int selectionStart, int selectionEnd) {
            }

        };
    }


    /**
     * Возвращает выбранный путь к файлу или директории.
     */
    public String getValue() {
        return filePathField.getText();
    }

    public void setValue(String path) {
        File file = new File(path);

        // Проверка существования пути
        if (!file.exists()) {
            try {
                if (fileChooser.getFileSelectionMode() == JFileChooser.DIRECTORIES_ONLY) {
                    // Создание директории
                    if (file.mkdirs()) {
                        Engine.LOGGER.info("Created missing directory: " + path);
                    } else {
                        throw new RuntimeException("Failed to create directory: " + path);
                    }
                } else if (fileChooser.getFileSelectionMode() == JFileChooser.FILES_ONLY) {
                    if (file.getParentFile() != null && file.getParentFile().mkdirs()) {
                        Engine.LOGGER.info("Created parent directories for file: " + path);
                    }
                    if (file.createNewFile()) {
                        Engine.LOGGER.info("Created missing file: " + path);
                    } else {
                        throw new RuntimeException("Failed to create file: " + path);
                    }
                }
            } catch (Exception e) {
                throw new RuntimeException("Error while creating path: " + path, e);
            }
        }

        // Проверка соответствия режиму
        if ((file.isFile() && fileChooser.getFileSelectionMode() == JFileChooser.FILES_ONLY) ||
                (file.isDirectory() && fileChooser.getFileSelectionMode() == JFileChooser.DIRECTORIES_ONLY)) {
            filePathField.setText(path);
        } else {
            throw new IllegalArgumentException("The provided path does not match the current selection mode.");
        }
    }


    /**
     * Настраивает JFileChooser для работы с файлами или директориями.
     */
    private void configureFileChooser(List<String> fileExtensions, SelectionMode selectionMode) {
        if (selectionMode == SelectionMode.DIRECTORIES_ONLY) {
            fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        } else {
            fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            if (fileExtensions != null && !fileExtensions.isEmpty()) {
                String description = String.join(", ", fileExtensions) + " files";
                FileNameExtensionFilter filter = new FileNameExtensionFilter(description, fileExtensions.toArray(new String[0]));
                fileChooser.setFileFilter(filter);
            }
        }
    }

    /**
     * Обработчик кнопки "Browse".
     */
    private class BrowseButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String currentPath = filePathField.getText();
            if (!currentPath.isEmpty()) {
                File currentFile = new File(currentPath);
                if (currentFile.exists()) {
                    if (currentFile.isFile()) {
                        fileChooser.setSelectedFile(currentFile);
                    } else {
                        fileChooser.setCurrentDirectory(currentFile);
                    }
                }
            }

            int returnValue = fileChooser.showOpenDialog(FileSelector.this);
            if (returnValue == JFileChooser.APPROVE_OPTION) {
                File selectedFile = fileChooser.getSelectedFile();
                filePathField.setText(selectedFile.getAbsolutePath());
            }
        }
    }

    //public void setChosen(String file) {
    //    this.fileChooser.setSelectedFile(new File(file));
    //}

    /**
     * Геттеры для дочерних компонентов.
     */
    public JTextField getFilePathField() {
        return filePathField;
    }

    public JButton getBrowseButton() {
        return browseButton;
    }

    public JFileChooser getFileChooser() {
        return fileChooser;
    }
}
