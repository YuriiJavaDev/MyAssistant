package com.yurii.pavlenko.myassistant.weather.data;

/** A city the weather is shown for. */
public final class Place {

    private final String name;
    private final String region;
    private final String country;
    private final double latitude;
    private final double longitude;

    public Place(String name, String region, String country, double latitude, double longitude) {
        this.name = name;
        this.region = region;
        this.country = country;
        this.latitude = latitude;
        this.longitude = longitude;
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
