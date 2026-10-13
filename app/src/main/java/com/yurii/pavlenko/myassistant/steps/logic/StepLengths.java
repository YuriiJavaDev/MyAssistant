package com.yurii.pavlenko.myassistant.steps.logic;

/** The user's step lengths in centimetres and the cadence from which steps count as running. */
public final class StepLengths {

    public static final double DEFAULT_WALK_CM = 75;
    public static final double DEFAULT_RUN_CM = 105;
    public static final int DEFAULT_RUN_CADENCE = 135;

    public static final double MIN_LENGTH_CM = 20;
    public static final double MAX_LENGTH_CM = 200;
    public static final int MIN_RUN_CADENCE = 100;
    public static final int MAX_RUN_CADENCE = 200;

    private final double walkCm;
    private final double runCm;
    private final int runCadence;

    public StepLengths(double walkCm, double runCm, int runCadence) {
        this.walkCm = walkCm;
        this.runCm = runCm;
        this.runCadence = runCadence;
    }

    public static StepLengths defaults() {
        return new StepLengths(DEFAULT_WALK_CM, DEFAULT_RUN_CM, DEFAULT_RUN_CADENCE);
    }

    public static boolean isValidLength(double cm) {
        return cm >= MIN_LENGTH_CM && cm <= MAX_LENGTH_CM;
    }

    public static boolean isValidCadence(int stepsPerMinute) {
        return stepsPerMinute >= MIN_RUN_CADENCE && stepsPerMinute <= MAX_RUN_CADENCE;
    }

    public double getWalkCm() {
        return walkCm;
    }

    public double getRunCm() {
        return runCm;
    }

    public int getRunCadence() {
        return runCadence;
    }

    public double distanceMeters(long walkSteps, long runSteps) {
        return (walkSteps * walkCm + runSteps * runCm) / 100.0;
    }
}
