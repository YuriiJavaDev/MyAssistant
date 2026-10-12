package com.yurii.pavlenko.myassistant.steps.logic;

/** Statistics of a range of days, both ends included. */
public final class PeriodSummary {

    private final int dayCount;
    private final int daysWithSteps;
    private final long walkSteps;
    private final long runSteps;
    private final double distanceMeters;
    private final long bestDayEpoch;
    private final int bestDaySteps;

    PeriodSummary(int dayCount, int daysWithSteps, long walkSteps, long runSteps, double distanceMeters,
                  long bestDayEpoch, int bestDaySteps) {
        this.dayCount = dayCount;
        this.daysWithSteps = daysWithSteps;
        this.walkSteps = walkSteps;
        this.runSteps = runSteps;
        this.distanceMeters = distanceMeters;
        this.bestDayEpoch = bestDayEpoch;
        this.bestDaySteps = bestDaySteps;
    }

    public int getDayCount() {
        return dayCount;
    }

    public int getDaysWithSteps() {
        return daysWithSteps;
    }

    public long getWalkSteps() {
        return walkSteps;
    }

    public long getRunSteps() {
        return runSteps;
    }

    public long totalSteps() {
        return walkSteps + runSteps;
    }

    public double getDistanceMeters() {
        return distanceMeters;
    }

    /** Average over every day of the range, days without steps included. */
    public long averageStepsPerDay() {
        return dayCount == 0 ? 0 : Math.round(totalSteps() / (double) dayCount);
    }

    public double averageDistanceMetersPerDay() {
        return dayCount == 0 ? 0 : distanceMeters / dayCount;
    }

    public long getBestDayEpoch() {
        return bestDayEpoch;
    }

    public int getBestDaySteps() {
        return bestDaySteps;
    }
}
