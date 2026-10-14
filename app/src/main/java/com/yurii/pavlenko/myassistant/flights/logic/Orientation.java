package com.yurii.pavlenko.myassistant.flights.logic;

/** Where the phone points and how it leans: the heading, the pitch and the roll, all in degrees. */
public final class Orientation {

    private static final float FLAT_LIMIT = 0.7f;
    private static final int UP_X = 6;
    private static final int UP_Y = 7;
    private static final int UP_Z = 8;

    private final double heading;
    private final double pitch;
    private final double roll;

    public Orientation(double heading, double pitch, double roll) {
        this.heading = heading;
        this.pitch = pitch;
        this.roll = roll;
    }

    /** The orientation of a phone that has no rotation sensor. */
    public static Orientation unknown() {
        return new Orientation(Double.NaN, Double.NaN, Double.NaN);
    }

    /**
     * Reads the orientation from the rotation matrix of the Android sensors. A phone lying flat is
     * taken to point with its top edge; an upright phone points with its back, where the camera looks.
     *
     * @param matrix the 3x3 rotation matrix from the device to the world, row by row
     */
    public static Orientation fromRotationMatrix(float[] matrix) {
        double upX = matrix[UP_X];
        double upY = matrix[UP_Y];
        double upZ = matrix[UP_Z];
        boolean flat = Math.abs(upZ) > FLAT_LIMIT;
        double pitch = Math.toDegrees(Math.asin(clampUnit(flat ? upY : -upZ)));
        double roll = Math.toDegrees(Math.atan2(-upX, flat ? upZ : upY));
        return new Orientation(HeadingMath.fromRotationMatrix(matrix), pitch, roll);
    }

    private static double clampUnit(double value) {
        return Math.max(-1, Math.min(1, value));
    }

    /** Degrees from magnetic north, or NaN when not known. */
    public double getHeading() {
        return heading;
    }

    /** Degrees of the nose above the horizon; negative when below. */
    public double getPitch() {
        return pitch;
    }

    /** Degrees of the lean to the right; negative when leaning left. */
    public double getRoll() {
        return roll;
    }

    public boolean isKnown() {
        return !Double.isNaN(heading);
    }
}
