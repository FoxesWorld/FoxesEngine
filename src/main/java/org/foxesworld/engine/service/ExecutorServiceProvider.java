package org.foxesworld.engine.service;

import org.foxesworld.engine.Engine;

import javax.swing.*;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.concurrent.*;

@SuppressWarnings("unused")
public class ExecutorServiceProvider {

    private final ExecutorService executorService;
    private final ExecutorProgress executorProgress;
    private final String threadNamePrefix;

    /**
     * Constructor to initialize the ExecutorServiceProvider with a specified pool size and thread name prefix.
     *
     * @param poolSize        The number of threads in the pool.
     * @param threadNamePrefix The prefix for thread names.
     */
    public ExecutorServiceProvider(int poolSize, String threadNamePrefix) {
        this.threadNamePrefix = threadNamePrefix;
        this.executorProgress = new ExecutorProgress();
        this.executorService = initializeExecutorService(poolSize);
    }

    /**
     * Initializes the ExecutorService using configuration from a properties file or default settings.
     *
     * @return An initialized ExecutorService.
     */
    private ExecutorService initializeExecutorService(int poolSize) {
        Properties properties = new Properties();
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("executor-config.properties")) {
            if (input == null) {
                return createDefaultExecutorService(poolSize);
            }
            properties.load(input);
            int configuredPoolSize = Integer.parseInt(properties.getProperty("executor.pool.size", String.valueOf(poolSize)));
            return createExecutorService(configuredPoolSize);
        } catch (IOException ex) {
            return createDefaultExecutorService(poolSize);
        }
    }

    /**
     * Creates a default ExecutorService with a fixed thread pool.
     *
     * @return A default ExecutorService.
     */
    private ExecutorService createDefaultExecutorService(int poolSize) {
        return Executors.newFixedThreadPool(poolSize, new CustomThreadFactory(this.threadNamePrefix));
    }

    /**
     * Creates an ExecutorService with a custom thread factory.
     *
     * @param poolSize The number of threads in the pool.
     * @return A custom ExecutorService.
     */
    private ExecutorService createExecutorService(int poolSize) {
        return new ThreadPoolExecutor(
                poolSize,
                poolSize,
                0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>(),
                new CustomThreadFactory(this.threadNamePrefix),
                new ThreadPoolExecutor.AbortPolicy()
        );
    }

    /**
     * Submits a task to the executor service with progress tracking.
     *
     * @param task     The task to execute.
     * @param taskName The name of the task.
     */
    public void submitTask(Runnable task, String taskName) {
        String taskId = executorProgress.generateTaskId();
        executorProgress.addTask(taskId, taskName);

        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                try {
                    task.run();
                    executorProgress.updateTask(taskId, 100);
                } finally {
                    executorProgress.removeTask(taskId);
                }
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
            }
        };

        executorService.submit(worker);
    }

    /**
     * Gets the status of the ExecutorService.
     *
     * @return A string representing the status of the ExecutorService.
     */
    protected String getExecutorServiceStatus() {
        if (executorService instanceof ThreadPoolExecutor threadPoolExecutor) {
            if (threadPoolExecutor.getActiveCount() > 0) {
                return String.format("Pool Size: %d, Active Threads: %d, Completed Tasks: %d, Task Count: %d",
                        threadPoolExecutor.getPoolSize(),
                        threadPoolExecutor.getActiveCount(),
                        threadPoolExecutor.getCompletedTaskCount(),
                        threadPoolExecutor.getTaskCount());
            } else {
                return "No active tasks in the executor.";
            }
        } else {
            return "ExecutorService is not an instance of ThreadPoolExecutor";
        }
    }

    /**
     * Sets a custom RejectedExecutionHandler for the ExecutorService.
     *
     * @param handler The handler to set.
     */
    protected void setRejectedExecutionHandler(RejectedExecutionHandler handler) {
        if (executorService instanceof ThreadPoolExecutor threadPoolExecutor) {
            threadPoolExecutor.setRejectedExecutionHandler(handler);
        }
    }

    /**
     * Gets the ExecutorService instance.
     *
     * @return The ExecutorService instance.
     */
    public ExecutorService getExecutorService() {
        return this.executorService;
    }


    /**
     * Shuts down the ExecutorService.
     */
    public void shutdown() {
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(60, TimeUnit.MILLISECONDS)) {
                executorService.shutdownNow();
                if (!executorService.awaitTermination(60, TimeUnit.MILLISECONDS)) {
                    Engine.LOGGER.warn("ExecutorService did not terminate");
                }
            }
        } catch (InterruptedException ie) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Gets the ExecutorProgress instance.
     *
     * @return The ExecutorProgress instance.
     */
    public ExecutorProgress getExecutorProgress() {
        return executorProgress;
    }
}
