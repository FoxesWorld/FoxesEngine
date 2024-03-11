package org.foxesworld.engine.sound;

import de.jarnbjo.vorbis.VorbisAudioFileReader;
import org.foxesworld.engine.Engine;

import javax.sound.sampled.*;
import javax.swing.*;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class SoundPlayer {

    private final Engine engine;
    private boolean enabled;
    private float volume;
    private final VorbisAudioFileReader vorbisAudioFileReader;
    private final List<Clip> activeClips = new ArrayList<>();

    public SoundPlayer(Engine engine, boolean enabled, float volume) {
        this.engine = engine;
        this.enabled = enabled;
        this.volume = volume;
        vorbisAudioFileReader = new VorbisAudioFileReader();
    }

    public void playSound(String path, boolean loop) {
        if (this.enabled) {
            try {
                InputStream inputStream = SoundPlayer.class.getClassLoader().getResourceAsStream(path);
                AudioInputStream audioInputStream = vorbisAudioFileReader.getAudioInputStream(inputStream);
                Clip clip = AudioSystem.getClip();
                clip.open(audioInputStream);
                setVolume(clip, this.volume / 100.0f);

                if (loop) {
                    clip.loop(Clip.LOOP_CONTINUOUSLY);
                }

                clip.start();
                activeClips.add(clip);
            } catch (IOException | LineUnavailableException | UnsupportedAudioFileException e) {
                e.printStackTrace();
            }
        }
    }

    private void setVolume(Clip clip, float volume) {
        if (volume < 0.0f || volume > 1.0f) {
            throw new IllegalArgumentException("Volume should be between 0.0 and 1.0");
        }

        FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
        float range = gainControl.getMaximum() - gainControl.getMinimum();
        float gain = (range * volume) + gainControl.getMinimum();
        gainControl.setValue(gain);
    }

    public void changeActiveVolume(float volume) {
        for (Clip clip : activeClips) {
            setVolume(clip, volume);
        }
    }


    public void stopAllSounds() {
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                for (Clip clip : activeClips) {
                    if (clip.isRunning()) {
                        fadeOut(clip);
                    }
                }
                activeClips.clear();
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
        activeClips.remove(clip);
    }
}
