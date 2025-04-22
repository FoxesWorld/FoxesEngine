package org.foxesworld.engine.fileLoader;

import org.foxesworld.engine.Engine;

import javax.swing.*;
import java.io.File;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * Абстрактный базовый класс для загрузчиков файлов, реализующий общую логику.
 */
public abstract class AbstractFileLoader {

    protected final Engine engine;
    protected final ILoadingManager loadingManager;
    protected final IFileFetcher fileFetcher;
    protected final IFileValidator fileValidator;
    protected final IDownloadUtils downloadUtils;

    protected final AtomicBoolean isCancelled = new AtomicBoolean(false);
    protected final Set<String> filesToKeep = new HashSet<>();
    protected Set<FileAttributes> fileAttributes = new HashSet<>();

    protected String homeDir;
    protected String client;
    protected String version;
    protected IFileLoaderListener fileLoaderListener;

    protected FileAttributes currentFile;
    protected long totalSize = -1;
    protected String fileExtension;
    protected final AtomicInteger filesDownloaded = new AtomicInteger(0);
    protected boolean forceUpdate = false;

    public AbstractFileLoader(Engine engine,
                              ILoadingManager loadingManager,
                              IFileFetcher fileFetcher,
                              IFileValidator fileValidator,
                              IDownloadUtils downloadUtils,
                              String homeDir,
                              String client,
                              String version) {
        this.engine = engine;
        this.loadingManager = loadingManager;
        this.fileFetcher = fileFetcher;
        this.fileValidator = fileValidator;
        this.downloadUtils = downloadUtils;
        this.homeDir = homeDir.endsWith(File.separator) ? homeDir : homeDir + File.separator;
        this.client = client;
        this.version = version;
    }

    /**
     * Запуск процесса получения списка файлов для загрузки.
     *
     * @param forceUpdate принудительное обновление списка
     */
    public void getFilesToDownload(boolean forceUpdate) {
        this.forceUpdate = forceUpdate;
        if (!isClientDataValid(client, version)) {
            Engine.LOGGER.warn("Invalid client data: client={}, version={}", client, version);
            return;
        }

        loadingManager.toggleVisibility();
        loadingManager.setLoadingText("file.gettingFiles-desc", "file.gettingFiles-title");

        fileFetcher.fetchDownloadList(client, version, getPlatformNumber())
                .thenAcceptAsync(attributes -> processFileAttributes(attributes, forceUpdate))
                .thenRun(this::onFilesProcessed)
                .exceptionally(this::handleFileListRetrievalError);
    }

    protected boolean isClientDataValid(String client, String version) {
        return client != null && !client.isEmpty() && version != null && !version.isEmpty();
    }

    protected void processFileAttributes(FileAttributes[] attributes, boolean forceUpdate) {
        // Уведомляем слушателя для каждого файла
        for (FileAttributes attribute : attributes) {
            fileLoaderListener.onFileAdd(attribute);
        }

        Engine.LOGGER.info("Keeping {} files", filesToKeep.size());
        loadingManager.setLoadingText("file.listBuilt-desc", "file.listBuilt-title");

        if (forceUpdate) {
            this.fileAttributes = new HashSet<>(java.util.Arrays.asList(attributes));
        } else {
            this.fileAttributes = filterFileAttributes(attributes);
        }

        fileLoaderListener.onFilesRead();
    }

    protected HashSet<FileAttributes> filterFileAttributes(FileAttributes[] attributes) {
        return java.util.Arrays.stream(attributes)
                .filter(attr -> !filesToKeep.contains(attr.getFilename()))
                .filter(this::shouldDownloadFile)
                .collect(Collectors.toCollection(HashSet::new));
    }

    public boolean shouldDownloadFile(FileAttributes attribute) {
        String localPath = attribute.getFilename().replace(attribute.getReplaceMask(), "");
        File localFile = new File(homeDir, localPath);
        return fileValidator.isInvalidFile(localFile, attribute.getHash(), attribute.getSize());
    }

    /**
     * Запуск процесса загрузки файлов.
     */
    public void downloadFiles() {
        int totalFiles = fileAttributes.size();
        if (totalFiles == 0) {
            fileLoaderListener.onFilesLoaded();
            return;
        }
        // Перед запуском загрузки устанавливаем общее количество байт для утилит загрузки
        totalSize = calculateTotalSize();
        downloadUtils.setTotalSize(totalSize);

        fileLoaderListener.onDownloadStart();
        // Асинхронно обрабатываем каждый файл
        fileAttributes.forEach(attribute ->
                CompletableFuture.runAsync(() -> downloadFile(attribute, totalFiles))
        );
    }

    protected void downloadFile(FileAttributes attribute, int totalFiles) {
        if (isCancelled.get()) {
            return;
        }
        this.currentFile = attribute;
        fileExtension = getFileExtension(attribute.getFilename());
        fileLoaderListener.onNewFileFound(this);

        filesDownloaded.incrementAndGet();
        if (filesDownloaded.get() == totalFiles) {
            fileLoaderListener.onFilesLoaded();
        }
    }

    protected int getPlatformNumber() {
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

    protected String getFileExtension(String fileName) {
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
        fileLoaderListener.onCancel();
    }

    protected long calculateTotalSize() {
        if (totalSize == -1) {
            totalSize = fileAttributes.stream()
                    .mapToLong(attribute -> {
                        String localPath = attribute.getFilename().replace(attribute.getReplaceMask(), "");
                        File localFile = new File(homeDir, localPath);
                        return (localFile.exists() && !fileValidator.isInvalidFile(localFile, attribute.getHash(), attribute.getSize()))
                                ? 0 : attribute.getSize();
                    }).sum();
        }
        return totalSize;
    }

    // Геттеры для использования в слушателях и наследниках
    public String getHomeDir() { return homeDir; }
    public String getClient() { return client; }
    public String getVersion() { return version; }
    public FileAttributes getCurrentFile() { return currentFile; }
    public String getFileExtension() { return fileExtension; }
    public IDownloadUtils getDownloadUtils() { return downloadUtils; }
    public Set<String> getFilesToKeep() { return filesToKeep; }
    public Set<FileAttributes> getFileAttributes() { return fileAttributes; }
    public Engine getEngine() { return engine; }

    public void addFileToKeep(String fileName) {
        this.filesToKeep.add(fileName);
    }

    public void addFileToDownload(FileAttributes attribute) {
        this.fileAttributes.add(attribute);
    }

    public void setLoaderListener(IFileLoaderListener listener) {
        this.fileLoaderListener = listener;
    }

    // Обработка ошибок получения списка файлов.
    protected Void handleFileListRetrievalError(Throwable e) {
        Engine.LOGGER.error("Error retrieving file list: {}", e.getMessage(), e);
        SwingUtilities.invokeLater(() -> loadingManager.setLoadingText(e.getMessage(), "error.file"));
        // Можно реализовать повторную попытку или иной механизм обработки
        getFilesToDownload(forceUpdate);
        return null;
    }

    protected void onFilesProcessed() {
        SwingUtilities.invokeLater(fileLoaderListener::filesProcessed);
    }
}
