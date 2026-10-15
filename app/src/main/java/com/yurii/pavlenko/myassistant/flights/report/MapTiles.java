package com.yurii.pavlenko.myassistant.flights.report;

/** The sources of the layers of a report map: the photo, the names of places over it and the names of roads. */
final class MapTiles {

    private final TileSource imagery;
    private final TileSource places;
    private final TileSource roads;

    MapTiles(TileSource imagery, TileSource places, TileSource roads) {
        this.imagery = imagery;
        this.places = places;
        this.roads = roads;
    }

    static MapTiles esri() {
        return new MapTiles(EsriTiles.imagery(), EsriTiles.places(), EsriTiles.roads());
    }

    TileSource getImagery() { return imagery; }
    TileSource getPlaces() { return places; }
    TileSource getRoads() { return roads; }
}
