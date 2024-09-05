package org.foxesworld.engine.fileLoader.fileGuard;

import org.apache.logging.log4j.Logger;
import org.foxesworld.engine.Engine;
import org.foxesworld.engine.game.GameLauncher;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@SuppressWarnings("unused")
public class FileGuard {
    private FileGuardListener fileGuardListener;
    private final List<String> checkList;
    private final Set<String> ignoreList;
    private final String[] basicIgnoreDirs = {"saves", "resourcepacks", "shaderpacks", "screenshots", "logs", "config"};
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

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void removeEmptyFolders(String dir, List<Boolean> parent_contains_files, boolean first_launch) {
        File file = new File(dir);
        if (!file.exists()) file.mkdirs();
        String[] content = file.list();

        boolean dir_contains_files = false, dir_contains_folders = false;

        assert content != null;
        for (String object : content) {
            File obj = new File(dir + File.separator + object);
            if (obj.isFile()) dir_contains_files = true;
            else if (obj.isDirectory()) dir_contains_folders = true;
        }

        if (first_launch) dir_contains_files = true;
        parent_contains_files.add(dir_contains_files);

        if (!first_launch && !dir_contains_files && !dir_contains_folders) {
            int num_folders_to_delete = 0;

            for (Boolean contains_or_not : parent_contains_files) {
                if (!contains_or_not) num_folders_to_delete++;
                else num_folders_to_delete = 0;
            }

            if (num_folders_to_delete > 1) {
                File dir_to_delete = file;
                for (int i = 0; i < num_folders_to_delete - 1; i++)
                    dir_to_delete = dir_to_delete.getParentFile();

                recursiveDelete(dir_to_delete);
                Engine.LOGGER.debug("Removed empty directory " + file);
            } else if (num_folders_to_delete == 1) {
                recursiveDelete(file);
                Engine.LOGGER.debug("Removed empty directory " + file);
            }
        }

        for (String object : content) {
            File obj = new File(dir + File.separator + object);

            if (obj.isDirectory()) {
                removeEmptyFolders(dir + File.separator + object, parent_contains_files, false);
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

    @SuppressWarnings({"unused", "ResultOfMethodCallIgnored"})
    public void recursiveDelete(File file) {
        try {
            if (!file.exists())
                return;
            if (file.isDirectory()) {
                for (File f : Objects.requireNonNull(file.listFiles()))
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