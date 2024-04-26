package org.foxesworld.engine.fileLoader;

public interface FileLoaderListener {

    void onDownloadStart();
    void onFilesRead();
    void onFilesLoaded();
    void onFileAdd(FileAttributes file);
    void onNewFileFound(FileLoader fileLoader);
}
