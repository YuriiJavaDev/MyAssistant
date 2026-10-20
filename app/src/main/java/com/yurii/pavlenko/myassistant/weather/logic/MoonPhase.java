package com.yurii.pavlenko.myassistant.weather.logic;

/** Phase of the moon at a moment in time, from the mean length of the lunar month. */
public final class MoonPhase {

    /** The eight named phases, in the order of the lunar month. */
    public enum Phase {
        NEW_MOON, WAXING_CRESCENT, FIRST_QUARTER, WAXING_GIBBOUS,
        FULL_MOON, WANING_GIBBOUS, LAST_QUARTER, WANING_CRESCENT
    }

    private static final double SYNODIC_MONTH_DAYS = 29.530588853;
    private static final long MILLIS_PER_DAY = 86_400_000L;
    /** A new moon: 2000-01-06 18:14 UTC. */
    private static final long REFERENCE_NEW_MOON_MILLIS = 947_182_440_000L;

    private final double cycle;

    private MoonPhase(double cycle) {
        this.cycle = cycle;
    }

    public static MoonPhase at(long epochMillis) {
        double days = (epochMillis - REFERENCE_NEW_MOON_MILLIS) / (double) MILLIS_PER_DAY;
        double cycle = days / SYNODIC_MONTH_DAYS;
        return new MoonPhase(cycle - Math.floor(cycle));
    }

    /** Position in the lunar month: 0 is the new moon, 0.5 the full moon, and it returns to 0 at the next new moon. */
    public double cycle() {
        return cycle;
    }

    /** Share of the visible disc that is lit, from 0 to 1. */
    public double illumination() {
        return (1 - Math.cos(2 * Math.PI * cycle)) / 2;
    }

    public boolean isWaxing() {
        return cycle < 0.5;
    }

    public Phase phase() {
        Phase[] phases = Phase.values();
        int index = (int) Math.floor(cycle * phases.length + 0.5) % phases.length;
        return phases[index];
    }
}
