package org.foxesworld.engine.fileLoader;

import com.google.gson.Gson;
import org.foxesworld.engine.Engine;
import org.foxesworld.engine.config.Config;
import org.foxesworld.engine.gui.ActionHandler;
import org.foxesworld.engine.gui.loadingManager.LoadingManager;
import org.foxesworld.engine.utils.Download.DownloadUtils;
import org.foxesworld.engine.utils.HTTP.HTTPrequest;
import org.foxesworld.engine.utils.HTTP.OnFailure;
import org.foxesworld.engine.utils.HTTP.OnSuccess;

import javax.swing.*;
import java.io.File;
import java.io.FileInputStream;
import java.security.MessageDigest;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@SuppressWarnings("unused")
public class FileLoader {
    private final HTTPrequest postRequest;
    private final Engine engine;
    private final LoadingManager loadingManager;
    private final Set<String> filesToKeep = new HashSet<>();
    private final String homeDir, client, version;
    private String fileExtension;
    private String replaceMask;
    private final DownloadUtils downloadUtils;
    private final ExecutorService executorService;
    private final AtomicInteger filesDownloaded = new AtomicInteger(0);
    private FileLoaderListener fileLoaderListener;
    private List<FileAttributes> fileAttributes = new ArrayList<>();
    private FileAttributes currentFile;
    private long totalSize = -1;

    public FileLoader(ActionHandler actionHandler) {
        this.engine = actionHandler.getEngine();
        this.client = actionHandler.getCurrentServer().getServerName();
        this.version = actionHandler.getCurrentServer().getServerVersion();
        this.postRequest = engine.getPOSTrequest();
        this.homeDir = Config.getFullPath();
        this.downloadUtils = new DownloadUtils(engine);
        this.executorService = Executors.newFixedThreadPool(engine.getEngineData().getDownloadManager().getDownloadThreads());
        this.loadingManager = engine.getLoadingManager();
    }

    public void getFilesToDownload(boolean forceUpdate) {
        if (!isClientDataValid(this.client, this.version)) {
            Engine.getLOGGER().warn("Invalid client data: client={}, version={}", this.client, this.version);
            return;
        }

        this.loadingManager.toggleLoader();
        this.loadingManager.setLoadingText("file.gettingFiles-desc", "file.gettingFiles-title");

        fetchDownloadList(client, version, getPlatformNumber())
                .thenAccept(fileAttributes -> SwingUtilities.invokeLater(() -> processFileAttributes(fileAttributes, forceUpdate)))
                .exceptionally(e -> {
                    Engine.getLOGGER().error("Error retrieving file list: {}", e.getMessage(), e);
                    SwingUtilities.invokeLater(() -> this.loadingManager.setLoadingText("file.error-desc", "file.error.title"));
                    return null;
                });
    }

    private boolean isClientDataValid(String client, String version) {
        return client != null && !client.isEmpty() && version != null && !version.isEmpty();
    }

    private void processFileAttributes(FileAttributes[] fileAttributes, boolean forceUpdate) {
        for (FileAttributes file : fileAttributes) {
            this.fileLoaderListener.onFileAdd(file);
        }

        Engine.getLOGGER().info("Keeping " + this.filesToKeep.size() + " files");
        this.loadingManager.setLoadingText("file.listBuilt-desc", "file.listBuilt.title");

        if (forceUpdate) {
            this.fileAttributes = Arrays.asList(fileAttributes);
            Engine.getLOGGER().info("Force updating " + fileAttributes.length + " files");
        } else {
            this.fileAttributes = Arrays.stream(fileAttributes)
                    .filter(file -> !filesToKeep.contains(file.getFilename()))
                    .filter(this::shouldDownloadFile)
                    .collect(Collectors.toList());
        }

        this.fileLoaderListener.onFilesRead();
    }

    private CompletableFuture<FileAttributes[]> fetchDownloadList(String client, String version, int platform) {
        Map<String, Object> request = new HashMap<>();
        request.put("sysRequest", "loadFiles");
        request.put("version", version);
        request.put("client", client);
        request.put("platform", String.valueOf(platform));

        CompletableFuture<FileAttributes[]> future = new CompletableFuture<>();

        postRequest.sendAsync(request, response -> {
            FileAttributes[] fileAttributes = new Gson().fromJson(String.valueOf(response), FileAttributes[].class);
            future.complete(fileAttributes);
        }, future::completeExceptionally);

        return future;
    }

