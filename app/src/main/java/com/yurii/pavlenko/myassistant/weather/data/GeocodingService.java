package com.yurii.pavlenko.myassistant.weather.data;

import android.content.Context;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * Finds cities by name in any language, and names the place where the phone is, using the list of cities
 * bundled with the app, so it works without a network and sends nothing anywhere. The calls read the list
 * and must run off the main thread.
 */
public final class GeocodingService {

    private static final String LIST_FILE = "cities.tsv";
    private static final int MAX_MATCHES = 5;
    /** A phone farther than this from every listed city gets no name rather than a misleading one. */
    private static final double NEAREST_CITY_KM = 60;

    private GeocodingService() {
    }

    /** Cities that match the name, whole names first and then the most populous. */
    public static List<Place> search(Context context, String query) throws IOException {
        try (InputStream list = context.getAssets().open(LIST_FILE)) {
            return CityIndex.search(list, query, MAX_MATCHES);
        }
    }

    /** Names the place at the given coordinates; falls back to a neutral name when no listed city is near. */
    public static Place describe(Context context, double latitude, double longitude) {
        try (InputStream list = context.getAssets().open(LIST_FILE)) {
            Place city = CityIndex.nearest(list, latitude, longitude, NEAREST_CITY_KM);
            if (city != null) {
                return new Place(city.getName(), "", city.getCountry(), latitude, longitude, 1);
            }
        } catch (IOException ignored) {
            // The weather itself is still available for these coordinates
        }
        return new Place("", "", "", latitude, longitude, 1);
    }
}
