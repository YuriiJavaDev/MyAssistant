package com.yurii.pavlenko.myassistant.flights.logic;

/** The unit in which the climb rate is shown, with the layout of the scale of the variometer. */
public enum ClimbUnit {
    METRES_PER_SECOND(1, "m/s", 1, 1, 1),
    FEET_PER_MINUTE(196.8504, "ft/min", 0, 200, 100);

    /** Number of numbered steps on each side of the variometer scale. */
    public static final int SCALE_STEPS = 8;

    private final double perMetrePerSecond;
    private final String symbol;
    private final int decimals;
    private final double scaleStep;
    private final int labelDivisor;

    ClimbUnit(double perMetrePerSecond, String symbol, int decimals, double scaleStep, int labelDivisor) {
        this.perMetrePerSecond = perMetrePerSecond;
        this.symbol = symbol;
        this.decimals = decimals;
        this.scaleStep = scaleStep;
        this.labelDivisor = labelDivisor;
    }

    public double fromMetresPerSecond(double metresPerSecond) {
        return metresPerSecond * perMetrePerSecond;
    }

    public String symbol() {
        return symbol;
    }

    /** How many decimals a reading in this unit needs. */
    public int decimals() {
        return decimals;
    }

    /** The value of one numbered step of the scale, in this unit. */
    public double scaleStep() {
        return scaleStep;
    }

    /** The number written at a step of the scale; large units are written in hundreds to keep it short. */
    public String scaleLabel(int step) {
        return String.valueOf(Math.round(scaleStep * step / labelDivisor));
    }

    /** A note for the scale, such as "x100", or an empty text when the labels are the real values. */
    public String scaleNote() {
        return labelDivisor == 1 ? "" : "x" + labelDivisor;
    }

    /** The reading at the end of the scale, in this unit. */
    public double scaleEnd() {
        return scaleStep * SCALE_STEPS;
    }
}
