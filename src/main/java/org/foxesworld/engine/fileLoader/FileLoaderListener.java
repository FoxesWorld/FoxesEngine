package org.foxesworld.engine.fileLoader;

public interface FileLoaderListener {

    void onFilesRead();
    void onFilesLoaded();
    void onNewFileFound(FileAttributes file, String localPath, final long totalSizeFinal);
}
