package org.foxesworld.engine.utils.Download;

import org.foxesworld.engine.Engine;

import javax.swing.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.concurrent.ExecutionException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class DownloadUtils {
    private final Engine engine;
    private DownloadListener downloadListener;
    private final JLabel progressLabel;
    private JProgressBar progressBar;

    public DownloadUtils(Engine engine) {
        this.engine = engine;
        this.progressLabel = (JLabel) engine.getSystemComponents().getComponentsMap().get("progressLabel");
        this.progressBar = (JProgressBar) engine.getSystemComponents().getComponentsMap().get("PBar");
        this.progressBar.add(progressLabel);
    }

    public void downloadAsync(String Durl, String PATH) {
        progressBar.setVisible(true);
        progressLabel.setVisible(true);
        try {Thread.sleep(1000L);} catch (InterruptedException e) {throw new RuntimeException(e);}
        SwingWorker<Void, Integer> worker = new SwingWorker<Void, Integer>() {
            @Override
            protected Void doInBackground() {
                download(Durl, PATH);
                if(Durl.contains(engine.getEngineData().getLauncherRuntime())) {
                    unpackAsync(PATH, new File(PATH).getParentFile());
                }
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                } finally {
                    SwingUtilities.invokeLater(() -> {
                        progressBar.setVisible(false);
                        progressLabel.setVisible(false);
                        progressBar.setValue(0);
                    });
                }
            }
        };

        worker.execute();
    }

    public void unpackAsync(String path, File dir_to) {
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() {
                unpack(path, dir_to);
                return null;
            }
        };
        worker.execute();
    }

    private void download(String url, String destinationPath) {
        engine.displayPanel("download->true");
        progressBar.setValue(0);

        try {
            URL downloadUrl = new URL(url);
            HttpURLConnection connection = (HttpURLConnection) downloadUrl.openConnection();
            long fileSize = connection.getContentLength();
            String fileName = extractFileNameFromUrl(downloadUrl);
            downloadListener.downloading(fileName);
            try (InputStream inputStream = new BufferedInputStream(connection.getInputStream())) {
                File destinationFile = new File(destinationPath);
                FileOutputStream outputStream = new FileOutputStream(destinationFile);

                byte[] buffer = new byte[1024];
                long downloaded = 0;
                int read;
                while ((read = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, read);
                    downloaded += read;
                    int progress = (int) ((downloaded * 100) / fileSize);
                    String loadProgress = formatFileSize(downloaded) + " / " + formatFileSize(fileSize);
                    SwingUtilities.invokeLater(() -> {
                        progressLabel.setText(loadProgress);
                        progressBar.setValue(progress);
                    });
                    publish(progress, fileName);
                }

                outputStream.close();
            }

            SwingUtilities.invokeLater(() -> {
                progressBar.setVisible(false);
                progressLabel.setVisible(false);
                progressBar.setValue(0);
                //engine.getSetInfo().setInfo("download.downloaded", fileName, false);
                downloadListener.onFileDownloaded(fileName);
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String formatFileSize(long sizeInBytes) {
        if (sizeInBytes < 1024) {
            return sizeInBytes + " bytes";
        } else if (sizeInBytes < 1024 * 1024) {
            double sizeInKb = sizeInBytes / 1024.0;
            return String.format("%.2f KB", sizeInKb);
        } else if (sizeInBytes < 1024 * 1024 * 1024) {
            double sizeInMb = sizeInBytes / (1024.0 * 1024.0);
            return String.format("%.2f MB", sizeInMb);
        } else {
            double sizeInGb = sizeInBytes / (1024.0 * 1024.0 * 1024.0);
            return String.format("%.2f GB", sizeInGb);
        }
    }



    private String extractFileNameFromUrl(URL url) {
        String urlString = url.toString();
        int lastSlashIndex = urlString.lastIndexOf('/');
        return lastSlashIndex != -1 && lastSlashIndex < urlString.length() - 1 ? urlString.substring(lastSlashIndex + 1) : null;
    }

    private void unpack(String zipFilePath, File destinationDir) {
        String fileName = null;
        try (ZipFile zipFile = new ZipFile(zipFilePath, StandardCharsets.UTF_8)) {
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            LinkedList<ZipEntry> zippedFiles = new LinkedList<>();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                if (entry.isDirectory()) {
                    new File(destinationDir + File.separator + entry.getName()).mkdirs();
                } else {
                    zippedFiles.add(entry);
                }
            }

            for (ZipEntry entry : zippedFiles) {
                fileName = entry.getName();
                File outFile = new File(destinationDir, fileName);
                //engine.getSetInfo().setInfo("download.unpacking", fileName, false);
                downloadListener.unpacking(fileName);
                try (InputStream inputStream = zipFile.getInputStream(entry);
                     OutputStream outputStream = Files.newOutputStream(outFile.toPath())) {
                    if (!outFile.getParentFile().exists()) {
                        outFile.getParentFile().mkdirs();
                    }
                    byte[] buffer = new byte[1024];
                    int len;
                    while ((len = inputStream.read(buffer)) >= 0) {
                        outputStream.write(buffer, 0, len);
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        new File(zipFilePath).delete();
        //engine.getSetInfo().setInfo("download.unpacked", fileName, false);
        downloadListener.onFileUnpacked(fileName);
    }

    private void publish(int progress, String fileName) {
        SwingUtilities.invokeLater(() -> {
            progressBar.setValue(progress);
            //engine.getSetInfo().setInfo("download.downloading", fileName, false);
            //downloadListener.downloading(progress, fileName);
        });
    }

    public void setDownloadListener(DownloadListener downloadListener) {
        this.downloadListener = downloadListener;
    }
}
