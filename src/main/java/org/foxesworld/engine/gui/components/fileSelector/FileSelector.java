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

    private final JTextField filePathField;
    private final JButton browseButton;
    private final JFileChooser fileChooser;

    private final ComponentFactory componentFactory;

    public FileSelector(ComponentFactory componentFactory) {
        this.componentFactory = componentFactory;
        List<String> fileExtensions = componentFactory.getComponentAttribute().getFileExtensions();

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        setLayout(new BorderLayout());

        filePathField = new JTextField();
        filePathField.setEditable(false);

        browseButton = new JButton("Browse...");
        browseButton.addActionListener(new BrowseButtonListener());

        fileChooser = new JFileChooser();
        if (fileExtensions != null && !fileExtensions.isEmpty()) {
            String description = String.join(", ", fileExtensions) + " files";
            FileNameExtensionFilter filter = new FileNameExtensionFilter(description, fileExtensions.toArray(new String[0]));
            fileChooser.setFileFilter(filter);
        }

        add(filePathField, BorderLayout.CENTER);
        add(browseButton, BorderLayout.EAST);
    }

    public String getSelectedFilePath() {
        return filePathField.getText();
    }

    public void setSelectedFilePath(String path) {
        filePathField.setText(path);
    }

    private class BrowseButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            int returnValue = fileChooser.showOpenDialog(FileSelector.this);
            if (returnValue == JFileChooser.APPROVE_OPTION) {
                File selectedFile = fileChooser.getSelectedFile();
                filePathField.setText(selectedFile.getAbsolutePath());
            }
        }
    }

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
