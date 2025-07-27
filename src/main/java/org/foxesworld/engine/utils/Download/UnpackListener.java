package org.foxesworld.engine.utils.Download;

import java.io.File;

public interface UnpackListener {
    void unpackingStart(int totalFiles, File archive);
    void unpackProgress(int percent, String fileName);
    void onFileUnpacked(String fileName);
}