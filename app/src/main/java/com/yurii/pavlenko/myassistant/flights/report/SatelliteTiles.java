package com.yurii.pavlenko.myassistant.flights.report;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.yurii.pavlenko.myassistant.flights.logic.TileGrid;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Locale;

/** Satellite photos from Esri World Imagery, fetched one at a time and only for the few a report needs. */
final class SatelliteTiles implements TileSource {

    private static final String URL_FORMAT = "https://server.arcgisonline.com/ArcGIS/rest/services/World_Imagery/MapServer/tile/%d/%d/%d";
    private static final String USER_AGENT = "MyAssistant/1.0 (personal flight log)";
    private static final int TIMEOUT_MS = 6_000;

    @Override
    public String attribution() {
        return "Imagery: Esri, Maxar, Earthstar Geographics and the GIS User Community.";
    }

    @Override
    public Bitmap tile(TileGrid.Tile tile) throws IOException {
        URL url = new URL(String.format(Locale.US, URL_FORMAT, tile.getZoom(), tile.getY(), tile.getX()));
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setConnectTimeout(TIMEOUT_MS);
        connection.setReadTimeout(TIMEOUT_MS);
        connection.setRequestProperty("User-Agent", USER_AGENT);
        try {
            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                throw new IOException("Tile server responded with HTTP " + connection.getResponseCode());
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
