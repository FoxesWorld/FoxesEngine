package org.foxesworld.engine.sound;

import de.jarnbjo.vorbis.VorbisAudioFileReader;
import org.foxesworld.engine.Engine;

import javax.sound.sampled.*;
import javax.swing.*;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.Timer;

public class SoundPlayer implements LineListener {

    private final Engine engine;
    private final VorbisAudioFileReader vorbisAudioFileReader;
    private final Map<Clip, PlaybackStatusListener> clipListeners = new HashMap<>();
    private final Map<Clip, String> clipPaths = new HashMap<>();
    private final Map<Clip, Timer> clipTimers = new HashMap<>();

    public SoundPlayer(Engine engine) {
        this.engine = engine;
        vorbisAudioFileReader = new VorbisAudioFileReader();
    }

    public void playSound(String path, boolean loop, PlaybackStatusListener listener) {
        if (Boolean.parseBoolean(String.valueOf(this.engine.getConfig().getCONFIG().get("enableSound")))) {
            float volume;
            try {
                InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream(path);
                AudioInputStream audioInputStream = vorbisAudioFileReader.getAudioInputStream(inputStream);
                Clip clip = AudioSystem.getClip();
                clip.open(audioInputStream);
                clip.addLineListener(this);

                clipPaths.put(clip, path);
                clipListeners.put(clip, listener);

                if (path.contains("mus")) {
                    volume = Float.parseFloat(String.valueOf(this.engine.getConfig().getCONFIG().get("volume"))) / 100.0f - 0.15f;
                } else {
                    volume = Float.parseFloat(String.valueOf(this.engine.getConfig().getCONFIG().get("volume"))) / 100.0f;
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
                // e.printStackTrace();
            }
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
        }, 0, 1000); // Update every second
    }

    @SuppressWarnings("unused")
    public void changeActiveVolume(float volume) {
        for (Clip clip : clipListeners.keySet()) {
            setVolume(clip, volume);
        }
    }

    @SuppressWarnings("unused")
    public void stopAllSounds() {
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                for (Clip clip : clipListeners.keySet()) {
                    if (clip.isRunning()) {
                        fadeOut(clip);
                    }
                }
                clipListeners.clear();
                return null;
            }
        };
        worker.execute();
    }

    private void fadeOut(Clip clip) {
        FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
        float currentVolume = gainControl.getValue();
        while (currentVolume > -80.0f) {
            currentVolume -= 0.25f;
            gainControl.setValue(currentVolume);
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        clip.stop();
        gainControl.setValue(0.0f);
        clipListeners.remove(clip);
        clipPaths.remove(clip);
        Timer timer = clipTimers.remove(clip);
        if (timer != null) {
            timer.cancel();
        }
    }

    @Override
    public void update(LineEvent event) {
        Clip clip = (Clip) event.getLine();
        if (event.getType() == LineEvent.Type.STOP) {
            clip.close();

            PlaybackStatusListener listener = clipListeners.remove(clip);
            String path = clipPaths.remove(clip);
            Timer timer = clipTimers.remove(clip);
            if (timer != null) {
                timer.cancel();
            }

            if (listener != null && path != null) {
                listener.onPlaybackStopped(path);
            }
        }
    }
}