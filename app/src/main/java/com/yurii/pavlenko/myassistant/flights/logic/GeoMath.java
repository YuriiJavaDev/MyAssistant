package com.yurii.pavlenko.myassistant.flights.logic;

/** Distances on the Earth and angles on a compass. */
public final class GeoMath {

    private static final double EARTH_RADIUS_METRES = 6_371_000;
    private static final double FULL_TURN = 360;
    private static final double HALF_TURN = 180;

    private GeoMath() {
    }

    /** Great-circle distance between two points given in degrees. */
    public static double distanceMetres(double lat1, double lon1, double lat2, double lon2) {
        double phi1 = Math.toRadians(lat1);
        double phi2 = Math.toRadians(lat2);
        double deltaPhi = Math.toRadians(lat2 - lat1);
        double deltaLambda = Math.toRadians(lon2 - lon1);
        double a = Math.sin(deltaPhi / 2) * Math.sin(deltaPhi / 2)
                + Math.cos(phi1) * Math.cos(phi2) * Math.sin(deltaLambda / 2) * Math.sin(deltaLambda / 2);
        return 2 * EARTH_RADIUS_METRES * Math.asin(Math.min(1, Math.sqrt(a)));
    }

    /** An angle in degrees brought into the range from 0 up to but not including 360. */
    public static double normalizeDegrees(double degrees) {
        double result = degrees % FULL_TURN;
        return result < 0 ? result + FULL_TURN : result;
    }

    /** The shortest signed turn from one heading to another, from -180 up to 180 degrees. */
    public static double turnBetween(double from, double to) {
        double turn = normalizeDegrees(to - from);
        return turn > HALF_TURN ? turn - FULL_TURN : turn;
    }
}
