package com.yurii.pavlenko.myassistant.flights.logic;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/** The outlines of the land as closed rings of points, read from the compact asset made by tools/coastline. */
public final class Coastline {

    private static final int[] MAGIC = {'C', 'L', '1'};
    private static final double SCALE = 1000.0;
    private static final int VARINT_PAYLOAD_BITS = 7;
    private static final int VARINT_PAYLOAD_MASK = 0x7F;
    private static final int VARINT_MORE = 0x80;

    /** One closed outline of land, or of a lake inside land, with the box that contains it. */
    public static final class Ring {
        private final float[] longitudes;
        private final float[] latitudes;
        private final double west;
        private final double east;
        private final double south;
        private final double north;

        private Ring(float[] longitudes, float[] latitudes) {
            this.longitudes = longitudes;
            this.latitudes = latitudes;
            double minLon = Double.MAX_VALUE;
            double maxLon = -Double.MAX_VALUE;
            double minLat = Double.MAX_VALUE;
            double maxLat = -Double.MAX_VALUE;
            for (int i = 0; i < longitudes.length; i++) {
                minLon = Math.min(minLon, longitudes[i]);
                maxLon = Math.max(maxLon, longitudes[i]);
                minLat = Math.min(minLat, latitudes[i]);
                maxLat = Math.max(maxLat, latitudes[i]);
            }
            west = minLon;
            east = maxLon;
            south = minLat;
            north = maxLat;
        }

        public int size() {
            return longitudes.length;
        }

        public double longitude(int index) {
            return longitudes[index];
        }

        public double latitude(int index) {
            return latitudes[index];
        }

        /** Whether the box of the ring overlaps the given area. */
        public boolean touches(double areaWest, double areaEast, double areaSouth, double areaNorth) {
            return east >= areaWest && west <= areaEast && north >= areaSouth && south <= areaNorth;
        }
    }

    private final List<Ring> rings;

    private Coastline(List<Ring> rings) {
        this.rings = rings;
    }

    public List<Ring> getRings() {
        return rings;
    }

    /** Reads the rings; the stream is not closed. */
    public static Coastline read(InputStream in) throws IOException {
        for (int expected : MAGIC) {
            if (in.read() != expected) {
                throw new IOException("Not a coastline file");
            }
        }
        long count = readUnsigned(in);
        List<Ring> rings = new ArrayList<>((int) count);
        for (long i = 0; i < count; i++) {
            int points = (int) readUnsigned(in);
            float[] longitudes = new float[points];
            float[] latitudes = new float[points];
            long lon = 0;
            long lat = 0;
            for (int p = 0; p < points; p++) {
                lon += readSigned(in);
                lat += readSigned(in);
                longitudes[p] = (float) (lon / SCALE);
                latitudes[p] = (float) (lat / SCALE);
            }
            rings.add(new Ring(longitudes, latitudes));
        }
        return new Coastline(rings);
    }

    /** An empty coastline, for when the asset cannot be read: the map then shows only the grid. */
    public static Coastline empty() {
        return new Coastline(new ArrayList<>());
    }

    private static long readUnsigned(InputStream in) throws IOException {
        long result = 0;
        int shift = 0;
        int value;
        do {
            value = in.read();
            if (value < 0) {
                throw new IOException("Unexpected end of the coastline file");
            }
            result |= (long) (value & VARINT_PAYLOAD_MASK) << shift;
            shift += VARINT_PAYLOAD_BITS;
        } while ((value & VARINT_MORE) != 0);
        return result;
    }

    private static long readSigned(InputStream in) throws IOException {
        long raw = readUnsigned(in);
        return (raw >>> 1) ^ -(raw & 1);
    }
}
