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
            HttpURLConnection connection = null;
            try {
                URL url = new URL(engine.getEngineData().getBindUrl());
                connection = (HttpURLConnection) url.openConnection();
                configureConnection(connection);

                sendRequest(parameters, connection);
                String response = getResponse(connection);

                onSuccess.onSuccess(response);

            } catch (SocketException e) {
                Engine.LOGGER.warn("Socket closed unexpectedly {}", e);
                if (onFailure != null) {
                    onFailure.onFailure(e);
                }
            } catch (Exception e) {
                if (onFailure != null) {
                    onFailure.onFailure(e);
                }
                Engine.LOGGER.error("Request failed {}", e);
            } finally {
                // Закрытие соединения после завершения работы
                if (connection != null) {
                    connection.disconnect();
                }
            }
        });
    }

    private void sendRequest(Map<String, Object> parameters, HttpURLConnection connection) throws Exception {
        try (OutputStream os = connection.getOutputStream()) {
            String postData = formParams(parameters).toString();
            os.write(postData.getBytes(StandardCharsets.UTF_8));
        }
    }

    private String getResponse(HttpURLConnection connection) throws Exception {
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
        connection.setUseCaches(httpConf.isUseCaches());
        connection.setDoInput(httpConf.isDoInput());
        connection.setDoOutput(httpConf.isDoOutput());
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

    public HttpURLConnection getHttpURLConnection() {
        return httpURLConnection;
    }

    public void shutdown() {
        executorService.shutdown();
    }
}