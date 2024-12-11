package org.foxesworld.engine.fileLoader.fileGuard;

import org.apache.logging.log4j.Logger;
import org.foxesworld.engine.game.GameLauncher;

import java.io.File;
import java.util.*;
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
        buildBasicIgnoreList();
        this.logger = this.gameLauncher.getLogger();
    }

    public void scanAndDeleteFilesInSubdirectories(Set<String> filesToKeep) {
        this.gameLauncher.getEngine().getExecutorServiceProvider().submitTask(() -> {
            totalFiles.set(countTotalFiles());
            resetCounters();

            logger.info("Ignoring the following directories:");
            ignoreList.forEach(dir -> logger.info("  - {}", dir));

            for (String dir : checkList) {
                logger.debug("Checking Directory: {}", dir);
                if (fileGuardListener != null) {
                    fileGuardListener.onDirCheck(dir);
                }
                scanAndDeleteFilesRecursively(new File(dir), filesToKeep);
            }

            if (fileGuardListener != null) {
                fileGuardListener.onFilesChecked(filesDeleted.get());
            }
        }, "fileGuard");
    }

    private void resetCounters() {
        checkedFiles.set(0);
        filesDeleted.set(0);
    }

    private int countTotalFiles() {
        return checkList.stream()
                .map(File::new)
                .filter(File::exists)
                .mapToInt(this::countFilesInDirectory)
                .sum();
    }

    private int countFilesInDirectory(File directory) {
        File[] files = Optional.ofNullable(directory.listFiles()).orElse(new File[0]);
        return Arrays.stream(files)
                .mapToInt(file -> file.isFile() ? 1 : countFilesInDirectory(file))
                .sum();
    }

    public void removeEmptyFolders(String dir) {
        File directory = new File(dir);
        if (!directory.exists()) return;

        String[] content = Optional.ofNullable(directory.list()).orElse(new String[0]);
        boolean dirContainsFiles = Arrays.stream(content)
                .map(object -> new File(directory, object))
                .anyMatch(File::isFile);

        if (!dirContainsFiles) {
            boolean deleted = directory.delete();
            if (deleted) {
                logger.debug("Removed empty directory: {}", directory);
            } else {
                logger.warn("Failed to delete empty directory: {}", directory);
            }
        } else {
            // Recursively check subdirectories
            Arrays.stream(content)
                    .map(object -> new File(directory, object))
                    .filter(File::isDirectory)
                    .forEach(subDir -> removeEmptyFolders(subDir.getPath()));
        }
    }

    private void scanAndDeleteFilesRecursively(File directory, Set<String> filesToKeep) {
        File[] files = Optional.ofNullable(directory.listFiles()).orElse(new File[0]);

        Arrays.stream(files).forEach(file -> {
            if (file.isFile()) {
                checkAndDeleteFile(file, filesToKeep);
            } else if (file.isDirectory()) {
                scanAndDeleteFilesRecursively(file, filesToKeep);
                // After checking files, remove empty folders
                removeEmptyFolders(file.getPath());
            }
        });
    }

    private void checkAndDeleteFile(File file, Set<String> filesToKeep) {
        if (fileGuardListener != null) {
            fileGuardListener.onFileCheck(file);
        }
        String checkPath = getRelativePath(file);
        if (!filesToKeep.contains(checkPath) && !isUserConfig(file) && !isInIgnoreList(file)) {
            if (file.delete()) {
                logger.debug("Deleted unlisted file: {}", checkPath);
                filesDeleted.incrementAndGet();
            } else {
                logger.error("Failed to delete file: {}", checkPath);
            }
        } else {
            logger.debug("{} is checked", checkPath);
        }
        checkedFiles.incrementAndGet();
    }

    private String getRelativePath(File file) {
        return file.getPath()
                .replace(gameLauncher.getPathBuilders().buildGameDir(), "")
                .replace("\\", "/");
    }

    private boolean isInIgnoreList(File file) {
        String filePath = getRelativePath(file);
        return ignoreList.stream().anyMatch(filePath::startsWith);
    }

    private void buildBasicIgnoreList() {
        Arrays.stream(basicIgnoreDirs).forEach(dir -> {
            String ignoreDirPath = (gameLauncher.getPathBuilders().buildClientDir()
                    .replace(gameLauncher.getPathBuilders().buildGameDir(), "")
                    + "/" + dir).replace("\\", "/");
            ignoreList.add(ignoreDirPath);
        });
    }


    @SuppressWarnings("unused")
    public void addIgnoreDirs(String dirs) {
        if (dirs != null) {
            Arrays.stream(dirs.split(","))
                    .map(dir -> (gameLauncher.getPathBuilders().buildClientDir()
                            .replace(gameLauncher.getPathBuilders().buildGameDir(), "")
                            + "/" + dir).replace("\\", "/"))
                    .forEach(ignoreList::add);
        }
    }

    @SuppressWarnings({"unused", "ResultOfMethodCallIgnored"})
    public void recursiveDelete(File file) {
        if (!file.exists()) return;

        if (file.isDirectory()) {
            Arrays.stream(Optional.ofNullable(file.listFiles()).orElse(new File[0]))
                    .forEach(this::recursiveDelete);
        }

        if (file.delete()) {
            logger.debug("Deleted file/directory: {}", file);
        } else {
            logger.error("Failed to delete: {}", file);
        }
    }

    private boolean isUserConfig(File file) {
        return file.getName().endsWith(".txt");
    }

    public void setFileGuardListener(FileGuardListener fileGuardListener) {
        this.fileGuardListener = fileGuardListener;
    }
}
