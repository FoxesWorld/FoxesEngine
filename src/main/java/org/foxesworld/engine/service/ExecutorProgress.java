package org.foxesworld.engine.service;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ExecutorProgress {
    private static final ConcurrentHashMap<String, TaskProgress> progressMap = new ConcurrentHashMap<>();
    private JFrame statusFrame;
    private final DefaultTableModel tableModel = new DefaultTableModel(new Object[]{"Task ID", "Task Name", "Progress", "Memory Usage", "Status"}, 0);
    private final JTable taskTable = new JTable(tableModel);
    private final JLabel totalTasksLabel = new JLabel();
    private final JLabel totalMemoryLabel = new JLabel();
    private final JLabel systemMemoryLabel = new JLabel();
    private static final long UPDATE_INTERVAL = 100;
    private static boolean isInitialized = false;

    // Constructor
    public ExecutorProgress() {
    }

    // Method to show the task manager frame
    public void showTaskMgr() {
        if (!isInitialized) {
            initializeFrame();
            isInitialized = true;
        }
        startUpdating();
    }

    // Method to generate a unique task ID
    public String generateTaskId() {
        return UUID.randomUUID().toString();
    }

    // Method to add a new task
    public void addTask(String taskId, String taskName) {
        progressMap.put(taskId, new TaskProgress(taskName));
        tableModel.addRow(new Object[]{taskId, taskName, 0, "0 bytes", "Not Started"});
        updateStatistics();
    }

    // Method to update the progress of a task
    public void updateTask(String taskId, int progress) {
        TaskProgress taskProgress = progressMap.get(taskId);
        if (taskProgress != null) {
            taskProgress.setProgress(progress);
            updateTaskInTable(taskId);
        }
    }

    // Method to update the memory usage of a task
    public void updateTaskMemoryUsage(String taskId, long memoryUsage) {
        TaskProgress taskProgress = progressMap.get(taskId);
        if (taskProgress != null) {
            taskProgress.setMemoryUsage(memoryUsage);
            updateTaskInTable(taskId);
        }
    }

    // Method to remove a task
    public void removeTask(String taskId) {
        Timer removalTimer = new Timer();
        removalTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                SwingUtilities.invokeLater(() -> {
                    TaskProgress taskProgress = progressMap.get(taskId);
                    if (taskProgress != null) {
                        taskProgress.complete();
                        progressMap.remove(taskId);
                        for (int i = 0; i < tableModel.getRowCount(); i++) {
                            if (tableModel.getValueAt(i, 0).equals(taskId)) {
                                tableModel.removeRow(i);
                                break;
                            }
                        }
                        updateStatistics();
                    }
                });
            }
        }, 500);
        updateStatistics();
    }

    // Method to start updating statistics at fixed intervals
    private void startUpdating() {
        Timer updateTimer = new Timer();
        updateTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                updateStatistics();
            }
        }, 0, UPDATE_INTERVAL);
    }

    // Method to update statistics such as total tasks and memory usage
    private void updateStatistics() {
        SwingUtilities.invokeLater(() -> {
            int activeTasks = progressMap.size();
            long totalMemoryUsage = progressMap.values().stream().mapToLong(TaskProgress::getMemoryUsage).sum();
            totalTasksLabel.setText("Active Tasks: " + activeTasks);
            totalMemoryLabel.setText("Total Memory Usage: " + formatMemory(totalMemoryUsage));
            systemMemoryLabel.setText("System Memory: " + getMemoryStats());
        });
    }

    // Method to update a specific task in the table
    private void updateTaskInTable(String taskId) {
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            if (tableModel.getValueAt(i, 0).equals(taskId)) {
                TaskProgress taskProgress = progressMap.get(taskId);
                if (taskProgress != null) {
                    tableModel.setValueAt(taskProgress.getProgress(), i, 2);
                    tableModel.setValueAt(formatMemory(taskProgress.getMemoryUsage()), i, 3);
                    tableModel.setValueAt(getTaskStatus(taskProgress), i, 4);
                }
                break;
            }
        }
    }

    // Method to initialize the task manager frame
    private void initializeFrame() {
        statusFrame = new JFrame("Task Manager");
        statusFrame.setLayout(new BorderLayout());
        statusFrame.setSize(800, 600);
        statusFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Center the frame on the screen
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int x = (screenSize.width - statusFrame.getWidth()) / 2;
        int y = (screenSize.height - statusFrame.getHeight()) / 2;
        statusFrame.setLocation(x, y);

        // Configure the task table
        taskTable.setFillsViewportHeight(true);
        taskTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        taskTable.setFont(new Font("Monospaced", Font.PLAIN, 12));
        taskTable.setRowHeight(25);
        taskTable.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 14));
        taskTable.getTableHeader().setBorder(new EmptyBorder(5, 5, 5, 5));
        JScrollPane tableScrollPane = new JScrollPane(taskTable);

        // Configure the statistics panel
        JPanel statisticsPanel = new JPanel(new GridLayout(3, 1));
        statisticsPanel.setBorder(BorderFactory.createTitledBorder("Statistics"));
        statisticsPanel.add(totalTasksLabel);
        statisticsPanel.add(totalMemoryLabel);
        statisticsPanel.add(systemMemoryLabel);

        // Configure the terminate button
        JButton terminateButton = new JButton("Terminate Task");
        terminateButton.addActionListener(e -> {
            int selectedRow = taskTable.getSelectedRow();
            if (selectedRow != -1) {
                String taskId = (String) tableModel.getValueAt(selectedRow, 0);
                removeTask(taskId);
            }
        });

        // Main panel layout
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(tableScrollPane, BorderLayout.CENTER);
        mainPanel.add(statisticsPanel, BorderLayout.NORTH);
        mainPanel.add(terminateButton, BorderLayout.SOUTH);

        // Add main panel to the frame
        statusFrame.add(mainPanel, BorderLayout.CENTER);
        statusFrame.setVisible(true);
    }

    // Method to get the status of a task based on its progress
    private String getTaskStatus(TaskProgress taskProgress) {
        int progress = taskProgress.getProgress();
        if (progress == 100) {
            return "Complete";
        } else if (progress > 50) {
            return "In Progress";
        } else {
            return "Not Started";
        }
    }

    // Method to format memory in a readable format
    private String formatMemory(long memory) {
        if (memory < 1024) {
            return memory + " bytes";
        } else if (memory < 1024 * 1024) {
            return (memory / 1024) + " KB";
        } else if (memory < 1024 * 1024 * 1024) {
            return (memory / (1024 * 1024)) + " MB";
        } else {
            return (memory / (1024 * 1024 * 1024)) + " GB";
        }
    }

    // Method to get system memory statistics
    private String getMemoryStats() {
        MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
        MemoryUsage heapMemoryUsage = memoryMXBean.getHeapMemoryUsage();
        long maxMemory = heapMemoryUsage.getMax();
        long usedMemory = heapMemoryUsage.getUsed();
        long freeMemory = maxMemory - usedMemory;

        return String.format("Used: %s | Free: %s | Total: %s",
                formatMemory(usedMemory),
                formatMemory(freeMemory),
                formatMemory(maxMemory));
    }

    // Method to get the status frame
    public JFrame getStatusFrame() {
        return statusFrame;
    }
}
