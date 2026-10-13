package com.yurii.pavlenko.myassistant.weather.logic;

/** What goes on at the beach in the given weather: who is there, what they wear, and how the sea behaves. */
public final class BeachScenario {

    private static final double SUNBATHING_MIN_CELSIUS = 20;
    private static final double LIGHT_CLOTHES_MIN_CELSIUS = 18;
    private static final double JACKET_MIN_CELSIUS = 8;
    private static final double COAT_MIN_CELSIUS = 0;
    private static final double FLOWERS_MIN_CELSIUS = 8;
    private static final double BUTTERFLIES_MIN_CELSIUS = 15;
    private static final double ICE_MAX_CELSIUS = 0;
    private static final double BREEZE_MS = 5;
    private static final double STRONG_WIND_MS = 8;
    private static final double GALE_MS = 12;

    private final Crowd crowd;
    private final Outfit outfit;
    private final boolean parasolsOpen;
    private final boolean butterflies;
    private final boolean gulls;
    private final boolean iceFloes;
    private final boolean flowers;
    private final double seaRoughness;
    private final double wind;

    private BeachScenario(Crowd crowd, Outfit outfit, boolean parasolsOpen, boolean butterflies, boolean gulls,
                          boolean iceFloes, boolean flowers, double seaRoughness, double wind) {
        this.crowd = crowd;
        this.outfit = outfit;
        this.parasolsOpen = parasolsOpen;
        this.butterflies = butterflies;
        this.gulls = gulls;
        this.iceFloes = iceFloes;
        this.flowers = flowers;
        this.seaRoughness = seaRoughness;
        this.wind = wind;
    }

    public static BeachScenario of(WeatherCondition condition, boolean day, double celsius, double windMetresPerSecond) {
        boolean stormy = condition == WeatherCondition.STORM;
        boolean wet = condition == WeatherCondition.RAIN || condition == WeatherCondition.DRIZZLE;
        boolean snowing = condition == WeatherCondition.SNOW;
        boolean sunny = condition == WeatherCondition.CLEAR || condition == WeatherCondition.PARTLY_CLOUDY;
        boolean windy = windMetresPerSecond >= STRONG_WIND_MS;
        boolean breezy = windMetresPerSecond >= BREEZE_MS;

        return new BeachScenario(
                crowd(day, celsius, stormy, wet, sunny, windMetresPerSecond),
                outfit(celsius),
                day && !stormy && !wet && !windy,
                day && sunny && celsius >= BUTTERFLIES_MIN_CELSIUS && !breezy,
                day && breezy && !stormy,
                celsius <= ICE_MAX_CELSIUS,
                celsius >= FLOWERS_MIN_CELSIUS && !snowing,
                roughness(stormy, wet, windMetresPerSecond),
                windMetresPerSecond);
    }

    private static Crowd crowd(boolean day, double celsius, boolean stormy, boolean wet, boolean sunny, double wind) {
        if (!day || stormy || wind >= GALE_MS) {
            return Crowd.NONE;
        }
        if (wet) {
            return Crowd.UMBRELLA_WALKERS;
        }
        if (sunny && celsius >= SUNBATHING_MIN_CELSIUS && wind < STRONG_WIND_MS) {
            return Crowd.SUNBATHERS;
        }
        return Crowd.STROLLERS;
    }

    private static Outfit outfit(double celsius) {
        if (celsius >= LIGHT_CLOTHES_MIN_CELSIUS) {
            return Outfit.LIGHT;
        }
        if (celsius >= JACKET_MIN_CELSIUS) {
            return Outfit.JACKET;
        }
        return celsius >= COAT_MIN_CELSIUS ? Outfit.COAT : Outfit.FUR;
    }

    /** 0 is a calm sea, 1 a storm. */
    private static double roughness(boolean stormy, boolean wet, double wind) {
        double base = Math.min(1, wind / GALE_MS) + (wet ? 0.15 : 0);
        return stormy ? 1 : Math.min(1, base);
    }

    public Crowd getCrowd() { return crowd; }
    public Outfit getOutfit() { return outfit; }
    public boolean areParasolsOpen() { return parasolsOpen; }
    public boolean hasButterflies() { return butterflies; }
    public boolean hasGulls() { return gulls; }
    public boolean hasIceFloes() { return iceFloes; }
    public boolean hasFlowers() { return flowers; }
    public double getSeaRoughness() { return seaRoughness; }

    /** How hard the wind blows, in metres per second; closed parasols lean and gulls glide with it. */
    public double getWind() { return wind; }
}
