package com.yurii.pavlenko.myassistant.steps.logic;

/** One reading of the phone's cumulative step counter, which counts steps since the last reboot. */
public final class StepSample {

    private final long counter;
    private final long timeMillis;

    public StepSample(long counter, long timeMillis) {
        this.counter = counter;
        this.timeMillis = timeMillis;
    }

    public long getCounter() {
        return counter;
    }

    public long getTimeMillis() {
        return timeMillis;
    }
}
