package org.foxesworld.engine.utils.Download;

public interface UnpackListener {
    void unpacking(String fileName);
    void onFileUnpacked(String fileName);
}