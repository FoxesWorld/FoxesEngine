package org.foxesworld.engine.service;

public class TaskProgress {
    private final String taskName;
    private int progress;
    private long memoryUsage;
    private boolean isCompleted;

    public TaskProgress(String taskName) {
        this.taskName = taskName;
        this.progress = 0;
        this.memoryUsage = 0;
        this.isCompleted = false;
    }

    public String getTaskName() {
        return taskName;
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    public long getMemoryUsage() {
        return memoryUsage;
    }

    public void setMemoryUsage(long memoryUsage) {
        this.memoryUsage = memoryUsage;
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    public void complete() {
        this.isCompleted = true;
    }
}
