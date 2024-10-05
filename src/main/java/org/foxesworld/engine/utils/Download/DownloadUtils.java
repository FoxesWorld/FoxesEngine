package org.foxesworld.engine.utils.Download;

import org.foxesworld.engine.Engine;

import javax.swing.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class DownloadUtils {
    private final Engine engine;
    private JLabel progressLabel;
    private JProgressBar progressBar;
    private int percent;
    public static int updateInterval = 100;
    private long downloaded = 0;
    public DownloadUtils(Engine engine) {
        this.engine = engine;
    }

    @SuppressWarnings({"unused", "ResultOfMethodCallIgnored"})
    public void downloader(String downloadFile, String savePath, long totalSize) {
        this.progressBar.add(this.progressLabel);

        File parentDir = new File(savePath).getParentFile();
        if (!parentDir.isDirectory()) {
            parentDir.mkdirs();
        }

        Timer timer = new Timer(updateInterval, null);

        try {
            URL url = new URL(engine.getEngineData().getBindUrl() + downloadFile);
            HttpURLConnection httpConnection = (HttpURLConnection) url.openConnection();
            httpConnection.setDoOutput(false);
            httpConnection.setRequestMethod("GET");
            engine.getGETrequest().setRequestProperties(httpConnection, engine.getEngineData().getRequestProperties());
            long fileSize = httpConnection.getContentLength();

            FileOutputStream fileOutputStream = new FileOutputStream(savePath);
            byte[] buffer = new byte[65536];

            long finalDownloaded = downloaded;
            timer.addActionListener(e -> {
                percent = (int) (finalDownloaded * 100 / totalSize);
                progressBar.setValue(percent);
                progressLabel.setText(formatFileSize((int) finalDownloaded) + " / " + formatFileSize(totalSize));
            });

            timer.start();

            try (InputStream in = new BufferedInputStream(httpConnection.getInputStream())) {
                int read;
                while ((read = in.read(buffer, 0, buffer.length)) != -1) {
                    fileOutputStream.write(buffer, 0, read);
                    downloaded += read;
                }
            }

            timer.stop();
            fileOutputStream.close();
            httpConnection.disconnect();

        } catch (IOException e) {
            throw new RuntimeException(e);
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
    @SuppressWarnings({"unused", "ResultOfMethodCallIgnored"})
    public void unpack(String path, File dir_to) {
        File fileZip = new File(path);
        try (ZipFile zip = new ZipFile(path, StandardCharsets.UTF_8)) {
            Enumeration<? extends ZipEntry> entries = zip.entries();
            LinkedList<ZipEntry> zfiles = new LinkedList<>();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                if (entry.isDirectory()) {
                    new File(dir_to + File.separator + entry.getName()).mkdirs();
                } else {
                    zfiles.add(entry);
                }
            }
            for (ZipEntry entry : zfiles) {
                File outFile = new File(dir_to, entry.getName());
                try (InputStream in = zip.getInputStream(entry);
                     OutputStream out = new FileOutputStream(outFile)) {
                    if (!outFile.getParentFile().exists()) {
                        outFile.getParentFile().mkdirs();
                    }
                    byte[] buffer = new byte[1024];
                    int len;
                    while ((len = in.read(buffer)) >= 0) {
                        out.write(buffer, 0, len);
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        fileZip.delete();
    }

    @SuppressWarnings("unused")
    public void setProgressLabel(JLabel progressLabel) {
        this.progressLabel = progressLabel;
    }

    @SuppressWarnings("unused")
    public void setProgressBar(JProgressBar progressBar) {
        this.progressBar = progressBar;
    }
}