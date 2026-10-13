package com.yurii.pavlenko.myassistant.steps.logic;

/** Steps taken between two counter readings. */
public final class StepBurst {

    private final int steps;
    private final long fromMillis;
    private final long toMillis;

    public StepBurst(int steps, long fromMillis, long toMillis) {
        this.steps = steps;
        this.fromMillis = fromMillis;
        this.toMillis = toMillis;
    }

    public int getSteps() {
        return steps;
    }

    public long getFromMillis() {
        return fromMillis;
    }

    public long getToMillis() {
        return toMillis;
    }

    /** Average steps per minute over the whole interval, idle time included. */
    public double cadence() {
        double minutes = (toMillis - fromMillis) / 60_000.0;
        return minutes <= 0 ? 0 : steps / minutes;
    }
}
