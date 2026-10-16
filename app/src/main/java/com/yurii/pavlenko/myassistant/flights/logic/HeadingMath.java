package com.yurii.pavlenko.myassistant.flights.logic;

/** Works out where the phone points from its rotation matrix, as the Android sensors report it. */
public final class HeadingMath {

    private static final float FLAT_LIMIT = 0.7f;

    private HeadingMath() {
    }

    /**
     * The compass heading, in degrees from magnetic north, of the top edge of the phone when it
     * lies flat and of the back of the phone, where the camera looks, when it is held upright.
     *
     * @param matrix the 3x3 rotation matrix from the device to the world, row by row
     */
    public static double fromRotationMatrix(float[] matrix) {
        boolean flat = Math.abs(matrix[8]) > FLAT_LIMIT;
        double east = flat ? matrix[1] : -matrix[2];
        double north = flat ? matrix[4] : -matrix[5];
        return GeoMath.normalizeDegrees(Math.toDegrees(Math.atan2(east, north)));
    }
}
