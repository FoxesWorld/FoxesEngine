package org.foxesworld.engine.service;

class TaskProgress {
    private final String name;
    private int progress;
    private long memoryUsage;
    private OnUpdateListener onUpdateListener;

    interface OnUpdateListener {
        void onUpdate(int progress, long memoryUsage);
    }

    TaskProgress(String name) {
        this.name = name;
        this.progress = 0;
        this.memoryUsage = 0;
    }

    public void setProgress(int progress) {
        this.progress = progress;
        notifyUpdate();
    }

    public int getProgress() {
        return progress;
    }

    public String getName() {
        return name;
    }

    public void setMemoryUsage(long memoryUsage) {
        this.memoryUsage = memoryUsage;
        notifyUpdate();
    }

    public long getMemoryUsage() {
        return memoryUsage;
    }

    public boolean isComplete() {
        return progress >= 100;
    }

    public void setOnUpdateListener(OnUpdateListener listener) {
        this.onUpdateListener = listener;
    }

    private void notifyUpdate() {
        if (onUpdateListener != null) {
            onUpdateListener.onUpdate(progress, memoryUsage);
        }
    }

    @Override
    public String toString() {
        return String.format("Task[name='%s', progress=%d%%, memoryUsage=%d bytes]",
                name, progress, memoryUsage);
    }
}
