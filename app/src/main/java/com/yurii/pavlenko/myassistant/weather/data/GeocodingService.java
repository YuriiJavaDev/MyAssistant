package com.yurii.pavlenko.myassistant.weather.data;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Finds cities by name in any language, and names the place where the phone is, using the free
 * OpenStreetMap geocoder (Nominatim). All calls block, so they must run off the main thread.
 */
public final class GeocodingService {

    private static final String SEARCH_URL = "https://nominatim.openstreetmap.org/search?format=jsonv2&addressdetails=1"
            + "&accept-language=en&featureType=settlement&limit=10&q=%s";
    private static final String REVERSE_URL = "https://nominatim.openstreetmap.org/reverse?format=jsonv2&addressdetails=1"
            + "&accept-language=en&zoom=10&lat=%s&lon=%s";
    private static final String[] TOWN_KEYS = {"city", "town", "village", "municipality", "hamlet", "suburb", "county"};
    private static final Set<String> SETTLEMENT_TYPES = new HashSet<>(Arrays.asList("city", "town", "village", "hamlet", "municipality"));
    private static final int MAX_MATCHES = 5;
    private static final String UNKNOWN_PLACE = "Current location";

    private GeocodingService() {
    }

    /** Cities that match the name, best known first; smaller features such as districts are used only if nothing else matches. */
    public static List<Place> search(String query) throws IOException {
        String encoded = URLEncoder.encode(query.trim(), StandardCharsets.UTF_8.name());
        try {
            JSONArray items = new JSONArray(HttpText.get(String.format(Locale.US, SEARCH_URL, encoded)));
            List<Place> settlements = new ArrayList<>();
            List<Place> others = new ArrayList<>();
            Set<String> seen = new HashSet<>();
            for (int i = 0; i < items.length(); i++) {
                JSONObject item = items.getJSONObject(i);
                Place place = toPlace(item);
                if (place != null && seen.add(place.fullName())) {
                    (SETTLEMENT_TYPES.contains(item.optString("addresstype")) ? settlements : others).add(place);
                }
            }
            List<Place> found = settlements.isEmpty() ? others : settlements;
            return found.subList(0, Math.min(MAX_MATCHES, found.size()));
        } catch (JSONException e) {
            throw new IOException("Unexpected response from the search service", e);
        }
    }

    /** Names the place at the given coordinates; falls back to a neutral name when the lookup fails. */
    public static Place describe(double latitude, double longitude) {
        try {
            JSONObject item = new JSONObject(HttpText.get(String.format(Locale.US, REVERSE_URL, latitude, longitude)));
            Place place = toPlace(item);
            if (place != null) {
                return new Place(place.getName(), regionOf(item), place.getCountry(), latitude, longitude, 1);
            }
        } catch (IOException | JSONException ignored) {
            // The weather itself is still available for these coordinates
        }
        return new Place(UNKNOWN_PLACE, "", "", latitude, longitude, 1);
    }

    private static Place toPlace(JSONObject item) {
        JSONObject address = item.optJSONObject("address");
        String name = townName(item, address);
        if (name.isEmpty()) {
            return null;
        }
        String country = address == null ? "" : address.optString("country");
        return new Place(name, regionOf(item), country, item.optDouble("lat"), item.optDouble("lon"), item.optDouble("importance"));
    }

    private static String townName(JSONObject item, JSONObject address) {
        String name = item.optString("name");
        if (!name.isEmpty() || address == null) {
            return name;
        }
        for (String key : TOWN_KEYS) {
            String value = address.optString(key);
            if (!value.isEmpty()) {
                return value;
            }
        }
        return "";
    }

    private static String regionOf(JSONObject item) {
        JSONObject address = item.optJSONObject("address");
        return address == null ? "" : address.optString("state");
    }
}
