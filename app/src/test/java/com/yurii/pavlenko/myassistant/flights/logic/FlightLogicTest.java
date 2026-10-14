package com.yurii.pavlenko.myassistant.flights.logic;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class FlightLogicTest {

    private static FlightPoint point(long seconds, double lat, double lon, double altitude, double speed) {
        return new FlightPoint(seconds * 1000, lat, lon, altitude, speed);
    }

    @Test
    public void measuresDistancesOnTheGlobe() {
        assertEquals(111_195, GeoMath.distanceMetres(0, 0, 1, 0), 100);
        assertEquals(0, GeoMath.distanceMetres(32.0, 34.8, 32.0, 34.8), 1e-6);
        assertEquals(3_550_000, GeoMath.distanceMetres(32.0, 34.8, 51.5, -0.1), 100_000);
        assertEquals(20_015_000, GeoMath.distanceMetres(0, 0, 0, 180), 5_000);
    }

    @Test
    public void normalizesAnglesAndFindsTheShortestTurn() {
        assertEquals(350, GeoMath.normalizeDegrees(-10), 1e-9);
        assertEquals(10, GeoMath.normalizeDegrees(370), 1e-9);
        assertEquals(20, GeoMath.turnBetween(350, 10), 1e-9);
        assertEquals(-20, GeoMath.turnBetween(10, 350), 1e-9);
        assertEquals(180, GeoMath.turnBetween(0, 180), 1e-9);
    }

    @Test
    public void namesTheNearestCompassPoint() {
        assertEquals("N", Cardinal.of(0));
        assertEquals("N", Cardinal.of(359));
        assertEquals("NE", Cardinal.of(50));
        assertEquals("E", Cardinal.of(90));
        assertEquals("S", Cardinal.of(180));
        assertEquals("W", Cardinal.of(268));
        assertEquals("NW", Cardinal.of(-45));
    }

    @Test
    public void smoothsAHeadingAcrossNorth() {
        AngleSmoother smoother = new AngleSmoother(0.5);
        assertEquals(350, smoother.add(350), 1e-9);
        assertEquals(0, smoother.add(10), 1e-9);
        assertEquals(5, smoother.add(10), 1e-9);
    }

    @Test
    public void readsTheHeadingFromTheRotationMatrix() {
        float[] flatFacingNorth = {1, 0, 0, 0, 1, 0, 0, 0, 1};
        assertEquals(0, HeadingMath.fromRotationMatrix(flatFacingNorth), 1e-6);
        float[] flatFacingEast = {0, 1, 0, -1, 0, 0, 0, 0, 1};
        assertEquals(90, HeadingMath.fromRotationMatrix(flatFacingEast), 1e-6);
        float[] uprightCameraNorth = {1, 0, 0, 0, 0, -1, 0, 1, 0};
        assertEquals(0, HeadingMath.fromRotationMatrix(uprightCameraNorth), 1e-6);
        float[] uprightCameraEast = {0, 0, -1, -1, 0, 0, 0, 1, 0};
        assertEquals(90, HeadingMath.fromRotationMatrix(uprightCameraEast), 1e-6);
    }

    @Test
    public void computesClimbRateOverTheLastSeconds() {
        VerticalSpeed climb = new VerticalSpeed();
        climb.add(0, 1000);
        assertEquals(0, climb.metresPerSecond(), 1e-9);
        climb.add(1000, 1005);
        assertEquals(0, climb.metresPerSecond(), 1e-9);
        climb.add(5000, 1025);
        assertEquals(5, climb.metresPerSecond(), 1e-9);
        climb.add(20_000, 900);
        climb.add(25_000, 850);
        assertEquals(-10, climb.metresPerSecond(), 1e-9);
        climb.reset();
        assertEquals(0, climb.metresPerSecond(), 1e-9);
    }

    @Test
    public void buildsUpFlightStatistics() {
        FlightStats stats = FlightStats.empty();
        assertTrue(stats.isEmpty());
        assertEquals(0, stats.getMaxAltitude(), 1e-9);
        stats = stats.plus(point(0, 32.0, 34.8, 50, 0), 1.0);
        stats = stats.plus(point(10, 32.0, 34.801, 300, 90), 1.4);
        stats = stats.plus(point(20, 32.0, 34.802, 800, 250), 1.1);
        assertEquals(20_000, stats.getDurationMillis());
        assertEquals(800, stats.getMaxAltitude(), 1e-9);
        assertEquals(250, stats.getMaxSpeed(), 1e-9);
        assertEquals(1.4, stats.getMaxGForce(), 1e-9);
        assertEquals(188, stats.getDistanceMetres(), 5);
        assertEquals(stats.getDistanceMetres() / 20, stats.getAverageSpeed(), 1e-9);
    }

    @Test
    public void ignoresGpsNoiseWhileStanding() {
        FlightStats stats = FlightStats.empty().plus(point(0, 32.0, 34.8, 10, 0), 1)
                .plus(point(10, 32.000005, 34.800005, 10, 0), 1);
        assertEquals(0, stats.getDistanceMetres(), 1e-9);
    }

    @Test
    public void convertsUnits() {
        assertEquals(36, SpeedUnit.KILOMETRES_PER_HOUR.fromMetresPerSecond(10), 1e-9);
        assertEquals(19.43844, SpeedUnit.KNOTS.fromMetresPerSecond(10), 1e-5);
        assertEquals(10, SpeedUnit.METRES_PER_SECOND.fromMetresPerSecond(10), 1e-9);
        assertEquals(3280.84, AltitudeUnit.FEET.fromMetres(1000), 1e-6);
        assertEquals("km/h", SpeedUnit.KILOMETRES_PER_HOUR.symbol());
        assertEquals("ft", AltitudeUnit.FEET.symbol());
    }

    @Test
    public void picksRoundDialRanges() {
        assertEquals(10, NiceScale.maxFor(0), 1e-9);
        assertEquals(10, NiceScale.maxFor(9.9), 1e-9);
        assertEquals(20, NiceScale.maxFor(10.1), 1e-9);
        assertEquals(50, NiceScale.maxFor(21), 1e-9);
        assertEquals(100, NiceScale.maxFor(51), 1e-9);
        assertEquals(1000, NiceScale.maxFor(999), 1e-9);
        assertEquals(20_000, NiceScale.maxFor(12_000), 1e-9);
    }

    @Test
    public void formatsFlightTime() {
        assertEquals("0:00:00", FlightTime.format(0));
        assertEquals("0:00:59", FlightTime.format(59_999));
        assertEquals("1:05:09", FlightTime.format(3_909_000));
        assertEquals("0:00:00", FlightTime.format(-5));
    }

    @Test
    public void formatsReadingsInTheChosenUnits() {
        assertEquals("900 km/h", FlightFormat.speed(250, SpeedUnit.KILOMETRES_PER_HOUR));
        assertEquals("486 kt", FlightFormat.speed(250, SpeedUnit.KNOTS));
        assertEquals("10650 m", FlightFormat.altitude(10650, AltitudeUnit.METRES));
        assertEquals("34941 ft", FlightFormat.altitude(10650, AltitudeUnit.FEET));
        assertEquals("1.50 km", FlightFormat.distance(1500, SpeedUnit.KILOMETRES_PER_HOUR));
        assertEquals("1234.0 km", FlightFormat.distance(1_234_000, SpeedUnit.KILOMETRES_PER_HOUR));
        assertEquals("10.0 nm", FlightFormat.distance(18_520, SpeedUnit.KNOTS));
        assertEquals("+5.0 m/s", FlightFormat.climbRate(5, AltitudeUnit.METRES));
        assertEquals("-2.5 m/s", FlightFormat.climbRate(-2.5, AltitudeUnit.METRES));
        assertEquals("+984 ft/min", FlightFormat.climbRate(5, AltitudeUnit.FEET));
        assertEquals("1.37 G", FlightFormat.gForce(1.37));
    }
}
