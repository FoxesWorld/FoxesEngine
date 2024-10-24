package org.foxesworld.engine.fileLoader;

import org.foxesworld.engine.Engine;
import org.foxesworld.engine.config.Config;
import org.foxesworld.engine.gui.ActionHandler;
import org.foxesworld.engine.gui.loadingManager.LoadingManager;
import org.foxesworld.engine.utils.Download.DownloadUtils;

import javax.swing.*;
import java.io.File;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class FileLoader {

    private final AtomicBoolean isCancelled = new AtomicBoolean(false);
    private final Engine engine;
    private final LoadingManager loadingManager;
    private final Set<String> filesToKeep = new HashSet<>();
    private final String homeDir, client, version;
    private final DownloadUtils downloadUtils;
    private final ExecutorService executorService;
    private final AtomicInteger filesDownloaded = new AtomicInteger(0);
    private FileLoaderListener fileLoaderListener;
    private List<FileAttributes> fileAttributes = new ArrayList<>();
    private FileAttributes currentFile;
    private long totalSize = -1;

    private final FileFetcher fileFetcher;
    private final FileValidator fileValidator;
    private String fileExtension;

    public FileLoader(ActionHandler actionHandler) {
        this.engine = actionHandler.getEngine();
        this.client = actionHandler.getCurrentServer().getServerName();
        this.version = actionHandler.getCurrentServer().getServerVersion();
        this.homeDir = Config.getFullPath();
        this.downloadUtils = new DownloadUtils(engine);
        this.executorService = Executors.newFixedThreadPool(engine.getEngineData().getDownloadManager().getDownloadThreads());
        this.loadingManager = engine.getLoadingManager();
        this.fileFetcher = new FileFetcher(engine);
        this.fileValidator = new FileValidator();
    }

    public void getFilesToDownload(boolean forceUpdate) {
        if (!isClientDataValid(this.client, this.version)) {
            Engine.getLOGGER().warn("Invalid client data: client={}, version={}", this.client, this.version);
            return;
        }

        this.loadingManager.toggleLoader();
        this.loadingManager.setLoadingText("file.gettingFiles-desc", "file.gettingFiles-title");

        fileFetcher.fetchDownloadList(client, version, getPlatformNumber())
                .thenAcceptAsync(fileAttributes -> {
                    // Processing files
                    processFileAttributes(fileAttributes, forceUpdate);
                }, executorService)
                .thenRun(() -> {
                    SwingUtilities.invokeLater(() -> {
                        // Files Processed
                        this.fileLoaderListener.filesProcessed();
                    });
                })
                .exceptionally(e -> {
                    Engine.getLOGGER().error("Error retrieving file list: {}", e.getMessage(), e);
                    SwingUtilities.invokeLater(() -> this.loadingManager.setLoadingText(e.getMessage(), "error.file"));
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
        this.loadingManager.setLoadingText("file.listBuilt-desc", "file.listBuilt-title");

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

    private boolean shouldDownloadFile(FileAttributes fileSection) {
        String localPath = fileSection.getFilename().replace(fileSection.getReplaceMask(), "");
        File localFile = new File(homeDir, localPath);
        return fileValidator.isInvalidFile(localFile, fileSection.getHash(), fileSection.getSize());
    }

    public void downloadFiles() {
        int totalFiles = fileAttributes.size();
        if (totalFiles == 0) {
            fileLoaderListener.onFilesLoaded();
        } else {
            fileLoaderListener.onDownloadStart();
        }

        totalSize = fileAttributes.stream().mapToLong(FileAttributes::getSize).sum();

        fileAttributes.forEach(file -> CompletableFuture.runAsync(() -> {
            if (isCancelled.get()) {
                return; // Stop execution if cancellation was called
            }

            this.currentFile = file;
            fileExtension = getFileExtension(file.getFilename());
            fileLoaderListener.onNewFileFound(this);


            filesDownloaded.incrementAndGet();
            if (filesDownloaded.get() == totalFiles) {
                fileLoaderListener.onFilesLoaded();
            }
        }, executorService));
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

    public String getFileType() {
        return fileExtension;
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

    public void cancel() {
        isCancelled.set(true);
        executorService.shutdownNow();
        fileLoaderListener.onCancel();
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

    public String getHomeDir() {
        return homeDir;
    }

    public String getClient() {
        return client;
    }

    public FileAttributes getCurrentFile() {
        return currentFile;
    }

    public String getVersion() {
        return version;
    }

    public FileFetcher getFileFetcher() {
        return fileFetcher;
    }

    public FileValidator getFileValidator() {
        return fileValidator;
    }

    public String getFileExtension() {
        return fileExtension;
    }
}
