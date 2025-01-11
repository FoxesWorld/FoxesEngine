package org.foxesworld.engine.gui.components.fileSelector;

import org.foxesworld.engine.gui.components.ComponentFactory;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.List;

public class FileSelector extends JComponent {

    public enum SelectionMode {
        FILES_ONLY,
        DIRECTORIES_ONLY
    }

    private final JTextField filePathField;
    private final JButton browseButton;
    private JFileChooser fileChooser;

    public FileSelector(ComponentFactory componentFactory, SelectionMode selectionMode) {
        List<String> fileExtensions = componentFactory.getComponentAttribute().getFileExtensions();

        setLayout(new BorderLayout());
        filePathField = new JTextField();
        filePathField.setEditable(false);
        browseButton = new JButton("...");
        browseButton.addActionListener(new BrowseButtonListener());

        fileChooser = new JFileChooser();
        configureFileChooser(fileExtensions, selectionMode);

        add(filePathField, BorderLayout.CENTER);
        add(browseButton, BorderLayout.EAST);
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
                        System.out.println("Created missing directory: " + path);
                    } else {
                        throw new RuntimeException("Failed to create directory: " + path);
                    }
                } else if (fileChooser.getFileSelectionMode() == JFileChooser.FILES_ONLY) {
                    if (file.getParentFile() != null && file.getParentFile().mkdirs()) {
                        System.out.println("Created parent directories for file: " + path);
                    }
                    if (file.createNewFile()) {
                        System.out.println("Created missing file: " + path);
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
