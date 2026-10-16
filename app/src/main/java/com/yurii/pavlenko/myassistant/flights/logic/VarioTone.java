package com.yurii.pavlenko.myassistant.flights.logic;

/** The sound the variometer makes at one moment. */
public final class VarioTone {

    public enum Kind { SILENT, BEEP, RASP }

    public static final VarioTone SILENT = new VarioTone(Kind.SILENT, 0, 1, 0);

    private static final double CLIMB_FULL = 5.0;
    private static final double CLIMB_LOW_HZ = 500;
    private static final double CLIMB_HIGH_HZ = 1200;
    private static final double CLIMB_SLOW_PERIOD = 0.85;
    private static final double CLIMB_FAST_PERIOD = 0.25;
    private static final double MAX_BEEP = 0.14;
    private static final double BEEP_SHARE = 0.45;
    private static final double SINK_HZ = 220;
    private static final double SINK_PERIOD = 1.0;
    private static final double SINK_BEEP = 0.3;

    private final Kind kind;
    private final double frequencyHz;
    private final double periodSeconds;
    private final double beepSeconds;

    private VarioTone(Kind kind, double frequencyHz, double periodSeconds, double beepSeconds) {
        this.kind = kind;
        this.frequencyHz = frequencyHz;
        this.periodSeconds = periodSeconds;
        this.beepSeconds = beepSeconds;
    }

    /**
     * Climbing gives beeps that come faster and higher the stronger the lift. Sinking gives slow low
     * beeps, and a strong sink a harsh continuous sound. In between there is silence.
     */
    public static VarioTone of(double metresPerSecond, VarioSoundSettings settings) {
        if (metresPerSecond >= settings.getClimbStart()) {
            double share = Math.min(1, (metresPerSecond - settings.getClimbStart()) / (CLIMB_FULL - settings.getClimbStart()));
            double period = CLIMB_SLOW_PERIOD + (CLIMB_FAST_PERIOD - CLIMB_SLOW_PERIOD) * share;
            return new VarioTone(Kind.BEEP, CLIMB_LOW_HZ + (CLIMB_HIGH_HZ - CLIMB_LOW_HZ) * share,
                    period, Math.min(MAX_BEEP, period * BEEP_SHARE));
        }
        if (metresPerSecond <= -settings.getSinkAlarm()) {
            return new VarioTone(Kind.RASP, 0, 1, 1);
        }
        if (metresPerSecond <= -settings.getSinkStart()) {
            return new VarioTone(Kind.BEEP, SINK_HZ, SINK_PERIOD, SINK_BEEP);
        }
        return SILENT;
    }

    public Kind getKind() {
        return kind;
    }

    public double getFrequencyHz() {
        return frequencyHz;
    }

    /** Time from the start of one beep to the start of the next. */
    public double getPeriodSeconds() {
        return periodSeconds;
    }

    public double getBeepSeconds() {
        return beepSeconds;
    }
}
