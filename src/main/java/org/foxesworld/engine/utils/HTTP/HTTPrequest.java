package org.foxesworld.engine.utils.HTTP;

import org.foxesworld.engine.Engine;

import java.io.*;
import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.*;

/**
 * A utility class for performing asynchronous HTTP requests.
 * <p>
 * This class uses reflection to collect HTTP parameters and headers from annotated fields,
 * sends requests asynchronously, and supports both callback-based and CompletableFuture-based approaches.
 * </p>
 * <p>
 * Note: The {@link #sendAsync(Map, OnSuccess, OnFailure)} method is deprecated. Please use {@link #sendAsyncCF(Map)}
 * instead.
 * </p>
 */
public class HTTPrequest {
    private final String requestMethod;
    private final Engine engine;
    private final ExecutorService executorService;
    private static final int RETRY_INTERVAL = 50; // in milliseconds

    private volatile RequestState requestState;
    private HttpURLConnection httpURLConnection; // Last used connection reference

    // Shared Random instance for generating boundary strings
    private static final Random RANDOM = new Random();

    /**
     * Constructs a new HTTPrequest with the specified engine and request method.
     *
     * @param engine        the engine instance providing configuration and logging
     * @param requestMethod the HTTP request method (e.g., "GET", "POST")
     */
    public HTTPrequest(Engine engine, String requestMethod) {
        this.engine = engine;
        this.requestMethod = requestMethod;
        this.executorService = Executors.newCachedThreadPool();
        this.requestState = RequestState.PENDING;
        Engine.LOGGER.info("HTTP {} initialized", requestMethod);
    }

    /**
     * Gets the current state of the request.
     *
     * @return the current request state (PENDING, SUCCESS, or FAILED)
     */
    public RequestState getRequestState() {
        return requestState;
    }

    /**
     * Sends an asynchronous HTTP request with additional parameters.
     * <p>
     * This method is deprecated. Use {@link #sendAsyncCF(Map)} for a CompletableFuture-based approach.
     * </p>
     *
     * @param extraParams additional request parameters
     * @param onSuccess   callback to be executed on a successful response
     * @param onFailure   callback to be executed on failure
     * @deprecated Use {@link #sendAsyncCF(Map)} instead.
     */
    @Deprecated
    public void sendAsync(Map<String, Object> extraParams, OnSuccess onSuccess, OnFailure onFailure) {
        sendAsyncCF(extraParams).whenComplete((response, throwable) -> {
            if (throwable != null) {
                if (onFailure != null) {
                    onFailure.onFailure((Exception) throwable);
                }
            } else {
                onSuccess.onSuccess(response);
            }
        });
    }

    /**
     * Sends an asynchronous HTTP request with additional parameters.
     * <p>
     * Returns a {@link CompletableFuture} that will complete when the server responds.
     * </p>
     *
     * @param extraParams additional request parameters
     * @return CompletableFuture containing the server response, or an exception if the request fails
     */
    public CompletableFuture<String> sendAsyncCF(Map<String, Object> extraParams) {
        CompletableFuture<String> future = new CompletableFuture<>();
        executorService.submit(() -> {
            try {
                String response = executeRequest(extraParams);
                requestState = RequestState.SUCCESS;
                future.complete(response);
            } catch (Exception e) {
                requestState = RequestState.FAILED;
                future.completeExceptionally(e);
                Engine.LOGGER.error("Request failed {}", e);
            }
        });
        return future;
    }

    /**
     * Executes the HTTP request synchronously.
     *
     * @param extraParams additional request parameters
     * @return the response from the server
     * @throws Exception if an error occurs during the request
     */
    private String executeRequest(Map<String, Object> extraParams) throws Exception {
        URL url = new URL(engine.getEngineData().getBindUrl());
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        // Store the connection reference for potential inspection
        this.httpURLConnection = connection;
        try {
            configureConnection(connection);
            // Merge parameters from annotations and extra parameters
            Map<String, Object> allParams = new HashMap<>(collectParams());
            allParams.putAll(extraParams);
            sendRequest(allParams, connection);
            return getResponse(connection);
        } finally {
            connection.disconnect();
        }
    }

