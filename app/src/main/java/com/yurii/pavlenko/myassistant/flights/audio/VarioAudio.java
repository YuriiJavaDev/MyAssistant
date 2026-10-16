package com.yurii.pavlenko.myassistant.flights.audio;

import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioTrack;

import com.yurii.pavlenko.myassistant.flights.logic.ToneSynth;
import com.yurii.pavlenko.myassistant.flights.logic.VarioTone;

/** Plays the sound of the variometer on a thread of its own, following the tone it is given. */
public final class VarioAudio {

    private static final int SAMPLE_RATE = 22_050;
    private static final int CHUNK_SAMPLES = SAMPLE_RATE / 50;
    private static final int BYTES_PER_SAMPLE = 2;
    private static final int CHUNKS_IN_BUFFER = 4;

    private volatile VarioTone tone = VarioTone.SILENT;
    private Player player;

    public void setTone(VarioTone tone) {
        this.tone = tone;
    }

    public synchronized void start() {
        if (player == null) {
            player = new Player();
            new Thread(player, "vario-audio").start();
        }
    }

    public synchronized void stop() {
        if (player != null) {
            player.running = false;
            player = null;
        }
        tone = VarioTone.SILENT;
    }

    private final class Player implements Runnable {

        volatile boolean running = true;

        @Override
        public void run() {
            AudioTrack track = createTrack();
            if (track == null) {
                return;
            }
            ToneSynth synth = new ToneSynth(SAMPLE_RATE);
            short[] buffer = new short[CHUNK_SAMPLES];
            try {
                track.play();
                while (running) {
                    synth.fill(buffer, CHUNK_SAMPLES, tone);
                    track.write(buffer, 0, CHUNK_SAMPLES);
                }
                track.stop();
            } catch (IllegalStateException audioLost) {
                // The audio system went away; the variometer stays silent
            } finally {
                track.release();
            }
        }
    }

    private static AudioTrack createTrack() {
        int minimum = AudioTrack.getMinBufferSize(SAMPLE_RATE, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT);
        try {
            return new AudioTrack.Builder()
                    .setAudioAttributes(new AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build())
                    .setAudioFormat(new AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(SAMPLE_RATE)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build())
                    .setBufferSizeInBytes(Math.max(minimum, CHUNK_SAMPLES * BYTES_PER_SAMPLE * CHUNKS_IN_BUFFER))
                    .setTransferMode(AudioTrack.MODE_STREAM)
                    .build();
        } catch (UnsupportedOperationException | IllegalArgumentException cannotPlay) {
            return null;
        }
    }
}
