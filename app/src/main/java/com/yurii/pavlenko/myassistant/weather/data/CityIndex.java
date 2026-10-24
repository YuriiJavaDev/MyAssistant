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
 * display name (English), country code, latitude, longitude, population, the searchable names in the form
 * "|kharkiv|харьков|" and the names to show by language such as "he=חרקוב;el=Χάρκοβο", empty when there are none
 * (see tools/cities/build_cities.py). A city is shown by its name in the language asked for, or else its English name.
 */
public final class CityIndex {

    /** Cities with this many people or more have an importance of 1; smaller ones are ranked by their share of it. */
    private static final double BIG_CITY = 10_000_000.0;
    private static final Pattern MARKS = Pattern.compile("\\p{M}+");
    private static final int FIELDS = 7;
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
    public static List<Place> search(InputStream list, String query, int limit, Locale language) throws IOException {
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
                    matches.add(new Match(fields, exact, language));
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
    public static Place nearest(InputStream list, double latitude, double longitude, double maxKm, Locale language) throws IOException {
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
        return km <= maxKm ? new Match(bestFields, true, language).place : null;
    }

    /** The name in the language, such as "he" or "zh-CN" (found by the language and then the language alone), else the English one. */
    static String shownName(String[] fields, Locale language) {
        String names = fields[6];
        if (names.isEmpty()) {
            return fields[0];
        }
        String tag = language.getLanguage();
        if ("iw".equals(tag)) {
            tag = "he";
        } else if ("in".equals(tag)) {
            tag = "id";
        }
        if ("zh".equals(tag)) {
            tag = "TW".equals(language.getCountry()) || "HK".equals(language.getCountry()) ? "zh-TW" : "zh-CN";
        }
        for (String entry : names.split(";")) {
            int cut = entry.indexOf('=');
            if (cut > 0 && entry.substring(0, cut).equals(tag)) {
                return entry.substring(cut + 1);
            }
        }
        return fields[0];
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

        Match(String[] fields, boolean exact, Locale language) {
            this.exact = exact;
            long people;
            try {
                people = Long.parseLong(fields[4]);
            } catch (NumberFormatException e) {
                people = 0;
            }
            this.population = people;
            String country = fields[1].isEmpty() ? "" : new Locale("", fields[1]).getDisplayCountry(language);
            this.place = new Place(shownName(fields, language), "", country, parse(fields[2]), parse(fields[3]),
                    Math.min(1.0, people / BIG_CITY));
        }
    }
}