    /**
     * Sends the request payload to the server.
     *
     * @param parameters the request parameters to be sent
     * @param connection the active HttpURLConnection
     * @throws IOException if an I/O error occurs
     */
    private void sendRequest(Map<String, Object> parameters, HttpURLConnection connection) throws IOException {
        try (OutputStream os = connection.getOutputStream()) {
            String postData = formParams(parameters).toString();
            os.write(postData.getBytes(StandardCharsets.UTF_8));
        }
    }

    /**
     * Reads the response from the server.
     *
     * @param connection the active HttpURLConnection
     * @return the response as a String
     * @throws IOException if an I/O error occurs
     */
    private String getResponse(HttpURLConnection connection) throws IOException {
        StringBuilder response = new StringBuilder();
        try (InputStream is = connection.getInputStream();
             BufferedReader rd = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            String line;
            while ((line = rd.readLine()) != null) {
                response.append(line);
            }
        }
        return response.toString();
    }

    /**
     * Configures the HttpURLConnection with method, timeouts, headers, and other settings.
     *
     * @param connection the HttpURLConnection to configure
     * @throws Exception if an error occurs during configuration
     */
    private void configureConnection(HttpURLConnection connection) throws Exception {
        connection.setRequestMethod(this.requestMethod);
        HTTPconf httpConf = engine.getEngineData().getHttPconf();
        setRequestProperties(connection, httpConf.getRequestProperties());
        applyAnnotations(connection);
        connection.setUseCaches(httpConf.isUseCaches());
        connection.setDoInput(httpConf.isDoInput());
        connection.setDoOutput(httpConf.isDoOutput());
        connection.connect();
    }

    /**
     * Constructs a URL-encoded query string from the given parameters.
     *
     * @param parameters the request parameters
     * @return a StringBuilder containing the URL-encoded query string
     */
    private StringBuilder formParams(Map<String, Object> parameters) {
        StringBuilder postData = new StringBuilder();
        for (Map.Entry<String, Object> param : parameters.entrySet()) {
            if (postData.length() != 0) {
                postData.append('&');
            }
            postData.append(URLEncoder.encode(param.getKey(), StandardCharsets.UTF_8))
                    .append('=')
                    .append(URLEncoder.encode(param.getValue().toString(), StandardCharsets.UTF_8));
        }
        return postData;
    }

    /**
     * Sets request properties (headers) on the HttpURLConnection.
     *
     * @param connection the HttpURLConnection to configure
     * @param properties a list of RequestProperty objects representing header key-value pairs
     */
    public void setRequestProperties(HttpURLConnection connection, List<RequestProperty> properties) {
        for (RequestProperty requestProperty : properties) {
            String value = requestProperty.getPropertyValue();
            if (value.contains("{$boundary}")) {
                value = value.replace("{$boundary}", getBoundary(3, 3));
            }
            connection.setRequestProperty(requestProperty.getPropertyKey(), value);
        }
    }

    /**
     * Collects parameters annotated with {@code @HttpParam} from the current instance.
     *
     * @return a map of parameter names and their corresponding values
     */
    private Map<String, Object> collectParams() {
        Map<String, Object> params = new HashMap<>();
        for (Field field : this.getClass().getDeclaredFields()) {
            if (field.isAnnotationPresent(HttpParam.class)) {
                field.setAccessible(true);
                try {
                    Object value = field.get(this);
                    if (value != null) {
                        HttpParam httpParam = field.getAnnotation(HttpParam.class);
                        String key = httpParam.value().isEmpty() ? field.getName() : httpParam.value();
                        params.put(key, value);
                    }
                } catch (IllegalAccessException e) {
                    Engine.LOGGER.error("Error reading param {}: {}", field.getName(), e.getMessage());
                }
            }
        }
        return params;
    }

    /**
     * Generates a boundary string for multipart requests.
     *
     * @param length the number of segments to generate
     * @param radix  the radix used for number conversion
     * @return a generated boundary string
     */
    private String getBoundary(int length, int radix) {
        StringBuilder boundary = new StringBuilder();
        for (int k = 0; k < length; k++) {
            boundary.append(Long.toString(RANDOM.nextLong(), radix));
        }
        return boundary.toString();
    }

