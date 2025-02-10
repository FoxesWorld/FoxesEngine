package org.foxesworld.engine.utils.HTTP;

import org.foxesworld.engine.Engine;

import java.io.*;
import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@SuppressWarnings("unused")
public class HTTPrequest {

    private final String requestMethod;
    private final Engine engine;
    private final ExecutorService executorService;
    private HttpURLConnection httpURLConnection;

    public HTTPrequest(Engine engine, String requestMethod) {
        this.engine = engine;
        this.requestMethod = requestMethod;
        this.executorService = Executors.newCachedThreadPool();
        Engine.LOGGER.info("HTTP {} initialized", requestMethod);
    }

    public void sendAsync(Map<String, Object> extraParams, OnSuccess onSuccess, OnFailure onFailure) {
        executorService.submit(() -> {
            HttpURLConnection connection = null;
            try {
                URL url = new URL(engine.getEngineData().getBindUrl());
                connection = (HttpURLConnection) url.openConnection();
                configureConnection(connection);

                Map<String, Object> allParams = new HashMap<>(collectParams());
                allParams.putAll(extraParams);

                sendRequest(allParams, connection);
                String response = getResponse(connection);
                onSuccess.onSuccess(response);

            } catch (Exception e) {
                if (onFailure != null) {
                    onFailure.onFailure(e);
                }
                Engine.LOGGER.error("Request failed {}", e);
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
        });
    }


    private void sendRequest(Map<String, Object> parameters, HttpURLConnection connection) throws IOException {
        try (OutputStream os = connection.getOutputStream()) {
            String postData = formParams(parameters).toString();
            os.write(postData.getBytes(StandardCharsets.UTF_8));
        }
    }

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

    public void setRequestProperties(HttpURLConnection connection, List<RequestProperty> properties) {
        for (RequestProperty requestProperty : properties) {
            String value = requestProperty.getPropertyValue();
            if (value.contains("{$boundary}")) {
                value = value.replace("{$boundary}", getBoundary(3, 3));
            }
            connection.setRequestProperty(requestProperty.getPropertyKey(), value);
        }
    }


    private Map<String, Object> collectParams() {
        Map<String, Object> params = new HashMap<>();

        for (Field field : this.getClass().getDeclaredFields()) {
            if (field.isAnnotationPresent(HttpParam.class)) {
                field.setAccessible(true);
                try {
                    Object value = field.get(this);
                    if (value != null) {
                        String key = field.getAnnotation(HttpParam.class).key();
                        params.put(key, value);
                    }
                } catch (IllegalAccessException e) {
                    Engine.LOGGER.error("Error reading param {}: {}", field.getName(), e.getMessage());
                }
            }
        }

        return params;
    }
    private String getBoundary(int length, int radix) {
        StringBuilder boundary = new StringBuilder();
        Random random = new Random();
        for (int k = 0; k < length; k++) {
            boundary.append(Long.toString(random.nextLong(), radix));
        }
        return boundary.toString();
    }

    public HttpURLConnection getHttpURLConnection() {
        return httpURLConnection;
    }

    public void shutdown() {
        executorService.shutdown();
    }

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
                    String key = field.getAnnotation(HttpHeader.class).key();
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

}
