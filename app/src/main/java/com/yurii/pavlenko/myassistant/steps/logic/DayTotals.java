package com.yurii.pavlenko.myassistant.steps.logic;

/** Walking and running steps recorded for one calendar day. */
public final class DayTotals {

    private final long epochDay;
    private final int walkSteps;
    private final int runSteps;

    public DayTotals(long epochDay, int walkSteps, int runSteps) {
        this.epochDay = epochDay;
        this.walkSteps = walkSteps;
        this.runSteps = runSteps;
    }

    public long getEpochDay() {
        return epochDay;
    }

    public int getWalkSteps() {
        return walkSteps;
    }

    public int getRunSteps() {
        return runSteps;
    }

    public int totalSteps() {
        return walkSteps + runSteps;
    }
}
