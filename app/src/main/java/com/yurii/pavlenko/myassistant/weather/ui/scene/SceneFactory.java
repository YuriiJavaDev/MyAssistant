package com.yurii.pavlenko.myassistant.weather.ui.scene;

import com.yurii.pavlenko.myassistant.weather.logic.BeachScenario;
import com.yurii.pavlenko.myassistant.weather.logic.Crowd;
import com.yurii.pavlenko.myassistant.weather.logic.MoonPhase;
import com.yurii.pavlenko.myassistant.weather.logic.WeatherCondition;

import java.util.ArrayList;
import java.util.List;

/** Builds the animated scenes of the two portholes: the beach in the current weather, and tonight's moon. */
public final class SceneFactory {

    private SceneFactory() {
    }

    private static final float MOON_SKY_STAR_AREA = 0.95f;
    private static final int MOON_SKY_STARS = 60;
    private static final int BEACH_SKY_STARS = 36;

    /** The beach as it looks in the given weather, by day or by night. */
    public static Scene weather(WeatherCondition condition, boolean day, BeachScenario scenario) {
        Palette palette = Palette.of(condition, day);
        if (scenario.hasIceFloes()) {
            palette = palette.frosted();
        }
        float roughness = (float) scenario.getSeaRoughness();
        List<Layer> layers = new ArrayList<>();
        layers.add(new SkyLayer(palette, SceneGeometry.HORIZON + 0.02f));
        addSky(layers, condition, day, palette);
        layers.add(new SandLayer(palette));
        layers.add(new SeaLayer(palette, roughness));
        if (scenario.hasYacht()) {
            layers.add(new YachtLayer());
        }
        if (scenario.hasIceFloes()) {
            layers.add(new IceFloeLayer());
        }
        layers.add(new LoungerLayer(scenario.getCrowd() == Crowd.SUNBATHERS, scenario.areParasolsOpen(), scenario.getWind()));
        if (scenario.getCrowd() == Crowd.STROLLERS || scenario.getCrowd() == Crowd.UMBRELLA_WALKERS) {
            layers.add(new PromenadeLayer(scenario.getCrowd(), scenario.getOutfit()));
        }
        layers.add(new FlowerLayer(palette, scenario.hasFlowers()));
        if (scenario.hasButterflies()) {
            layers.add(new ButterflyLayer());
        }
        if (scenario.hasGulls()) {
            layers.add(new GullLayer(scenario.getWind()));
        }
        addEffects(layers, condition);
        return new Scene(layers);
    }

    /** The night sky with the moon in its current phase; in the southern hemisphere the moon appears mirrored. */
    public static Scene moon(MoonPhase phase, boolean southernHemisphere) {
        Palette night = Palette.of(WeatherCondition.CLEAR, false);
        List<Layer> layers = new ArrayList<>();
        layers.add(new SkyLayer(night, 1f));
        layers.add(new StarsLayer(MOON_SKY_STARS, MOON_SKY_STAR_AREA));
        layers.add(new MoonPhaseLayer(phase, southernHemisphere));
        return new Scene(layers);
    }

    private static void addSky(List<Layer> layers, WeatherCondition condition, boolean day, Palette palette) {
        boolean starry = condition == WeatherCondition.CLEAR || condition == WeatherCondition.PARTLY_CLOUDY;
        if (!day && starry) {
            if (condition == WeatherCondition.CLEAR) {
                layers.add(new StarsLayer(BEACH_SKY_STARS, SceneGeometry.HORIZON - 0.05f));
            }
            layers.add(new MoonLayer(palette));
        }
        if (day && starry) {
            layers.add(new SunLayer());
        }
        switch (condition) {
            case CLEAR:
                layers.add(new CloudLayer(palette, 2, 0.012f, 0.12f, 0.3f, 0.8f));
                break;
            case PARTLY_CLOUDY:
                layers.add(new CloudLayer(palette, 4, 0.018f, 0.1f, 0.35f, 1f));
                break;
            case FOG:
                layers.add(new CloudLayer(palette, 3, 0.008f, 0.1f, 0.3f, 1.2f));
                break;
            case CLOUDY:
            case SNOW:
                layers.add(new CloudLayer(palette, 6, 0.014f, 0.08f, 0.38f, 1.3f));
                break;
            case DRIZZLE:
            case RAIN:
            case STORM:
                layers.add(new CloudLayer(palette, 7, 0.02f, 0.06f, 0.36f, 1.5f));
                break;
            default:
                break;
        }
    }

    private static void addEffects(List<Layer> layers, WeatherCondition condition) {
        switch (condition) {
            case FOG:
                layers.add(new FogLayer());
                break;
            case DRIZZLE:
                layers.add(new RainLayer(45, 0.05f, 0.04f, 0.8f));
                break;
            case RAIN:
                layers.add(new RainLayer(110, 0.08f, 0.06f, 1.1f));
                break;
            case SNOW:
                layers.add(new SnowLayer());
                break;
            case STORM:
                layers.add(new RainLayer(150, 0.16f, 0.07f, 1.5f));
                layers.add(new LightningLayer());
                break;
            default:
                break;
        }
    }
}
