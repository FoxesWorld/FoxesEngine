package org.foxesworld.engine.fileLoader.fileGuard;

import org.apache.logging.log4j.Logger;
import org.foxesworld.engine.game.GameLauncher;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@SuppressWarnings("unused")
public class FileGuard {
    private FileGuardListener fileGuardListener;
    private final List<String> checkList;
    private final Set<String> ignoreList;
    private final String[] basicIgnoreDirs = {"saves", "resourcepacks", "shaderpacks", "logs", "config"};
    private final GameLauncher gameLauncher;
    private final Logger logger;
    private int totalFiles = 0;
    private int checkedFiles = 0;
    private int filesDeleted = 0;

    @SuppressWarnings("unused")
    public FileGuard(GameLauncher gameLauncher, List<String> checkList) {
        this.gameLauncher = gameLauncher;
        this.checkList = checkList;
        this.ignoreList = new HashSet<>();
        this.buildBasicIgnoreList();
        this.logger = this.gameLauncher.getLogger();
    }

    @SuppressWarnings("unused")
    public void scanAndDeleteFilesInSubdirectories(Set<String> filesToKeep) {
        totalFiles = countTotalFiles();
        checkedFiles = 0;
        filesDeleted = 0;

        for (String dir : checkList) {
            logger.debug("Checking Dir " + dir);
            fileGuardListener.onDirCheck(dir);
            scanAndDeleteFilesRecursively(new File(dir), filesToKeep);
        }

        fileGuardListener.onFilesChecked(filesDeleted);
    }

    private int countTotalFiles() {
        int total = 0;
        for (String dir : checkList) {
            File directory = new File(dir);
            if (directory.exists()) {
                total += countFilesInDirectory(directory);
            }
        }
        return total;
    }

    private int countFilesInDirectory(File directory) {
        File[] files = directory.listFiles();
        int count = 0;

        if (files != null) {
            for (File file : files) {
                if (file.isFile()) {
                    count++;
                } else if (file.isDirectory()) {
                    count += countFilesInDirectory(file);
                }
            }
        }

        return count;
    }

    private void scanAndDeleteFilesRecursively(File directory, Set<String> filesToKeep) {
        File[] files = directory.listFiles();

        if (files != null) {
            for (File file : files) {
                if (file.isFile()) {
                    fileGuardListener.onFileCheck(file);
                    String checkPath = file.getPath().replace(this.gameLauncher.getPathBuilders().buildGameDir(), "").replace("\\", "/");
                    if (!filesToKeep.contains(checkPath) && !this.isUserConfig(file) && !isInIgnoreList(file)){ //&& !this.isUserConfig(file) && !isInIgnoreList(file)) {
                        boolean deleted = file.delete();
                        if (deleted) {
                            logger.debug("Deleted unlisted file: " + checkPath);
                            filesDeleted++;
                        } else {
                            logger.error("Failed to delete invalid file: " + checkPath);
                        }
                    } else {
                        logger.debug(checkPath + " is checked");
                    }
                    checkedFiles++;
                } else if (file.isDirectory()) {
                    scanAndDeleteFilesRecursively(file, filesToKeep);
                }
            }
        } else {
            logger.error(directory + " is not found!");
        }
    }

    private boolean isInIgnoreList(File file) {
        String filePath = file.getPath().replace(this.gameLauncher.getPathBuilders().buildGameDir(), "").replace("\\", "/");
        for (String mask : this.ignoreList) {
            if (filePath.startsWith(mask.replace("\\", "/"))) {
                return true;
            }
        }
        return false;
    }

    private void buildBasicIgnoreList() {
        for (String dir : this.basicIgnoreDirs) {
            String thisDir = gameLauncher.getPathBuilders().buildClientDir().replace(gameLauncher.getPathBuilders().buildGameDir(), "") + File.separator + dir;
            this.ignoreList.add(thisDir);
        }
    }
    @SuppressWarnings("unused")
    public void addIgnoreDirs(String dirs) {
        if (dirs != null) {
            for (String dir : dirs.split(",")) {
                String thisDir = gameLauncher.getPathBuilders().buildClientDir().replace(gameLauncher.getPathBuilders().buildGameDir(), "") + File.separator + dir;
                this.ignoreList.add(thisDir);
            }
        }
    }

    @SuppressWarnings("unused")
    public void recursiveDelete(File file) {
        try {
            if (!file.exists())
                return;
            if (file.isDirectory()) {
                for (File f : file.listFiles())
                    recursiveDelete(f);
                file.delete();
            } else {
                file.delete();
            }
        } catch (Exception ignored) {
        }
    }

    private boolean isUserConfig(File file) {
        return file.getName().endsWith(".txt");
    }

    @SuppressWarnings("unused")
    public void setFileGuardListener(FileGuardListener fileGuardListener) {
        this.fileGuardListener = fileGuardListener;
    }
}