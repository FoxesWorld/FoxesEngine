package org.foxesworld.engine.utils.Download;

public interface DownloadListener {

    void onFileDownloaded();
    void onFileUnpacked(String file);
}