    private boolean shouldDownloadFile(FileAttributes fileSection) {
        String localPath = fileSection.getFilename().replace(fileSection.getReplaceMask(), "");
        File localFile = new File(homeDir, localPath);
        return isInvalidFile(localFile, fileSection.getHash(), fileSection.getSize());
    }

    public void downloadFiles() {
        int totalFiles = fileAttributes.size();
        if (totalFiles == 0) {
            fileLoaderListener.onFilesLoaded();
        } else {
            fileLoaderListener.onDownloadStart();
        }

        totalSize = fileAttributes.stream().mapToLong(FileAttributes::getSize).sum();

        fileAttributes.forEach(file -> executorService.execute(() -> {
            this.currentFile = file;
            fileExtension = getFileExtension(file.getFilename());
            fileLoaderListener.onNewFileFound(this);
            filesDownloaded.incrementAndGet();

            if (filesDownloaded.get() == totalFiles) {
                fileLoaderListener.onFilesLoaded();
            }
        }));
    }

    public boolean isInvalidFile(File file, String expectedHash, long expectedSize) {
        if (!file.exists() || file.length() != expectedSize) {
            return true;
        }

        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] dataBytes = new byte[1024];
            int bytesRead;

            try (FileInputStream fis = new FileInputStream(file)) {
                while ((bytesRead = fis.read(dataBytes)) != -1) {
                    md.update(dataBytes, 0, bytesRead);
                }
            }

            byte[] digestBytes = md.digest();
            StringBuilder hexString = new StringBuilder();

            for (byte digestByte : digestBytes) {
                hexString.append(String.format("%02x", digestByte));
            }

            return !hexString.toString().equals(expectedHash);
        } catch (Exception e) {
            e.printStackTrace();
            return true;
        }
    }

    public int getPlatformNumber() {
        String osName = System.getProperty("os.name").toLowerCase();
        if (osName.contains("win")) {
            return 1;
        } else if (osName.contains("mac")) {
            return 2;
        } else if (osName.contains("nix") || osName.contains("nux") || osName.contains("uni")) {
            return 3;
        } else if (osName.contains("sunos")) {
            return 4;
        } else {
            return 0;
        }
    }

    private String getFileExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex == -1 || dotIndex == fileName.length() - 1) {
            return "file";
        } else {
            return fileName.substring(dotIndex + 1).toLowerCase();
        }
    }

    public void addJreToLoadAsync(String jreVersion, OnSuccess<FileAttributes> onSuccess, OnFailure onFailure) {
        Map<String, Object> request = new HashMap<>();
        request.put("sysRequest", "getJre");
        request.put("jreVersion", jreVersion);

        HTTPrequest httpRequest = new HTTPrequest(engine, "POST");
        httpRequest.sendAsync(request, response -> {
            try {
                FileAttributes jreFile = new Gson().fromJson((String) response, FileAttributes.class);
                jreFile.setReplaceMask(this.replaceMask);
                onSuccess.onSuccess(jreFile);
            } catch (Exception e) {
                if (onFailure != null) {
                    onFailure.onFailure(e);
                }
            }
        }, onFailure);
    }

    public void setLoaderListener(FileLoaderListener fileLoaderListener) {
        this.fileLoaderListener = fileLoaderListener;
    }

    public DownloadUtils getDownloadUtils() {
        return downloadUtils;
    }

    public Set<String> getFilesToKeep() {
        return filesToKeep;
    }

    public void addFileToKeep(String fileToKeep) {
        this.filesToKeep.add(fileToKeep);
    }

    public void addFileToDownload(FileAttributes fileAttributes) {
        this.fileAttributes.add(fileAttributes);
    }

    public void setReplaceMask(String replaceMask) {
        this.replaceMask = replaceMask;
    }

    public String getHomeDir() {
        return homeDir;
    }

    public String getFileType() {
        return fileExtension;
    }

    public FileAttributes getCurrentFile() {
        return currentFile;
    }

    public long getTotalSize() {
        if (totalSize == -1) {
            totalSize = fileAttributes.stream()
                    .mapToLong(fileSection -> {
                        String localPath = fileSection.getFilename().replace(fileSection.getReplaceMask(), "");
                        File localFile = new File(homeDir, localPath);
                        return (localFile.exists() && localFile.length() == fileSection.getSize())
                                ? 0
                                : fileSection.getSize();
                    })
                    .sum();
        }
        return totalSize;
    }
}