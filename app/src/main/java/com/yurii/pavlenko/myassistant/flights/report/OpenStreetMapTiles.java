package com.yurii.pavlenko.myassistant.flights.report;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.yurii.pavlenko.myassistant.flights.logic.TileGrid;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Locale;

/** Tiles of OpenStreetMap, fetched one at a time and only for the few a report needs. */
final class OpenStreetMapTiles implements TileSource {

    private static final String URL_FORMAT = "https://tile.openstreetmap.org/%d/%d/%d.png";
    private static final String USER_AGENT = "MyAssistant/1.0 (personal flight log)";
    private static final int TIMEOUT_MS = 6_000;

    @Override
    public Bitmap tile(TileGrid.Tile tile) throws IOException {
        URL url = new URL(String.format(Locale.US, URL_FORMAT, tile.getZoom(), tile.getX(), tile.getY()));
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
