package com.yurii.pavlenko.myassistant.weather.data;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Finds cities in the bundled list (GeoNames, CC BY 4.0, cities of 5000 people and more), with no network.
 * The list is read line by line, so it never has to fit in memory. Every line holds, separated by tabs:
 * display name, country code, latitude, longitude, population and the searchable names in the form
 * "|kharkiv|харьков|" (see tools/cities/build_cities.py).
 */
public final class CityIndex {

    /** Cities with this many people or more have an importance of 1; smaller ones are ranked by their share of it. */
    private static final double BIG_CITY = 10_000_000.0;
    private static final Pattern MARKS = Pattern.compile("\\p{M}+");
    private static final int FIELDS = 6;
    private static final char BAR = '|';

    private CityIndex() {
    }

    /** Lower case and without accents, the form in which the names are stored. */
    public static String normalize(String text) {
        String lower = text.toLowerCase(Locale.ROOT);
        return MARKS.matcher(Normalizer.normalize(lower, Normalizer.Form.NFD)).replaceAll("").trim();
    }

    /**
     * Cities whose name is, or starts with, the query: whole names first, then the most populous first.
     * Needs at least two characters, so a single letter does not list half the world.
     */
    public static List<Place> search(InputStream list, String query, int limit) throws IOException {
        String wanted = normalize(query);
        if (wanted.length() < 2 || wanted.indexOf(BAR) >= 0) {
            return Collections.emptyList();
        }
        String whole = BAR + wanted + BAR;
        String start = BAR + wanted;
        List<Match> matches = new ArrayList<>();
        try (BufferedReader reader = reader(list)) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] fields = line.split("\t", FIELDS);
                if (fields.length < FIELDS) {
                    continue;
                }
                boolean exact = fields[5].contains(whole);
                if (exact || fields[5].contains(start)) {
                    matches.add(new Match(fields, exact));
                }
            }
        }
        Collections.sort(matches, (a, b) -> a.exact != b.exact
                ? (a.exact ? -1 : 1)
                : Long.compare(b.population, a.population));
        List<Place> found = new ArrayList<>();
        for (Match match : matches) {
            if (found.size() == limit) {
                break;
            }
            found.add(match.place);
        }
        return found;
    }

    /** The city nearest to the point, or null when none lies within {@code maxKm}. */
    public static Place nearest(InputStream list, double latitude, double longitude, double maxKm) throws IOException {
        double scale = Math.cos(Math.toRadians(latitude));
        double best = Double.MAX_VALUE;
        String[] bestFields = null;
        try (BufferedReader reader = reader(list)) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] fields = line.split("\t", FIELDS);
                if (fields.length < FIELDS) {
                    continue;
                }
                double dLat = parse(fields[2]) - latitude;
                double dLon = (parse(fields[3]) - longitude) * scale;
                double squared = dLat * dLat + dLon * dLon;
                if (squared < best) {
                    best = squared;
                    bestFields = fields;
                }
            }
        }
        if (bestFields == null) {
            return null;
        }
        double km = Math.sqrt(best) * 111.2;
        return km <= maxKm ? new Match(bestFields, true).place : null;
    }

    private static BufferedReader reader(InputStream list) {
        return new BufferedReader(new InputStreamReader(list, StandardCharsets.UTF_8), 1 << 16);
    }

    private static double parse(String number) {
        try {
            return Double.parseDouble(number);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private static final class Match {
        final boolean exact;
        final long population;
        final Place place;

        Match(String[] fields, boolean exact) {
            this.exact = exact;
            long people;
            try {
                people = Long.parseLong(fields[4]);
            } catch (NumberFormatException e) {
                people = 0;
            }
            this.population = people;
            String country = fields[1].isEmpty() ? "" : new Locale("", fields[1]).getDisplayCountry(Locale.ENGLISH);
            this.place = new Place(fields[0], "", country, parse(fields[2]), parse(fields[3]),
                    Math.min(1.0, people / BIG_CITY));
        }
    }
}
