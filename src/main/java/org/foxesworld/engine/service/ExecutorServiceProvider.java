package org.foxesworld.engine.service;

import org.foxesworld.engine.Engine;

import javax.swing.*;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.function.Consumer;

@SuppressWarnings("unused")
public class ExecutorServiceProvider {

    private final ExecutorService executorService;
    private final ExecutorProgress executorProgress;
    private final String threadNamePrefix;
    private final ConcurrentHashMap<UUID, SwingWorker<?, ?>> taskMap = new ConcurrentHashMap<>();

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
        Engine.LOGGER.info("Initializing ExecutorService with pool size: {}", poolSize);
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("executor-config.properties")) {
            if (input == null) {
                Engine.LOGGER.warn("executor-config.properties not found, using default settings.");
                return createDefaultExecutorService(poolSize);
            }
            properties.load(input);
            int configuredPoolSize = Integer.parseInt(properties.getProperty("executor.pool.size", String.valueOf(poolSize)));
            Engine.LOGGER.info("Configured pool size from properties file: {}", configuredPoolSize);
            return createExecutorService(configuredPoolSize);
        } catch (IOException ex) {
            Engine.LOGGER.error("Error loading executor-config.properties, using default settings: {}", ex.getMessage());
            return createDefaultExecutorService(poolSize);
        }
    }

    /**
     * Creates a default ExecutorService with a fixed thread pool.
     *
     * @return A default ExecutorService.
     */
    private ExecutorService createDefaultExecutorService(int poolSize) {
        Engine.LOGGER.info("Creating default ExecutorService with pool size: {}", poolSize);
        return Executors.newFixedThreadPool(poolSize, new CustomThreadFactory(this.threadNamePrefix));
    }

    /**
     * Creates an ExecutorService with a custom thread factory.
     *
     * @param poolSize The number of threads in the pool.
     * @return A custom ExecutorService.
     */
    private ExecutorService createExecutorService(int poolSize) {
        Engine.LOGGER.info("Creating custom ExecutorService with pool size: {}", poolSize);
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
     * @return The UUID of the submitted task.
     */
    public UUID submitTask(Runnable task, String taskName) {
        UUID taskId = UUID.randomUUID();
        executorProgress.addTask(taskId.toString(), taskName);
        Engine.LOGGER.debug("Submitting task: {} with ID: {}", taskName, taskId);

        SwingWorker<Void, Integer> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                try {
                    task.run();
                    publish(100);  // Mark progress as complete
                    Engine.LOGGER.debug("Task completed: {} with ID: {}", taskName, taskId);
                } finally {
                    executorProgress.removeTask(taskId.toString());
                    taskMap.remove(taskId);
                    Engine.LOGGER.debug("Task removed: {} with ID: {}", taskName, taskId);
                }
                return null;
            }

            @Override
            protected void process(List<Integer> chunks) {
                for (int progress : chunks) {
                    executorProgress.updateTask(taskId.toString(), progress);
                }
            }

            @Override
            protected void done() {
                try {
                    get();
                } catch (InterruptedException | ExecutionException e) {
                    Engine.LOGGER.error("Error executing task: {} with ID: {}: {}", taskName, taskId, e.getMessage());
                }
            }
        };

        executorService.submit(worker);
        taskMap.put(taskId, worker);
        return taskId;
    }

    /**
     * Submits a dynamic task to the executor service with a callback and progress tracking.
     *
     * @param task     The task to execute.
     * @param taskName The name of the task.
     * @param callback The callback to execute with the result of the task.
     * @param <T>      The type of the result produced by the task.
     * @return The UUID of the submitted task.
     */
    public <T> UUID submitDynamicTaskWithCallback(Callable<T> task, String taskName, Consumer<T> callback) {
        UUID taskId = UUID.randomUUID();
        executorProgress.addTask(taskId.toString(), taskName);
        Engine.LOGGER.debug("Submitting dynamic task: {} with ID: {}", taskName, taskId);

        SwingWorker<T, Integer> worker = new SwingWorker<>() {
            @Override
            protected T doInBackground() throws Exception {
                T result = task.call();
                publish(100);  // Mark progress as complete
                return result;
            }

            @Override
            protected void process(List<Integer> chunks) {
                for (int progress : chunks) {
                    executorProgress.updateTask(taskId.toString(), progress);
                }
            }

            @Override
            protected void done() {
                try {
                    T result = get();
                    SwingUtilities.invokeLater(() -> {
                        if (callback != null) {
                            callback.accept(result);
                        }
                    });
                } catch (Exception e) {
                    Engine.LOGGER.error("Error executing dynamic task: {} with ID: {}: {}", taskName, taskId, e.getMessage());
                } finally {
                    executorProgress.removeTask(taskId.toString());
                    taskMap.remove(taskId);
                    Engine.LOGGER.debug("Dynamic task removed: {} with ID: {}", taskName, taskId);
                }
            }
        };

        executorService.submit(worker);
        taskMap.put(taskId, worker);
        return taskId;
    }

    /**
     * Completes the task with the given UUID.
     *
     * @param taskId The UUID of the task to complete.
     */
    public void completeTask(UUID taskId) {
        SwingWorker<?, ?> worker = taskMap.remove(taskId);
        if (worker != null) {
            worker.cancel(true);
            executorProgress.removeTask(taskId.toString());
            Engine.LOGGER.info("Task completed: {}", taskId);
        } else {
            Engine.LOGGER.warn("No task found with ID: {}", taskId);
        }
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
            Engine.LOGGER.info("RejectedExecutionHandler set for the ExecutorService");
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
        Engine.LOGGER.info("Shutting down ExecutorService");
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