package org.foxesworld.engine.fileLoader;

import com.google.gson.Gson;
import org.foxesworld.engine.Engine;
import org.foxesworld.engine.config.Config;
import org.foxesworld.engine.gui.loadingManager.LoadingManager;
import org.foxesworld.engine.gui.ActionHandler;
import org.foxesworld.engine.utils.Download.DownloadUtils;
import org.foxesworld.engine.utils.HTTP.HTTPrequest;

import java.io.File;
import java.io.FileInputStream;
import java.security.MessageDigest;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@SuppressWarnings("unused")
public class FileLoader {
    private final HTTPrequest POSTrequest;
    private final LoadingManager loadingManager;
    private final Set<String> filesToKeep = new HashSet<>();
    private final String homeDir, client, version;
    private String fileExtension;
    private String replaceMask;
    private final DownloadUtils downloadUtils;
    private final ExecutorService executorService;
    private final AtomicInteger filesDownloaded = new AtomicInteger(0);
    private FileLoaderListener fileLoaderListener;
    private List<FileAttributes> fileAttributes;
    private FileAttributes currentFile;
    private long totalSize;

    @SuppressWarnings("unused")
    public FileLoader(ActionHandler actionHandler) {
        Engine engine = actionHandler.getEngine();
        this.client = actionHandler.getCurrentServer().getServerName();
        this.version = actionHandler.getCurrentServer().getServerVersion();
        this.POSTrequest = engine.getPOSTrequest();
        this.homeDir = Config.getFullPath();
        this.downloadUtils = new DownloadUtils(engine);
        this.executorService = Executors.newFixedThreadPool(engine.getEngineData().getDownloadManager().getDownloadThreads());
        this.loadingManager = engine.getLoadingManager();
    }

    @SuppressWarnings("unused")
    public void getFilesToDownload(boolean forceUpdate) {
        this.loadingManager.toggleLoader();
        FileAttributes[] fileAttributes = getDownloadList(this.client, this.version, getPlatformNumber());
        this.loadingManager.setLoadingText("file.gettingFiles-desc", "file.gettingFiles-title");
        for (FileAttributes file : fileAttributes)
            this.fileLoaderListener.onFileAdd(file);
        Engine.getLOGGER().info("Keeping " + this.filesToKeep.size() + " files");
        this.loadingManager.setLoadingText("file.listBuilt-desc", "file.listBuilt-title");
        if (forceUpdate) {
            this.fileAttributes = Arrays.asList(fileAttributes);
            Engine.getLOGGER().info("Force updating " + fileAttributes.length + " files");
        } else {
            this.fileAttributes = Stream.of(fileAttributes).filter(this::shouldDownloadFile).collect(Collectors.toList());
        }
        this.fileLoaderListener.onFilesRead();
    }

    public void forceUpdateFiles() {
        loadingManager.toggleLoader();
        FileAttributes[] fileAttributes = this.getDownloadList(client, version, getPlatformNumber());
        loadingManager.setLoadingText("file.gettingFiles-desc", "file.gettingFiles-title");
        for (FileAttributes file : fileAttributes) {
            this.fileLoaderListener.onFileAdd(file);
        }
        Engine.getLOGGER().info("Force updating " + fileAttributes.length + " files");
        loadingManager.setLoadingText("file.listBuilt-desc", "file.listBuilt-title");
        this.fileAttributes = Arrays.asList(fileAttributes); //No check - just loading
        fileLoaderListener.onFilesRead();
    }

    private FileAttributes[] getDownloadList(String client, String version, int platfom) {
        Map<String, Object> request = new HashMap<>();
        request.put("sysRequest", "loadFiles");
        request.put("version", version);
        request.put("client", client);
        request.put("platform", String.valueOf(platfom));
        return new Gson().fromJson(POSTrequest.send(request), FileAttributes[].class);
    }

    private boolean shouldDownloadFile(FileAttributes fileSection) {
        String localPath = fileSection.getFilename().replace(fileSection.getReplaceMask(), "");
        File localFile = new File(homeDir, localPath);
        return isInvalidFile(localFile, fileSection.getHash(), fileSection.getSize());
    }

    @SuppressWarnings("unused")
    public void downloadFiles() {
        int totalFiles = fileAttributes.size();
        if (totalFiles == 0) {
            fileLoaderListener.onFilesLoaded();
        } else {
            fileLoaderListener.onDownloadStart();
        }

        totalSize = fileAttributes.stream().mapToLong(FileAttributes::getSize).sum() - this.getTotalSizeAlreadyDownloaded();
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

    public long getTotalSizeAlreadyDownloaded() {
        long totalSize = 0;
        for (String file : filesToKeep) {
            File fileObj = new File(homeDir + file);
            if (fileObj.exists()) {
                totalSize += fileObj.length();
            }
        }
        return totalSize;
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

    @SuppressWarnings("unused")
    public FileAttributes addJreToLoad(String jreVersion) {
        Map<String, Object> request = new HashMap<>();
        request.put("sysRequest", "getJre");
        request.put("jreVersion", jreVersion);
        FileAttributes jreFile = new Gson().fromJson(POSTrequest.send(request), FileAttributes.class);
        jreFile.setReplaceMask(this.replaceMask);
        return jreFile;
    }

    @SuppressWarnings("unused")
    public void setLoaderListener(FileLoaderListener fileLoaderListener) {
        this.fileLoaderListener = fileLoaderListener;
    }

    @SuppressWarnings("unused")
    public DownloadUtils getDownloadUtils() {
        return downloadUtils;
    }

    public Set<String> getFilesToKeep() {
        return filesToKeep;
    }

    public void addFileToKeep(String fileToKeep) {
        this.filesToKeep.add(fileToKeep);
    }

    @SuppressWarnings("unused")
    public void addFileToDownload(FileAttributes fileAttributes) {
        this.fileAttributes.add(fileAttributes);
    }

    @SuppressWarnings("unused")
    public void setReplaceMask(String replaceMask) {
        this.replaceMask = replaceMask;
    }

    @SuppressWarnings("unused")
    public String getHomeDir() {
        return homeDir;
    }

    @SuppressWarnings("unused")
    public String getFileType() {
        return fileExtension;
    }

    public FileAttributes getCurrentFile() {
        return currentFile;
    }

    public long getTotalSize() {
        return totalSize;
    }
}