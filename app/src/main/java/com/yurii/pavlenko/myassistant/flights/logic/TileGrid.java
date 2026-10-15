package com.yurii.pavlenko.myassistant.flights.logic;

import java.util.ArrayList;
import java.util.List;

/** Chooses the web map tiles (the usual 256-pixel Web Mercator ones) that cover an area at a wanted sharpness. */
public final class TileGrid {

    public static final int TILE_SIZE = 256;
    public static final int MAX_ZOOM = 18;

    private static final double EQUATOR_METRES_PER_PIXEL = 156_543.03392;
    private static final double MAX_LATITUDE = 85.0511;

    private TileGrid() {
    }

    public static final class Tile {
        private final int zoom;
        private final int x;
        private final int y;

        Tile(int zoom, int x, int y) {
            this.zoom = zoom;
            this.x = x;
            this.y = y;
        }

        public int getZoom() { return zoom; }
        public int getX() { return x; }
        public int getY() { return y; }

        public double getWest() { return longitude(x, zoom); }
        public double getEast() { return longitude(x + 1, zoom); }
        public double getNorth() { return latitude(y, zoom); }
        public double getSouth() { return latitude(y + 1, zoom); }
    }

    /**
     * @param metresPerPoint how many metres one point of the printed map stands for
     * @param tilePixelsPerPoint how many tile pixels there should be for each printed point
     * @param maxTiles the most tiles to use; a lower zoom is taken when the area needs more
     */
    public static List<Tile> cover(double west, double east, double south, double north,
                                   double metresPerPoint, double tilePixelsPerPoint, int maxTiles) {
        double middle = Math.min(MAX_LATITUDE, Math.abs((south + north) / 2));
        double wanted = Math.log(EQUATOR_METRES_PER_PIXEL * Math.cos(Math.toRadians(middle)) * tilePixelsPerPoint
                / metresPerPoint) / Math.log(2);
        int zoom = (int) Math.max(0, Math.min(MAX_ZOOM, Math.ceil(wanted)));
        while (zoom > 0 && count(west, east, south, north, zoom) > maxTiles) {
            zoom--;
        }
        int firstX = tileX(west, zoom);
        int lastX = tileX(east, zoom);
        int firstY = tileY(north, zoom);
        int lastY = tileY(south, zoom);
        List<Tile> tiles = new ArrayList<>();
        for (int y = firstY; y <= lastY; y++) {
            for (int x = firstX; x <= lastX; x++) {
                tiles.add(new Tile(zoom, x, y));
            }
        }
        return tiles;
    }

    private static long count(double west, double east, double south, double north, int zoom) {
        return (long) (tileX(east, zoom) - tileX(west, zoom) + 1) * (tileY(south, zoom) - tileY(north, zoom) + 1);
    }

    static int tileX(double longitude, int zoom) {
        return clamp((int) Math.floor((longitude + 180) / 360 * (1 << zoom)), zoom);
    }

    static int tileY(double latitude, int zoom) {
        double radians = Math.toRadians(Math.max(-MAX_LATITUDE, Math.min(MAX_LATITUDE, latitude)));
        double mercator = Math.log(Math.tan(radians) + 1 / Math.cos(radians));
        return clamp((int) Math.floor((1 - mercator / Math.PI) / 2 * (1 << zoom)), zoom);
    }

    private static int clamp(int index, int zoom) {
        return Math.max(0, Math.min((1 << zoom) - 1, index));
    }

    private static double longitude(int x, int zoom) {
        return x / (double) (1 << zoom) * 360 - 180;
    }

    private static double latitude(int y, int zoom) {
        return Math.toDegrees(Math.atan(Math.sinh(Math.PI * (1 - 2.0 * y / (1 << zoom)))));
    }
}
