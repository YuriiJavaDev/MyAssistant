package com.yurii.pavlenko.myassistant.weather.data;

/** A city the weather is shown for. */
public final class Place {

    private final String name;
    private final String region;
    private final String country;
    private final double latitude;
    private final double longitude;
    private final double importance;

    /** @param importance how well known the place is, from 0 to 1; it ranks cities with the same name */
    public Place(String name, String region, String country, double latitude, double longitude, double importance) {
        this.name = name;
        this.region = region;
        this.country = country;
        this.latitude = latitude;
        this.longitude = longitude;
        this.importance = importance;
    }

    public String getName() {
        return name;
    }

    public String getCountry() {
        return country;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public double getImportance() {
        return importance;
    }

    /** City, region and country, as shown when the user has to pick among cities with the same name. */
    public String fullName() {
        StringBuilder text = new StringBuilder(name);
        if (!region.isEmpty() && !region.equals(name)) {
            text.append(", ").append(region);
        }
        if (!country.isEmpty()) {
            text.append(", ").append(country);
        }
        return text.toString();
    }
}
