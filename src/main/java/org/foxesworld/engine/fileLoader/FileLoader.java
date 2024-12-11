package org.foxesworld.engine.fileLoader;

import org.foxesworld.engine.Engine;
import org.foxesworld.engine.config.Config;
import org.foxesworld.engine.gui.ActionHandler;
import org.foxesworld.engine.gui.loadingManager.LoadingManager;
import org.foxesworld.engine.utils.Download.DownloadUtils;

import javax.swing.*;
import java.io.File;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@SuppressWarnings("unused")
public class FileLoader {

    private final Engine engine;
    private final AtomicBoolean isCancelled = new AtomicBoolean(false);
    private boolean forceUpdate = false;
    private final LoadingManager loadingManager;
    private final Set<String> filesToKeep = new HashSet<>();
    private final String homeDir;
    private final String client;
    private final String version;
    private final DownloadUtils downloadUtils;
    private final AtomicInteger filesDownloaded = new AtomicInteger(0);
    private final FileFetcher fileFetcher;
    private final FileValidator fileValidator;
    private FileLoaderListener fileLoaderListener;
    private List<FileAttributes> fileAttributes = new ArrayList<>();
    private FileAttributes currentFile;
    private long totalSize = -1;
    private String fileExtension;

    public FileLoader(ActionHandler actionHandler) {
        this.engine = actionHandler.getEngine();
        this.client = actionHandler.getCurrentServer().getServerName();
        this.version = actionHandler.getCurrentServer().getServerVersion();
        this.homeDir = Config.getFullPath();
        this.downloadUtils = new DownloadUtils(engine);
        this.loadingManager = engine.getLoadingManager();
        this.fileFetcher = new FileFetcher(engine);
        this.fileValidator = new FileValidator();
    }

    public void getFilesToDownload(boolean forceUpdate) {
        this.forceUpdate = forceUpdate;
        if (!isClientDataValid(this.client, this.version)) {
            Engine.LOGGER.warn("Invalid client data: client={}, version={}", this.client, this.version);
            return;
        }

        this.loadingManager.toggleLoader();
        loadingManager.setLoadingText("file.gettingFiles-desc", "file.gettingFiles-title");

        fileFetcher.fetchDownloadList(client, version, getPlatformNumber())
                .thenAcceptAsync(fileAttributes -> processFileAttributes(fileAttributes, forceUpdate))
                .thenRun(this::onFilesProcessed)
                .exceptionally(this::handleFileListRetrievalError);
    }

    private boolean isClientDataValid(String client, String version) {
        return client != null && !client.isEmpty() && version != null && !version.isEmpty();
    }

    private void processFileAttributes(FileAttributes[] fileAttributes, boolean forceUpdate) {
        Arrays.stream(fileAttributes).forEach(fileLoaderListener::onFileAdd);

        Engine.LOGGER.info("Keeping {} files", filesToKeep.size());
        loadingManager.setLoadingText("file.listBuilt-desc", "file.listBuilt-title");

        this.fileAttributes = forceUpdate ?
                Arrays.asList(fileAttributes) :
                filterFileAttributes(fileAttributes);

        fileLoaderListener.onFilesRead();
    }

    private List<FileAttributes> filterFileAttributes(FileAttributes[] fileAttributes) {
        return Arrays.stream(fileAttributes)
                .filter(file -> !filesToKeep.contains(file.getFilename()))
                .filter(this::shouldDownloadFile)
                .collect(Collectors.toList());
    }

    public boolean shouldDownloadFile(FileAttributes fileAttributes) {
        String localPath = fileAttributes.getFilename().replace(fileAttributes.getReplaceMask(), "");
        File localFile = new File(homeDir, localPath);
        return fileValidator.isInvalidFile(localFile, fileAttributes.getHash(), fileAttributes.getSize());
    }

    public void downloadFiles() {

        int totalFiles = fileAttributes.size();
        if (totalFiles == 0) {
            fileLoaderListener.onFilesLoaded();
            return;
        }
        this.engine.getExecutorServiceProvider().submitTask(() -> {
            fileLoaderListener.onDownloadStart();
            totalSize = fileAttributes.stream().mapToLong(FileAttributes::getSize).sum();

            fileAttributes.forEach(file -> CompletableFuture.runAsync(() -> downloadFile(file, totalFiles)));
        }, "fileLoader");
    }

    private void downloadFile(FileAttributes file, int totalFiles) {
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
    }

    private int getPlatformNumber() {
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

    public String getFileExtension(String fileName) {
        if (fileName != null && !fileName.isEmpty()) {
            int lastSlashIndex = fileName.lastIndexOf('/');
            String name = (lastSlashIndex == -1) ? fileName : fileName.substring(lastSlashIndex + 1);
            int dotIndex = name.lastIndexOf('.');
            return (dotIndex != -1 && dotIndex != name.length() - 1)
                    ? name.substring(dotIndex + 1).toLowerCase()
                    : "file";
        } else {
            return "file";
        }
    }


    public void cancel() {
        isCancelled.set(true);
        //executorService.shutdownNow();
        fileLoaderListener.onCancel();
    }

    public long getTotalSize() {
        if (totalSize == -1) {
            totalSize = fileAttributes.stream()
                    .mapToLong(file -> {
                        String localPath = file.getFilename().replace(file.getReplaceMask(), "");
                        File localFile = new File(homeDir, localPath);
                        return (localFile.exists() && localFile.length() == file.getSize()) ? 0 : file.getSize();
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

    public String getFileType() {
        return fileExtension;
    }

    public DownloadUtils getDownloadUtils() {
        return downloadUtils;
    }

    public Set<String> getFilesToKeep() {
        return filesToKeep;
    }

    public FileValidator getFileValidator() {
        return fileValidator;
    }

    public void addFileToKeep(String fileToKeep) {
        this.filesToKeep.add(fileToKeep);
    }

    public void addFileToDownload(FileAttributes fileAttributes) {
        this.fileAttributes.add(fileAttributes);
    }

    public void setLoaderListener(FileLoaderListener fileLoaderListener) {
        this.fileLoaderListener = fileLoaderListener;
    }

    private Void handleFileListRetrievalError(Throwable e) {
        Engine.LOGGER.error("Error retrieving file list: {}", e.getMessage(), e);
        SwingUtilities.invokeLater(() -> loadingManager.setLoadingText(e.getMessage(), "error.file"));
        this.getFilesToDownload(this.forceUpdate);
        return null;
    }

    private void onFilesProcessed() {
        SwingUtilities.invokeLater(fileLoaderListener::filesProcessed);
    }
}