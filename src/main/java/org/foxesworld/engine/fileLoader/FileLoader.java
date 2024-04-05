package org.foxesworld.engine.fileLoader;

import com.google.gson.Gson;
import org.foxesworld.engine.Engine;
import org.foxesworld.engine.config.Config;
import org.foxesworld.engine.utils.LoadingManager;
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
    private final Engine engine;
    private final HTTPrequest POSTrequest;
    private final LoadingManager loadingManager;
    private final Set<String> filesToKeep = new HashSet<>();
    private final String homeDir, client, version;
    private String replaceMask;
    private final DownloadUtils downloadUtils;
    private final ExecutorService executorService;
    private final AtomicInteger filesDownloaded = new AtomicInteger(0);
    private FileLoaderListener fileLoaderListener;
    private List<FileAttributes> fileAttributes;

    @SuppressWarnings("unused")
    public FileLoader(ActionHandler actionHandler) {
        this.engine = actionHandler.getEngine();
        this.client = actionHandler.getCurrentServer().getServerName();
        this.version = actionHandler.getCurrentServer().getServerVersion();
        this.POSTrequest = engine.getPOSTrequest();
        this.homeDir = Config.getFullPath();
        this.downloadUtils = new DownloadUtils(engine);
        this.executorService = Executors.newFixedThreadPool(this.engine.getEngineData().getDownloadThreads());
        this.loadingManager = this.engine.getLoadingManager();
    }

    @SuppressWarnings("unused")
    public void getFilesToDownload() {
        loadingManager.toggleLoader();
        Map<String, String> request = new HashMap<>();
        request.put("sysRequest", "loadFiles");
        request.put("version", version);
        request.put("client", client);
        request.put("platform", String.valueOf(getPlatformNumber()));
        FileAttributes[] fileAttributes = new Gson().fromJson(POSTrequest.send(request), FileAttributes[].class);
        loadingManager.setLoadingText("file.gettingFiles-desc", "file.gettingFiles-title", 800);
        for (FileAttributes file : fileAttributes) {
            file.setReplaceMask(this.replaceMask);

            String fileWithoutMask = file.getFilename().replace(file.getReplaceMask(), "");
            String fullPath = this.homeDir +  fileWithoutMask;
            //if(md5Func.md5(fullPath).equals(file.getHash())) {
            addFileToKeep(fileWithoutMask);
            Engine.getLOGGER().debug("Adding to keep " + fullPath);
            //} else {
            //    Engine.getLOGGER().debug("Incorrect hash for " + fullPath);
            //}
        }
        Engine.getLOGGER().info("Keeping " + filesToKeep.size() + " files");
        loadingManager.setLoadingText("file.listBuilt-desc", "file.listBuilt-title", 800);
        this.fileAttributes = Stream.of(fileAttributes).filter(this::shouldDownloadFile).collect(Collectors.toList());
        fileLoaderListener.onFilesRead();
    }

    private boolean shouldDownloadFile(FileAttributes fileSection) {
        String localPath = fileSection.getFilename().replace(fileSection.getReplaceMask(), "");
        File localFile = new File(homeDir, localPath);
        return isInvalidFile(localFile, fileSection.getHash(), fileSection.getSize());
    }

    @SuppressWarnings("unused")
    public void downloadFiles() {
        int totalFiles = fileAttributes.size();
        Engine.getLOGGER().debug("~-=== Downloading " + totalFiles + " files ===-~");
        if (totalFiles == 0) {
            fileLoaderListener.onFilesLoaded();
        }

        engine.getPanelVisibility().displayPanel("loggedForm->false|newsForm->false|download->true");
        engine.getLoadingManager().toggleLoader();
        final long totalSizeFinal = fileAttributes.stream().mapToLong(FileAttributes::getSize).sum();
        fileAttributes.forEach(file -> executorService.execute(() -> {
            String localPath = file.getFilename().replace(file.getReplaceMask(), "");
            fileLoaderListener.onNewFileFound(file, localPath, totalSizeFinal);

            // Incrementing a counter
            filesDownloaded.incrementAndGet();

            // Checking if all files are loaded
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
            return 1; // Windows
        } else if (osName.contains("mac")) {
            return 2; // macOS
        } else if (osName.contains("nix") || osName.contains("nux") || osName.contains("uni")) {
            return 3; // Unix / Linux
        } else if (osName.contains("sunos")) {
            return 4; // Solaris
        } else {
            return 0; // Other or Unknown
        }
    }

    @SuppressWarnings("unused")
    public FileAttributes addJreToLoad(String jreVersion) {
        Map<String, String> request = new HashMap<>();
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
}