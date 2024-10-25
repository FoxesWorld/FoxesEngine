package org.foxesworld.engine.utils.HTTP;

import org.foxesworld.engine.Engine;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.SocketException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@SuppressWarnings("unused")
public class HTTPrequest {

    private static final int MAX_RETRIES = 3;  // Максимальное число повторных попыток
    private static final int RETRY_DELAY_MS = 1000;  // Задержка между повторными попытками

    private final String requestMethod;
    private final Engine engine;
    private final ExecutorService executorService;
    private HttpURLConnection httpURLConnection;

    public HTTPrequest(Engine engine, String requestMethod) {
        this.engine = engine;
        this.requestMethod = requestMethod;
        this.executorService = Executors.newCachedThreadPool();
        Engine.LOGGER.info("HTTP {} init", requestMethod);
    }

    public void sendAsync(Map<String, Object> parameters, OnSuccess onSuccess, OnFailure onFailure) {
        executorService.submit(() -> {
            int attempt = 0;
            while (attempt < MAX_RETRIES) {
                try {
                    URL url = new URL(engine.getEngineData().getBindUrl());
                    httpURLConnection = (HttpURLConnection) url.openConnection();
                    configureConnection(httpURLConnection);
                    sendRequest(parameters);
                    String response = getResponse();
                    onSuccess.onSuccess(response);
                    return;  // Завершаем выполнение, если успех
                } catch (SocketException e) {
                    attempt++;
                    Engine.LOGGER.warn("Socket closed unexpectedly on attempt {}: {}", attempt, e);
                    if (attempt >= MAX_RETRIES && onFailure != null) {
                        onFailure.onFailure(e);
                    }
                    waitBeforeRetry();
                } catch (Exception e) {
                    if (onFailure != null) {
                        onFailure.onFailure(e);
                    }
                    Engine.LOGGER.error("Request failed {}", e);
                    return;  // Завершаем выполнение, если произошла непредвиденная ошибка
                } finally {
                    if (httpURLConnection != null) {
                        httpURLConnection.disconnect();
                    }
                }
            }
        });
    }

    private void configureConnection(HttpURLConnection connection) throws Exception {
        connection.setRequestMethod(this.requestMethod);
        HTTPconf httpConf = engine.getEngineData().getHttPconf();
        setRequestProperties(connection, httpConf.getRequestProperties());
        connection.setUseCaches(httpConf.isUseCaches());
        connection.setDoInput(httpConf.isDoInput());
        connection.setDoOutput(httpConf.isDoOutput());
        connection.setConnectTimeout(5000);  // Задаем тайм-аут соединения (при необходимости)
        connection.setReadTimeout(5000);     // Задаем тайм-аут для чтения данных
        connection.connect();
    }

    private void sendRequest(Map<String, Object> parameters) throws Exception {
        try (OutputStream os = httpURLConnection.getOutputStream()) {
            String postData = formParams(parameters).toString();
            os.write(postData.getBytes(StandardCharsets.UTF_8));
        }
    }

    private String getResponse() throws Exception {
        StringBuilder response = new StringBuilder();
        try (InputStream is = httpURLConnection.getInputStream();
             BufferedReader rd = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            String line;
            while ((line = rd.readLine()) != null) {
                response.append(line);
            }
        }
        return response.toString();
    }

    private StringBuilder formParams(Map<String, Object> parameters) throws UnsupportedEncodingException {
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

    public void setRequestProperties(HttpURLConnection connection, List<RequestProperty> properties) {
        for (RequestProperty requestProperty : properties) {
            String value = requestProperty.getPropertyValue();
            if (value.contains("{$boundary}")) {
                value = value.replace("{$boundary}", getBoundary(3, 3));
            }
            connection.setRequestProperty(requestProperty.getPropertyKey(), value);
        }
    }

    private String getBoundary(int length, int radix) {
        StringBuilder boundary = new StringBuilder();
        Random random = new Random();
        for (int k = 0; k < length; k++) {
            boundary.append(Long.toString(random.nextLong(), radix));
        }
        return boundary.toString();
    }

    private void waitBeforeRetry() {
        try {
            Thread.sleep(RETRY_DELAY_MS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public HttpURLConnection getHttpURLConnection() {
        return httpURLConnection;
    }

    public void shutdown() {
        executorService.shutdown();
    }
}
