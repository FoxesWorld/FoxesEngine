package org.foxesworld.engine.utils.Download;

public interface DownloadListener {

    void onFileDownloaded(String fileName);
    void unpacking(String fileName);
    void downloading(String fileName);
    void onFileUnpacked(String file);
}
