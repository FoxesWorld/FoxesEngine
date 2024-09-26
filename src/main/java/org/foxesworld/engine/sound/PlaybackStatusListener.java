package org.foxesworld.engine.sound;

public interface PlaybackStatusListener {
    void onPlaybackStarted(String path);
    void onPlaybackStopped(String path);
    void onPlaybackProgress(String path, long microsecondPosition, long microsecondLength);
}
