package org.foxesworld.engine.fileLoader;

public interface FileLoaderListener {

    void onDownloadStart();
    void onFilesRead();
    void onFilesLoaded();
    void onFileAdd(FileAttributes file);
    void onNewFileFound(FileAttributes file, String localPath, final long totalSizeFinal);
}
