package com.yurii.pavlenko.myassistant.flights.logic;

/** Whether the variometer makes sound, and at which rates of climb and descent the sound changes. */
public final class VarioSoundSettings {

    public static final double CLIMB_START_MIN = 0.1;
    public static final double CLIMB_START_MAX = 1.0;
    public static final double SINK_START_MIN = 0.5;
    public static final double SINK_START_MAX = 3.0;
    public static final double SINK_ALARM_MIN = 1.0;
    public static final double SINK_ALARM_MAX = 6.0;

    private final boolean enabled;
    private final double climbStart;
    private final double sinkStart;
    private final double sinkAlarm;

    /**
     * @param climbStart climb rate in m/s from which the beeping begins
     * @param sinkStart  descent rate in m/s from which the low beeping begins
     * @param sinkAlarm  descent rate in m/s from which the sound becomes continuous
     */
    public VarioSoundSettings(boolean enabled, double climbStart, double sinkStart, double sinkAlarm) {
        this.enabled = enabled;
        this.climbStart = clamp(climbStart, CLIMB_START_MIN, CLIMB_START_MAX);
        this.sinkStart = clamp(sinkStart, SINK_START_MIN, SINK_START_MAX);
        this.sinkAlarm = clamp(Math.max(sinkAlarm, this.sinkStart), SINK_ALARM_MIN, SINK_ALARM_MAX);
    }

    public static VarioSoundSettings defaults() {
        return new VarioSoundSettings(false, 0.2, 1.0, 2.0);
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    public VarioSoundSettings withEnabled(boolean on) {
        return new VarioSoundSettings(on, climbStart, sinkStart, sinkAlarm);
    }

    public boolean isEnabled() {
        return enabled;
    }

    public double getClimbStart() {
        return climbStart;
    }

    /** The descent rate, as a positive number of m/s, from which the low beeping begins. */
    public double getSinkStart() {
        return sinkStart;
    }

    /** The descent rate, as a positive number of m/s, from which the sound is continuous. */
    public double getSinkAlarm() {
        return sinkAlarm;
    }
}
