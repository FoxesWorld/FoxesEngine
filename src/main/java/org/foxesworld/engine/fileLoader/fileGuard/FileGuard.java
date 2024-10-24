package org.foxesworld.engine.fileLoader.fileGuard;

import org.apache.logging.log4j.Logger;
import org.foxesworld.engine.Engine;
import org.foxesworld.engine.game.GameLauncher;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

@SuppressWarnings("unused")
public class FileGuard {
    private FileGuardListener fileGuardListener;
    private final List<String> checkList;
    private final Set<String> ignoreList;
    private final String[] basicIgnoreDirs = {"saves", "resourcepacks", "shaderpacks", "screenshots", "logs", "config"};
    private final GameLauncher gameLauncher;
    private final Logger logger;
    private final AtomicInteger totalFiles = new AtomicInteger(0);
    private final AtomicInteger checkedFiles = new AtomicInteger(0);
    private final AtomicInteger filesDeleted = new AtomicInteger(0);

    @SuppressWarnings("unused")
    public FileGuard(GameLauncher gameLauncher, List<String> checkList) {
        this.gameLauncher = gameLauncher;
        this.checkList = new CopyOnWriteArrayList<>(checkList);
        this.ignoreList = new HashSet<>();
        this.buildBasicIgnoreList();
        this.logger = this.gameLauncher.getLogger();
    }

    @SuppressWarnings("unused")
    public void scanAndDeleteFilesInSubdirectories(Set<String> filesToKeep) {
        totalFiles.set(countTotalFiles());
        checkedFiles.set(0);
        filesDeleted.set(0);

        for (String dir : checkList) {
            logger.debug("Checking Dir " + dir);
            fileGuardListener.onDirCheck(dir);
            scanAndDeleteFilesRecursively(new File(dir), filesToKeep);
        }

        fileGuardListener.onFilesChecked(filesDeleted.get());
    }

    private int countTotalFiles() {
        return checkList.stream()
                .map(File::new)
                .filter(File::exists)
                .mapToInt(this::countFilesInDirectory)
                .sum();
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

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void removeEmptyFolders(String dir, List<Boolean> parentContainsFiles, boolean firstLaunch) {
        File file = new File(dir);
        if (!file.exists()) file.mkdirs();
        String[] content = file.list();

        boolean dirContainsFiles = false;
        boolean dirContainsFolders = false;

        assert content != null;
        for (String object : content) {
            File obj = new File(dir + File.separator + object);
            if (obj.isFile()) dirContainsFiles = true;
            else if (obj.isDirectory()) dirContainsFolders = true;
        }

        if (firstLaunch) dirContainsFiles = true;
        parentContainsFiles.add(dirContainsFiles);

        if (!firstLaunch && !dirContainsFiles && !dirContainsFolders) {
            int numFoldersToDelete = 0;

            for (Boolean containsOrNot : parentContainsFiles) {
                if (!containsOrNot) numFoldersToDelete++;
                else numFoldersToDelete = 0;
            }

            if (numFoldersToDelete > 1) {
                File dirToDelete = file;
                for (int i = 0; i < numFoldersToDelete - 1; i++) {
                    dirToDelete = dirToDelete.getParentFile();
                }
                recursiveDelete(dirToDelete);
                Engine.LOGGER.debug("Removed empty directory " + file);
            } else if (numFoldersToDelete == 1) {
                recursiveDelete(file);
                Engine.LOGGER.debug("Removed empty directory " + file);
            }
        }

        for (String object : content) {
            File obj = new File(dir + File.separator + object);
            if (obj.isDirectory()) {
                removeEmptyFolders(dir + File.separator + object, parentContainsFiles, false);
            }
        }
    }

    private void scanAndDeleteFilesRecursively(File directory, Set<String> filesToKeep) {
        File[] files = directory.listFiles();

        if (files != null) {
            for (File file : files) {
                if (file.isFile()) {
                    fileGuardListener.onFileCheck(file);
                    String checkPath = file.getPath().replace(this.gameLauncher.getPathBuilders().buildGameDir(), "").replace("\\", "/");
                    if (!filesToKeep.contains(checkPath) && !this.isUserConfig(file) && !isInIgnoreList(file)) {
                        boolean deleted = file.delete();
                        if (deleted) {
                            logger.debug("Deleted unlisted file: " + checkPath);
                            filesDeleted.incrementAndGet();
                        } else {
                            logger.error("Failed to delete invalid file: " + checkPath);
                        }
                    } else {
                        logger.debug(checkPath + " is checked");
                    }
                    checkedFiles.incrementAndGet();
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

    @SuppressWarnings({"unused", "ResultOfMethodCallIgnored"})
    public void recursiveDelete(File file) {
        try {
            if (!file.exists()) return;
            if (file.isDirectory()) {
                for (File f : Objects.requireNonNull(file.listFiles())) {
                    recursiveDelete(f);
                }
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
