package org.foxesworld.engine.utils;

import me.tongfei.progressbar.ProgressBar;
import me.tongfei.progressbar.ProgressBarBuilder;
import me.tongfei.progressbar.ProgressBarStyle;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.atomic.AtomicLong;

public class Downloader {
    public static void downloadFile(String url,  Path downloadDirectory, String taskName) {
        try {
            long size = getFileSize(url);
            if (size < 0) {
                System.out.println("File size is invalid.");
                return;
            }

            createDirectoryIfNotExists(downloadDirectory);

            ProgressBar fileProgressBar = new ProgressBarBuilder()
                    .setTaskName(taskName)
                    .setInitialMax(size)
                    .setStyle(ProgressBarStyle.ASCII)
                    .setUnit("KB", 1024)
                    .build();

            AtomicLong current = new AtomicLong(0);

            download(url, fileProgressBar, current, downloadDirectory);

            fileProgressBar.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void createDirectoryIfNotExists(Path directory) throws IOException {
        if (!Files.exists(directory)) {
            Files.createDirectories(directory);
        }
    }

    private static long getFileSize(String urlString) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("HEAD");
        long size = connection.getContentLengthLong();
        connection.disconnect();
        return size;
    }

    private static void download(String url, ProgressBar fileProgressBar, AtomicLong current, Path downloadDirectory) {
        try (fileProgressBar) {
            URL fileUrl = new URL(url);
            HttpURLConnection connection = (HttpURLConnection) fileUrl.openConnection();
            try (InputStream inputStream = connection.getInputStream()) {
                String fileName = getFileNameFromUrl(url);
                Path filePath = downloadDirectory.resolve(fileName);
                try (OutputStream outputStream = Files.newOutputStream(filePath, StandardOpenOption.CREATE, StandardOpenOption.WRITE)) {
                    byte[] buffer = new byte[1024];
                    int bytesRead;
                    long totalBytesRead = 0;
                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, bytesRead);
                        totalBytesRead += bytesRead;
                        fileProgressBar.stepTo(totalBytesRead);
                    }
                }
            } finally {
                connection.disconnect();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private static String getFileNameFromUrl(String urlString) {
        String[] parts = urlString.split("/");
        return parts[parts.length - 1];
    }

}
