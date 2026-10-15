package com.yurii.pavlenko.myassistant.flights.report;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.yurii.pavlenko.myassistant.flights.logic.TileGrid;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Locale;

/** Free map layers of Esri: satellite photos, and transparent layers with the names of places and roads. */
final class EsriTiles implements TileSource {

    private static final String URL_FORMAT = "https://server.arcgisonline.com/ArcGIS/rest/services/%s/tile/%d/%d/%d";
    private static final String USER_AGENT = "MyAssistant/1.0 (personal flight log)";
    private static final String ATTRIBUTION = "Imagery: Esri, Maxar, Earthstar Geographics and the GIS User Community.";
    private static final int TIMEOUT_MS = 6_000;

    private final String service;

    private EsriTiles(String service) {
        this.service = service;
    }

    static EsriTiles imagery() {
        return new EsriTiles("World_Imagery/MapServer");
    }

    /** Names of countries, cities, seas, lakes and rivers, with borders. */
    static EsriTiles places() {
        return new EsriTiles("Reference/World_Boundaries_and_Places/MapServer");
    }

    /** Roads with their names; only has detail at close range. */
    static EsriTiles roads() {
        return new EsriTiles("Reference/World_Transportation/MapServer");
    }

    @Override
    public String attribution() {
        return ATTRIBUTION;
    }

    @Override
    public Bitmap tile(TileGrid.Tile tile) throws IOException {
        URL url = new URL(String.format(Locale.US, URL_FORMAT, service, tile.getZoom(), tile.getY(), tile.getX()));
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setConnectTimeout(TIMEOUT_MS);
        connection.setReadTimeout(TIMEOUT_MS);
        connection.setRequestProperty("User-Agent", USER_AGENT);
        try {
            int code = connection.getResponseCode();
            if (code == HttpURLConnection.HTTP_NOT_FOUND) {
                return null;
            }
            if (code != HttpURLConnection.HTTP_OK) {
                throw new IOException("Tile server responded with HTTP " + code);
            }
            try (InputStream in = connection.getInputStream()) {
                Bitmap bitmap = BitmapFactory.decodeStream(in);
                if (bitmap == null) {
                    throw new IOException("Tile is not an image");
                }
                return bitmap;
            }
        } finally {
            connection.disconnect();
        }
    }
}
