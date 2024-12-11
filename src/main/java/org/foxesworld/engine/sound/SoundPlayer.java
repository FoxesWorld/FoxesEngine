package org.foxesworld.engine.sound;

import de.jarnbjo.vorbis.VorbisAudioFileReader;
import org.foxesworld.engine.Engine;

import javax.sound.sampled.*;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public class SoundPlayer implements LineListener {

    private static int UPDATE_RATE = 100;
    private final Engine engine;
    private final VorbisAudioFileReader vorbisAudioFileReader;
    private final Map<Clip, PlaybackStatusListener> clipListeners = new HashMap<>();
    private final Map<Clip, String> clipPaths = new HashMap<>();
    private final Map<Clip, Timer> clipTimers = new HashMap<>();
    private volatile int activeClipCount = 0;
    private volatile Runnable stopAllSoundsCallback;

    public SoundPlayer(Engine engine) {
        this.engine = engine;
        this.vorbisAudioFileReader = new VorbisAudioFileReader();
    }

    public void playSound(String path, boolean loop, PlaybackStatusListener listener) {
        if (Boolean.parseBoolean(String.valueOf(this.engine.getConfig().getConfig().get("enableSound")))) {
            engine.getExecutorServiceProvider().submitTask(() -> {
                try {
                    float volume;
                    InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream(path);
                    AudioInputStream audioInputStream = vorbisAudioFileReader.getAudioInputStream(inputStream);
                    Clip clip = AudioSystem.getClip();
                    clip.open(audioInputStream);
                    clip.addLineListener(this);

                    clipPaths.put(clip, path);
                    clipListeners.put(clip, listener);

                    if (path.contains("mus")) {
                        volume = Float.parseFloat(String.valueOf(this.engine.getConfig().getConfig().get("volume"))) / 100.0f - 0.15f;
                    } else {
                        volume = Float.parseFloat(String.valueOf(this.engine.getConfig().getConfig().get("volume"))) / 100.0f;
                    }
                    setVolume(clip, volume);

                    if (loop) {
                        clip.loop(Clip.LOOP_CONTINUOUSLY);
                    }

                    clip.start();

                    if (listener != null) {
                        listener.onPlaybackStarted(path);
                    }

                    startPlaybackTimer(clip, path, listener);
                } catch (IOException | LineUnavailableException | UnsupportedAudioFileException e) {
                    // Handle exception
                    e.printStackTrace();
                }
            }, "Play Sound Task");
        }
    }

    public void playSound(String path, boolean loop) {
        playSound(path, loop, null);
    }

    private void setVolume(Clip clip, float volume) {
        if (volume < 0.0f || volume > 1.0f) {
            Engine.LOGGER.error("Volume should be between 0.0 and 1.0");
            return;
        }

        FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
        float range = gainControl.getMaximum() - gainControl.getMinimum();
        float gain = (range * volume) + gainControl.getMinimum();
        gainControl.setValue(gain);
    }

    private void startPlaybackTimer(Clip clip, String path, PlaybackStatusListener listener) {
        Timer timer = new Timer(true);
        clipTimers.put(clip, timer);

        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                long microsecondPosition = clip.getMicrosecondPosition();
                long microsecondLength = clip.getMicrosecondLength();
                if (listener != null) {
                    listener.onPlaybackProgress(path, microsecondPosition, microsecondLength);
                }
            }
        }, 0, UPDATE_RATE); // Update at the specified rate
    }

    public void changeActiveVolume(float volume) {
        for (Clip clip : clipListeners.keySet()) {
            setVolume(clip, volume);
        }
    }

    public void stopAllSounds(Runnable onStopAction) {
        synchronized (clipListeners) {
            stopAllSoundsCallback = onStopAction;
            activeClipCount = 0;
            for (Clip clip : new HashSet<>(clipListeners.keySet())) {
                if (clip.isRunning()) {
                    activeClipCount++;
                    fadeOut(clip);
                }
            }
            checkAndRunCallback();
        }
    }

    public void stopAllSounds() {
        stopAllSounds(null);
    }


    private void checkAndRunCallback() {
        if (activeClipCount == 0 && stopAllSoundsCallback != null) {
            stopAllSoundsCallback.run();
            stopAllSoundsCallback = null;
        }
    }

    private void fadeOut(Clip clip) {
        new Thread(() -> {
            try {
                FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
                float minVolume = gainControl.getMinimum();
                float currentVolume = gainControl.getValue();
                while (currentVolume > minVolume) {
                    currentVolume -= 0.25f;
                    if (currentVolume < minVolume) {
                        currentVolume = minVolume;
                    }
                    gainControl.setValue(currentVolume);
                    Thread.sleep(50);
                }
                clip.stop();
                gainControl.setValue(minVolume);
                clip.close();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                synchronized (clipListeners) {
                    clipListeners.remove(clip);
                    clipPaths.remove(clip);
                    Timer timer = clipTimers.remove(clip);
                    if (timer != null) {
                        timer.cancel();
                    }
                    activeClipCount--;
                    checkAndRunCallback();
                }
            }
        }).start();
    }
    @Override
    public void update(LineEvent event) {
        Clip clip = (Clip) event.getLine();
        if (event.getType() == LineEvent.Type.STOP) {
            synchronized (clipListeners) {
                PlaybackStatusListener listener = clipListeners.remove(clip);
                String path = clipPaths.remove(clip);
                Timer timer = clipTimers.remove(clip);
                if (timer != null) {
                    timer.cancel();
                }

                if (listener != null && path != null) {
                    listener.onPlaybackStopped(path);
                }

                activeClipCount--;
                checkAndRunCallback();
            }
            clip.close();
        }
    }

    public static void setUPDATE_RATE(int rate) {
        SoundPlayer.UPDATE_RATE = rate;
    }
}