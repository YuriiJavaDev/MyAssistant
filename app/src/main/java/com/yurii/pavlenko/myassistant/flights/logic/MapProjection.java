package com.yurii.pavlenko.myassistant.flights.logic;

/**
 * Places longitudes and latitudes on a flat map of a given size. Longitudes are narrowed by the cosine
 * of the middle latitude, which keeps shapes right over the span of a flight.
 */
public final class MapProjection {

    private static final double METRES_PER_DEGREE = 111_320;
    private static final double MAX_LATITUDE = 85;

    private final double centerLon;
    private final double centerLat;
    private final double cosLat;
    private final double pixelsPerDegree;
    private final double width;
    private final double height;

    private MapProjection(double centerLon, double centerLat, double pixelsPerDegree, double width, double height) {
        this.centerLon = centerLon;
        this.centerLat = centerLat;
        this.cosLat = Math.cos(Math.toRadians(Math.min(MAX_LATITUDE, Math.abs(centerLat))));
        this.pixelsPerDegree = pixelsPerDegree;
        this.width = width;
        this.height = height;
    }

    /**
     * A map that shows the whole area with some room around it.
     *
     * @param padding        the share of the area added on every side
     * @param minSpanMetres  the smallest width and height shown, so that a route that stands still is not magnified
     */
    public static MapProjection fit(double minLat, double maxLat, double minLon, double maxLon,
                                    double width, double height, double padding, double minSpanMetres) {
        double centerLat = (minLat + maxLat) / 2;
        double centerLon = (minLon + maxLon) / 2;
        double cos = Math.cos(Math.toRadians(Math.min(MAX_LATITUDE, Math.abs(centerLat))));
        double minSpanDegrees = minSpanMetres / METRES_PER_DEGREE;
        double spanLat = Math.max(maxLat - minLat, minSpanDegrees) * (1 + 2 * padding);
        double spanLon = Math.max((maxLon - minLon) * cos, minSpanDegrees) * (1 + 2 * padding);
        double pixelsPerDegree = Math.min(width / spanLon, height / spanLat);
        return new MapProjection(centerLon, centerLat, pixelsPerDegree, width, height);
    }

    public double x(double longitude) {
        return width / 2 + (longitude - centerLon) * cosLat * pixelsPerDegree;
    }

    public double y(double latitude) {
        return height / 2 - (latitude - centerLat) * pixelsPerDegree;
    }

    public double longitudeAt(double x) {
        return centerLon + (x - width / 2) / (cosLat * pixelsPerDegree);
    }

    public double latitudeAt(double y) {
        return centerLat - (y - height / 2) / pixelsPerDegree;
    }

    public double getWest() {
        return longitudeAt(0);
    }

    public double getEast() {
        return longitudeAt(width);
    }

    public double getSouth() {
        return latitudeAt(height);
    }

    public double getNorth() {
        return latitudeAt(0);
    }

    /** How many metres one pixel of the map stands for. */
    public double metresPerPixel() {
        return METRES_PER_DEGREE / pixelsPerDegree;
    }
}
