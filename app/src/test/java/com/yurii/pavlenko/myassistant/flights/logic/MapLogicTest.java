package com.yurii.pavlenko.myassistant.flights.logic;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.junit.Test;

public class MapLogicTest {

    @Test
    public void fitsTheRouteInsideTheMapKeepingItsShape() {
        MapProjection projection = MapProjection.fit(32.0, 32.1, 34.8, 34.9, 400, 300, 0.1, 100);
        assertTrue(projection.x(34.8) > 0 && projection.x(34.9) < 400);
        assertTrue(projection.y(32.1) > 0 && projection.y(32.0) < 300);
        assertEquals(200, (projection.x(34.8) + projection.x(34.9)) / 2, 1e-6);
        assertEquals(150, (projection.y(32.0) + projection.y(32.1)) / 2, 1e-6);
        double eastWest = (projection.x(34.9) - projection.x(34.8)) / (0.1 * Math.cos(Math.toRadians(32.05)));
        double northSouth = (projection.y(32.0) - projection.y(32.1)) / 0.1;
        assertEquals(eastWest, northSouth, 1e-6);
        assertEquals(34.8, projection.longitudeAt(projection.x(34.8)), 1e-9);
        assertEquals(32.1, projection.latitudeAt(projection.y(32.1)), 1e-9);
    }

    @Test
    public void doesNotMagnifyARouteThatStandsStill() {
        MapProjection projection = MapProjection.fit(32.0, 32.0, 34.8, 34.8, 400, 400, 0.1, 600);
        assertEquals(720, projection.metresPerPixel() * 400, 5);
        assertEquals(400 / 2.0, projection.x(34.8), 1e-6);
    }

    @Test
    public void roundsToNiceNumbers() {
        assertEquals(1, NiceNumbers.roundUp(0.9), 1e-9);
        assertEquals(2, NiceNumbers.roundUp(1.1), 1e-9);
        assertEquals(5, NiceNumbers.roundUp(3.4), 1e-9);
        assertEquals(0.05, NiceNumbers.roundUp(0.031), 1e-12);
        assertEquals(50, NiceNumbers.roundUp(50), 1e-9);
        assertEquals(2, NiceNumbers.roundDown(2.9), 1e-9);
        assertEquals(5, NiceNumbers.roundDown(7), 1e-9);
        assertEquals(0.5, NiceNumbers.roundDown(0.7), 1e-12);
        List<Double> ticks = NiceNumbers.ticks(0, 1850, 4);
        assertEquals(0, ticks.get(0), 1e-9);
        assertEquals(1500, ticks.get(ticks.size() - 1), 1e-9);
        assertEquals(1, NiceNumbers.ticks(5, 5, 4).size());
    }

    @Test
    public void clipsAPolygonToTheRectangle() {
        double[] square = {-10, -10, 10, -10, 10, 10, -10, 10};
        double[] clipped = PolygonClipper.clip(square, 0, 0, 5, 5);
        assertEquals(8, clipped.length);
        for (int i = 0; i < clipped.length; i++) {
            assertTrue(clipped[i] >= 0 && clipped[i] <= 5);
        }
        assertEquals(0, PolygonClipper.clip(new double[]{20, 20, 30, 20, 30, 30}, 0, 0, 5, 5).length);
        double[] inside = {1, 1, 4, 1, 4, 4};
        assertEquals(6, PolygonClipper.clip(inside, 0, 0, 5, 5).length);
    }

    private static void varint(ByteArrayOutputStream out, long value, boolean signed) {
        long number = signed ? (value << 1) ^ (value >> 63) : value;
        while (number > 0x7F) {
            out.write((int) ((number & 0x7F) | 0x80));
            number >>>= 7;
        }
        out.write((int) number);
    }

    @Test
    public void readsTheCoastlineFormat() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        out.write(new byte[]{'C', 'L', '1'});
        varint(out, 1, false);
        varint(out, 3, false);
        long[][] points = {{34800, 32000}, {34900, 32000}, {34900, 32100}};
        long lon = 0;
        long lat = 0;
        for (long[] point : points) {
            varint(out, point[0] - lon, true);
            varint(out, point[1] - lat, true);
            lon = point[0];
            lat = point[1];
        }
        Coastline coastline = Coastline.read(new ByteArrayInputStream(out.toByteArray()));
        assertEquals(1, coastline.getRings().size());
        Coastline.Ring ring = coastline.getRings().get(0);
        assertEquals(3, ring.size());
        assertEquals(34.9, ring.longitude(2), 1e-4);
        assertEquals(32.1, ring.latitude(2), 1e-4);
        assertTrue(ring.touches(34.85, 35, 32.05, 32.2));
        assertTrue(!ring.touches(36, 37, 32, 33));
    }

    @Test
    public void rejectsAFileThatIsNotACoastline() {
        boolean rejected = false;
        try {
            Coastline.read(new ByteArrayInputStream(new byte[]{1, 2, 3, 4}));
        } catch (IOException expected) {
            rejected = true;
        }
        assertTrue(rejected);
    }

    @Test
    public void readsTheBundledCoastline() throws IOException {
        try (InputStream in = new FileInputStream("src/main/assets/coastline.bin")) {
            Coastline coastline = Coastline.read(in);
            assertTrue(coastline.getRings().size() > 5000);
            int near = 0;
            for (Coastline.Ring ring : coastline.getRings()) {
                if (ring.touches(34.7, 34.9, 31.9, 32.2)) {
                    near++;
                }
            }
            assertTrue(near > 0);
        }
    }

    @Test
    public void tracksTheBestClimbAndWorstSink() {
        FlightStats stats = FlightStats.empty()
                .plus(new FlightPoint(0, 32, 34.8, 100, 5, 0.5), 1)
                .plus(new FlightPoint(1000, 32, 34.8, 103, 5, 3.2), 1)
                .plus(new FlightPoint(2000, 32, 34.8, 98, 5, -4.1), 1)
                .plus(new FlightPoint(3000, 32, 34.8, 97, 5, -1.0), 1);
        assertEquals(3.2, stats.getMaxClimb(), 1e-9);
        assertEquals(-4.1, stats.getMaxSink(), 1e-9);
        assertEquals(0, FlightStats.empty().getMaxSink(), 1e-9);
    }
}
