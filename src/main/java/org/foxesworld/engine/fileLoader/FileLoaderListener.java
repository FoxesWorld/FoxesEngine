package org.foxesworld.engine.fileLoader;

public interface FileLoaderListener {
    void onFileAdd(FileAttributes file);
    void onFilesRead();
    void onFilesLoaded();
    void onDownloadStart();
    void onNewFileFound(FileLoader fileLoader);
    void filesProcessed();
    void onCancel();
}
