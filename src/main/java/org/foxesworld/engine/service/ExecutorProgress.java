package org.foxesworld.engine.service;

import javax.swing.*;
import java.awt.*;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Timer;
import java.util.TimerTask;

public class ExecutorProgress {
    private static final ConcurrentHashMap<String, TaskProgress> progressMap = new ConcurrentHashMap<>();
    private JFrame statusFrame;
    //private JProgressBar globalProgressBar;
    private final JTextArea taskStatisticsArea = new JTextArea();

    private static final long UPDATE_INTERVAL = 100;
    private static boolean isInitialized = false;

    public ExecutorProgress() {
        if (!isInitialized) {
            initializeFrame();
            isInitialized = true;
        }
        startUpdating();
    }

    public String generateTaskId() {
        return UUID.randomUUID().toString();
    }

    public void addTask(String taskId, String taskName) {
        progressMap.put(taskId, new TaskProgress(taskName));
        updateStatistics();
    }

    public void updateTask(String taskId, int progress) {
        TaskProgress taskProgress = progressMap.get(taskId);
        if (taskProgress != null) {
            taskProgress.setProgress(progress);
        }
    }

    public void updateTaskMemoryUsage(String taskId, long memoryUsage) {
        TaskProgress taskProgress = progressMap.get(taskId);
        if (taskProgress != null) {
            taskProgress.setMemoryUsage(memoryUsage);
        }
    }

    public void removeTask(String taskId) {
        Timer removalTimer = new Timer();
        removalTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                progressMap.remove(taskId);
                updateStatistics(); // Update task statistics after removal
            }
        }, 500); // Delay for 500ms
        updateStatistics();
    }


    private void startUpdating() {
        Timer updateTimer = new Timer();
        updateTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                updateStatistics();
            }
        }, 0, UPDATE_INTERVAL);
    }

    private void updateStatistics() {
        SwingUtilities.invokeLater(() -> {
            String statistics = getFormattedTaskStatistics();
            taskStatisticsArea.setText(statistics);

            //int totalProgress = calculateTotalProgress();
            //globalProgressBar.setValue(totalProgress);
        });
    }

    private void initializeFrame() {
        statusFrame = new JFrame("Task Progress");
        statusFrame.setLayout(new BorderLayout());
        statusFrame.setSize(650, 350);
        statusFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int x = (screenSize.width - statusFrame.getWidth()) / 2;
        int y = (screenSize.height - statusFrame.getHeight()) / 2;
        statusFrame.setLocation(x, y);

        //globalProgressBar = new JProgressBar(0, 100);
        //globalProgressBar.setStringPainted(true);
        //globalProgressBar.setForeground(new Color(76, 175, 80));

        taskStatisticsArea.setEditable(false);
        taskStatisticsArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        taskStatisticsArea.setBackground(new Color(245, 245, 245));
        taskStatisticsArea.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel statsPanel = new JPanel(new BorderLayout());
        statsPanel.setBorder(BorderFactory.createTitledBorder("Task Statistics"));
        statsPanel.add(new JScrollPane(taskStatisticsArea), BorderLayout.CENTER);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        //mainPanel.add(globalProgressBar);
        mainPanel.add(statsPanel);

        statusFrame.add(mainPanel, BorderLayout.CENTER);
        statusFrame.setVisible(false);
    }

    public String getFormattedTaskStatistics() {
        StringBuilder statistics = new StringBuilder();

        statistics.append("Task Progress Report:\n");
        statistics.append("--------------------------------------\n");
        statistics.append(String.format("%-25s %-10s %-15s %-15s\n", "Task", "Progress", "Memory Usage", "Status"));
        statistics.append("--------------------------------------\n");

        int activeTasks = 0;
        long totalMemoryUsage = 0;

        for (TaskProgress taskProgress : progressMap.values()) {
            statistics.append(String.format("%-25s %-10d %-15s %-15s\n", taskProgress.getName(),
                    taskProgress.getProgress(),
                    formatMemory(taskProgress.getMemoryUsage()),
                    getTaskStatus(taskProgress)));
            activeTasks++;
            totalMemoryUsage += taskProgress.getMemoryUsage();
        }

        statistics.append("--------------------------------------\n");
        statistics.append(String.format("Active Tasks: %d\n", activeTasks));
        statistics.append(String.format("Total Memory Usage: %s\n", formatMemory(totalMemoryUsage)));
        statistics.append("System Memory: " + getMemoryStats() + "\n");

        return statistics.toString();
    }

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

    private int calculateTotalProgress() {
        if (progressMap.isEmpty()) {
            return 0;
        }

        int totalProgress = 0;
        for (TaskProgress taskProgress : progressMap.values()) {
            totalProgress += taskProgress.getProgress();
        }

        return totalProgress / progressMap.size();
    }

    public JFrame getStatusFrame() {
        return statusFrame;
    }
}
