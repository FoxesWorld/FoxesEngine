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
    private JProgressBar memoryProgressBar; // Прогресс-бар для загрузки памяти
    private static final long UPDATE_INTERVAL = 100;
    private static boolean isInitialized = false;

    // Constructor
    public ExecutorProgress() {
    }

    public void showTaskMgr() {
        if (!isInitialized) {
            initializeFrame();
            isInitialized = true;
        }
        startUpdating();
    }

    // Метод для генерации уникального идентификатора задачи
    public String generateTaskId() {
        return UUID.randomUUID().toString();
    }

    // Метод для добавления новой задачи
    public void addTask(String taskId, String taskName) {
        progressMap.put(taskId, new TaskProgress(taskName));
        tableModel.addRow(new Object[]{taskId, taskName, 0, "0 bytes", "Not Started"});
        updateStatistics();
    }

    // Метод для обновления прогресса задачи
    public void updateTask(String taskId, int progress) {
        TaskProgress taskProgress = progressMap.get(taskId);
        if (taskProgress != null) {
            taskProgress.setProgress(progress);
            updateTaskInTable(taskId);
        }
    }

    // Метод для обновления использования памяти задачей
    public void updateTaskMemoryUsage(String taskId, long memoryUsage) {
        TaskProgress taskProgress = progressMap.get(taskId);
        if (taskProgress != null) {
            taskProgress.setMemoryUsage(memoryUsage);
            updateTaskInTable(taskId);
        }
    }

    // Метод для удаления задачи
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


    // Обновляем прогресс-бар
    private void updateStatistics() {
        SwingUtilities.invokeLater(() -> {
            int activeTasks = progressMap.size();
            long totalMemoryUsage = progressMap.values().stream().mapToLong(TaskProgress::getMemoryUsage).sum();
            totalTasksLabel.setText("Active Tasks: " + activeTasks);
            totalMemoryLabel.setText("Total Memory Usage: " + formatMemory(totalMemoryUsage));
            systemMemoryLabel.setText("System Memory: " + getMemoryStats());

            // Обновление прогресс-бара
            MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
            MemoryUsage heapMemoryUsage = memoryMXBean.getHeapMemoryUsage();
            long maxMemory = heapMemoryUsage.getMax();
            long usedMemory = heapMemoryUsage.getUsed();

            int memoryPercentage = (int) ((usedMemory * 100) / maxMemory);
            if(memoryProgressBar != null) {
                memoryProgressBar.setValue(memoryPercentage);
                memoryProgressBar.setString(memoryPercentage + "% Used");
            }
        });
    }

    // Метод для запуска периодического обновления статистики
    private void startUpdating() {
        Timer updateTimer = new Timer();
        updateTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                updateStatistics();
            }
        }, 0, UPDATE_INTERVAL);
    }

    // Метод для обновления данных конкретной задачи в таблице
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

    // Метод для инициализации окна менеджера задач
    private void initializeFrame() {
        statusFrame = new JFrame("Task Manager");
        statusFrame.setLayout(new BorderLayout());
        statusFrame.setSize(800, 600);
        statusFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Центрирование окна на экране
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int x = (screenSize.width - statusFrame.getWidth()) / 2;
        int y = (screenSize.height - statusFrame.getHeight()) / 2;
        statusFrame.setLocation(x, y);

        // Настройка таблицы задач
        taskTable.setFillsViewportHeight(true);
        taskTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        taskTable.setFont(new Font("Monospaced", Font.PLAIN, 12));
        taskTable.setRowHeight(25);
        taskTable.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 14));
        taskTable.getTableHeader().setBorder(new EmptyBorder(5, 5, 5, 5));
        JScrollPane tableScrollPane = new JScrollPane(taskTable);

        // Инициализация прогресс-бара загрузки памяти
        memoryProgressBar = new JProgressBar(0, 100);
        memoryProgressBar.setStringPainted(true);

        // Панель для прогресс-бара с рамкой
        JPanel progressPanel = new JPanel(new BorderLayout());
        progressPanel.setBorder(BorderFactory.createTitledBorder("Memory Usage"));
        progressPanel.add(memoryProgressBar, BorderLayout.CENTER);

        // Настройка панели статистики (с 4 строками: 3 метки и панель прогресс-бара)
        JPanel statisticsPanel = new JPanel(new GridLayout(4, 1));
        statisticsPanel.setBorder(BorderFactory.createTitledBorder("Statistics"));
        statisticsPanel.add(totalTasksLabel);
        statisticsPanel.add(totalMemoryLabel);
        statisticsPanel.add(systemMemoryLabel);
        statisticsPanel.add(progressPanel);

        // Настройка кнопки завершения задачи
        JButton terminateButton = new JButton("Terminate Task");
        terminateButton.addActionListener(e -> {
            int selectedRow = taskTable.getSelectedRow();
            if (selectedRow != -1) {
                String taskId = (String) tableModel.getValueAt(selectedRow, 0);
                removeTask(taskId);
            }
        });

        // Основная панель с расположением элементов
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(tableScrollPane, BorderLayout.CENTER);
        mainPanel.add(statisticsPanel, BorderLayout.NORTH);
        mainPanel.add(terminateButton, BorderLayout.SOUTH);

        // Добавление основной панели в окно
        statusFrame.add(mainPanel, BorderLayout.CENTER);
        statusFrame.setVisible(true);
    }



    // Метод для определения статуса задачи по её прогрессу
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

    // Метод для форматирования памяти в удобочитаемый формат
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

    // Метод для получения статистики памяти системы
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

    // Метод для получения окна статуса
    public JFrame getStatusFrame() {
        return statusFrame;
    }
}