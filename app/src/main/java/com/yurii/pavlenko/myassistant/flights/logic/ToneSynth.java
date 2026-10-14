package com.yurii.pavlenko.myassistant.flights.logic;

/** Turns the tone of the variometer into sound samples, keeping the wave and the beat unbroken between calls. */
public final class ToneSynth {

    private static final double BEEP_LEVEL = 0.6;
    private static final double RASP_LEVEL = 0.95;
    private static final double RAMP_SECONDS = 0.004;
    private static final double RASP_HZ = 130;
    private static final double RASP_BUZZ_HZ = 30;
    private static final double SHORT_MAX = Short.MAX_VALUE;

    private final double secondsPerSample;
    private double wave;
    private double buzz;
    private double beat;

    public ToneSynth(int sampleRate) {
        secondsPerSample = 1.0 / sampleRate;
    }

    /** Writes the next samples of the tone into the start of the buffer. */
    public void fill(short[] buffer, int count, VarioTone tone) {
        for (int i = 0; i < count; i++) {
            buffer[i] = (short) (SHORT_MAX * next(tone));
        }
    }

    private double next(VarioTone tone) {
        switch (tone.getKind()) {
            case BEEP:
                return beep(tone);
            case RASP:
                return rasp();
            default:
                beat = 0;
                return 0;
        }
    }

    private double beep(VarioTone tone) {
        if (beat >= tone.getPeriodSeconds()) {
            beat %= tone.getPeriodSeconds();
        }
        double envelope = Math.min(1, Math.min(beat, tone.getBeepSeconds() - beat) / RAMP_SECONDS);
        double sample = envelope > 0 ? BEEP_LEVEL * envelope * Math.sin(2 * Math.PI * wave) : 0;
        wave = (wave + tone.getFrequencyHz() * secondsPerSample) % 1;
        beat += secondsPerSample;
        return sample;
    }

    private double rasp() {
        double square = wave < 0.5 ? 1 : -1;
        double sample = RASP_LEVEL * square * (0.55 + 0.45 * (1 - 2 * buzz));
        wave = (wave + RASP_HZ * secondsPerSample) % 1;
        buzz = (buzz + RASP_BUZZ_HZ * secondsPerSample) % 1;
        beat = 0;
        return sample;
    }
}
