package org.foxesworld.engine.service;

class TaskProgress {
    private final String name;
    private int progress;
    private long memoryUsage;

    TaskProgress(String name) {
        this.name = name;
        this.progress = 0;
        this.memoryUsage = 0;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    public int getProgress() {
        return progress;
    }

    public String getName() {
        return name;
    }

    public void setMemoryUsage(long memoryUsage) {
        this.memoryUsage = memoryUsage;
    }

    public long getMemoryUsage() {
        return memoryUsage;
    }
}
