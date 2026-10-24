package com.yurii.pavlenko.myassistant.weather.logic;

/** Tells whether the sun is up at a place and moment, to within a few minutes (the NOAA approximation). */
public final class Sun {

    /** The sun's centre this far below the horizon is the moment of sunrise and sunset, with refraction. */
    private static final double HORIZON_DEGREES = -0.833;
    private static final double MILLIS_PER_DAY = 86_400_000.0;
    private static final double UNIX_EPOCH_JULIAN_DAY = 2440587.5;
    private static final double J2000 = 2451545.0;

    private Sun() {
    }

    public static boolean isUp(double latitude, double longitude, long utcMillis) {
        return elevationDegrees(latitude, longitude, utcMillis) > HORIZON_DEGREES;
    }

    static double elevationDegrees(double latitude, double longitude, long utcMillis) {
        double julianDay = utcMillis / MILLIS_PER_DAY + UNIX_EPOCH_JULIAN_DAY;
        double centuries = (julianDay - J2000) / 36525.0;
        double meanLongitude = Math.toRadians((280.46646 + centuries * 36000.76983) % 360.0);
        double meanAnomaly = Math.toRadians(357.52911 + centuries * 35999.05029);
        double centre = Math.toRadians((1.914602 - 0.004817 * centuries) * Math.sin(meanAnomaly)
                + 0.019993 * Math.sin(2 * meanAnomaly));
        double trueLongitude = meanLongitude + centre;
        double obliquity = Math.toRadians(23.439291 - 0.0130042 * centuries);
        double declination = Math.asin(Math.sin(obliquity) * Math.sin(trueLongitude));
        double y = Math.tan(obliquity / 2) * Math.tan(obliquity / 2);
        double equationOfTimeMinutes = 4 * Math.toDegrees(y * Math.sin(2 * meanLongitude)
                - 2 * 0.016708634 * Math.sin(meanAnomaly)
                + 4 * 0.016708634 * y * Math.sin(meanAnomaly) * Math.cos(2 * meanLongitude)
                - 0.5 * y * y * Math.sin(4 * meanLongitude)
                - 1.25 * 0.016708634 * 0.016708634 * Math.sin(2 * meanAnomaly));
        double minutesOfDayUtc = ((utcMillis % 86_400_000L) + 86_400_000L) % 86_400_000L / 60_000.0;
        double solarMinutes = (minutesOfDayUtc + equationOfTimeMinutes + 4 * longitude) % 1440.0;
        double hourAngle = Math.toRadians(solarMinutes / 4.0 - 180.0);
        double latitudeRadians = Math.toRadians(latitude);
        double sine = Math.sin(latitudeRadians) * Math.sin(declination)
                + Math.cos(latitudeRadians) * Math.cos(declination) * Math.cos(hourAngle);
        return Math.toDegrees(Math.asin(Math.max(-1.0, Math.min(1.0, sine))));
    }
}
