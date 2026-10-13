package com.yurii.pavlenko.myassistant.steps.logic;

/** Turns two readings of the cumulative counter into the steps taken in between. */
public final class CounterTracker {

    private CounterTracker() {
    }

    /**
     * Returns null when there is nothing to count: no earlier reading to compare with, or no new steps.
     * The counter restarts from zero after a reboot, so a smaller value means all of it is new.
     */
    public static StepBurst between(StepSample previous, StepSample current) {
        if (previous == null || current.getTimeMillis() <= previous.getTimeMillis()) {
            return null;
        }
        long steps = current.getCounter() >= previous.getCounter()
                ? current.getCounter() - previous.getCounter()
                : current.getCounter();
        if (steps <= 0) {
            return null;
        }
        return new StepBurst((int) Math.min(steps, Integer.MAX_VALUE), previous.getTimeMillis(), current.getTimeMillis());
    }
}