    /**
     * Waits for the current HTTP request to complete by periodically checking its state,
     * then executes the provided completion action.
     *
     * @param onComplete the action to execute once the request state is no longer PENDING
     */
    public void waitForRequestCompletion(Runnable onComplete) {
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(() -> {
            if (getRequestState() != RequestState.PENDING) {
                scheduler.shutdown();
                onComplete.run();
            }
        }, 0, RETRY_INTERVAL, TimeUnit.MILLISECONDS);
    }

    /**
     * Gets the last used HttpURLConnection.
     *
     * @return the last HttpURLConnection instance, or null if none
     */
    public HttpURLConnection getHttpURLConnection() {
        return httpURLConnection;
    }

    /**
     * Shuts down the executor service used for asynchronous requests.
     */
    public void shutdown() {
        executorService.shutdown();
    }

    /**
     * Applies annotations to configure the HttpURLConnection (e.g., timeouts and headers).
     * <p>
     * Fields annotated with {@code @HttpConfig} or {@code @HttpHeader} will be processed.
     * Note: For header fields, the key is derived from the {@code @HttpParam} annotation.
     * </p>
     *
     * @param connection the HttpURLConnection to configure
     */
    private void applyAnnotations(HttpURLConnection connection) {
        Class<?> clazz = this.getClass();

        if (clazz.isAnnotationPresent(HttpConfig.class)) {
            HttpConfig config = clazz.getAnnotation(HttpConfig.class);
            connection.setConnectTimeout(config.connectTimeout());
            connection.setReadTimeout(config.readTimeout());
            Engine.LOGGER.info("Setting timeout: connect = {} ms, read = {} ms", config.connectTimeout(), config.readTimeout());
        }

        for (Field field : clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(HttpHeader.class)) {
                field.setAccessible(true);
                try {
                    // Assuming the header key is provided via the @HttpParam annotation.
                    HttpParam httpParam = field.getAnnotation(HttpParam.class);
                    String key = httpParam.value().isEmpty() ? field.getName() : httpParam.value();
                    String value = (String) field.get(this);
                    if (value != null) {
                        connection.setRequestProperty(key, value);
                        Engine.LOGGER.info("Added header: {} = {}", key, value);
                    }
                } catch (IllegalAccessException e) {
                    Engine.LOGGER.error("Error adding header {}: {}", field.getName(), e.getMessage());
                }
            }
        }
    }

    /**
     * Measures the connection speed to the server by performing a GET request,
     * reading the response, and calculating the transfer rate.
     * <p>
     * The speed is computed as the total number of bytes read divided by the elapsed time.
     * High-precision time measurement (nanoseconds) is used to increase accuracy.
     * </p>
     *
     * @return the measured connection speed in bytes per second.
     * @throws Exception if an error occurs during the connection or data transfer.
     */
    public double measureConnectionSpeed() throws Exception {
        URL url = new URL(engine.getEngineData().getBindUrl());
        HttpURLConnection speedConnection = (HttpURLConnection) url.openConnection();
        speedConnection.setRequestMethod("GET");
        speedConnection.setDoInput(true);
        speedConnection.setConnectTimeout(5000);
        speedConnection.setReadTimeout(5000);
        long startTime = System.nanoTime();
        int totalBytes = 0;
        try (InputStream is = speedConnection.getInputStream()) {
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1) {
                totalBytes += bytesRead;
            }
        } finally {
            speedConnection.disconnect();
        }
        long endTime = System.nanoTime();
        double elapsedTimeSec = (endTime - startTime) / 1_000_000_000.0;
        // Вычисление скорости (байт/сек)
        return totalBytes / elapsedTimeSec;
    }

    /**
     * Форматирует скорость соединения в человекочитаемый вид.
     *
     * @return строка с отформатированным значением и соответствующей единицей измерения.
     */
    public String getFormatedSpeed() {
        double speedBytesPerSec = 0;
        try {
            speedBytesPerSec = this.measureConnectionSpeed();

        if (speedBytesPerSec < 1024) {
            return String.format("%.2f B/s", speedBytesPerSec);
        } else if (speedBytesPerSec < 1024 * 1024) {
            return String.format("%.2f KB/s", speedBytesPerSec / 1024);
        } else {
            return String.format("%.2f MB/s", speedBytesPerSec / (1024 * 1024));
        }
        } catch (Exception e) {
        throw new RuntimeException(e);
    }
    }

    public Engine getEngine() {
        return engine;
    }
}
