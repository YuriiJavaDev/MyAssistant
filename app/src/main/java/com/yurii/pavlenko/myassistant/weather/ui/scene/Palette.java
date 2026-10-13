package com.yurii.pavlenko.myassistant.weather.ui.scene;

import com.yurii.pavlenko.myassistant.weather.logic.WeatherCondition;

/** The colours of the landscape for a kind of weather, by day or by night. */
final class Palette {

    private static final int NIGHT_TINT = 0x0B1530;
    private static final float NIGHT_STRENGTH = 0.65f;

    final int skyTop;
    final int skyBottom;
    final int seaFar;
    final int seaNear;
    final int sand;
    final int wetSand;
    final int grass;
    final int cloud;

    private Palette(int skyTop, int skyBottom, int seaFar, int seaNear, int sand, int wetSand, int grass, int cloud) {
        this.skyTop = opaque(skyTop);
        this.skyBottom = opaque(skyBottom);
        this.seaFar = opaque(seaFar);
        this.seaNear = opaque(seaNear);
        this.sand = opaque(sand);
        this.wetSand = opaque(wetSand);
        this.grass = opaque(grass);
        this.cloud = opaque(cloud);
    }

    /** Colours are written as RGB; this adds the full alpha that Android colour ints need. */
    private static int opaque(int rgb) {
        return 0xFF000000 | rgb;
    }

    /** The same landscape with frost on the sand and grass. */
    Palette frosted() {
        return new Palette(skyTop, skyBottom, seaFar, seaNear, blend(sand, 0xFFFFFF, 0.65f),
                blend(wetSand, 0xDDE6EE, 0.5f), blend(grass, 0xFFFFFF, 0.55f), cloud);
    }

    static Palette of(WeatherCondition condition, boolean day) {
        Palette base = byDay(condition);
        if (day) {
            return base;
        }
        boolean clear = condition == WeatherCondition.CLEAR;
        return new Palette(
                clear ? 0x050B24 : dim(base.skyTop), clear ? 0x1B2B5E : dim(base.skyBottom),
                dim(base.seaFar), dim(base.seaNear), dim(base.sand), dim(base.wetSand), dim(base.grass), dim(base.cloud));
    }

    private static Palette byDay(WeatherCondition condition) {
        switch (condition) {
            case CLEAR:
                return new Palette(0x2E9BE8, 0xBFE9FF, 0x1FB5C9, 0x5CD6D0, 0xF4D58D, 0xD9B36A, 0x4CAF50, 0xFFFFFF);
            case PARTLY_CLOUDY:
                return new Palette(0x4A9FDB, 0xCBE6F7, 0x2AA3B5, 0x62C8C4, 0xF0D391, 0xD3B070, 0x4CAF50, 0xFFFFFF);
            case FOG:
                return new Palette(0xC9D1D8, 0xE4E8EB, 0xA9BCC4, 0xBCCDD2, 0xE0D6BF, 0xC9BE9F, 0x7DA17F, 0xF0F2F3);
            case DRIZZLE:
            case RAIN:
                return new Palette(0x5D6F7F, 0x93A4B1, 0x3F6475, 0x557D8D, 0xC4B08A, 0x9C8A68, 0x4B7F4E, 0x9AA7B2);
            case SNOW:
                return new Palette(0xAEC3D6, 0xE8F0F7, 0x8FB4CC, 0xA9C9DC, 0xF3F6F9, 0xD6E1EA, 0xE6EEF3, 0xF4F7FA);
            case STORM:
                return new Palette(0x2B3544, 0x56637A, 0x23415A, 0x2E5470, 0x9A8663, 0x6F5F45, 0x35603A, 0x4A5568);
            case CLOUDY:
            default:
                return new Palette(0x8FA3B5, 0xC9D3DC, 0x5F8A99, 0x7FA9B3, 0xDCC9A0, 0xBFA97D, 0x5E9461, 0xE6EBEF);
        }
    }

    private static int dim(int color) {
        return blend(color, NIGHT_TINT, NIGHT_STRENGTH);
    }

    static int blend(int from, int to, float amount) {
        int r = mix((from >> 16) & 0xFF, (to >> 16) & 0xFF, amount);
        int g = mix((from >> 8) & 0xFF, (to >> 8) & 0xFF, amount);
        int b = mix(from & 0xFF, to & 0xFF, amount);
        return 0xFF000000 | (r << 16) | (g << 8) | b;
    }

    private static int mix(int a, int b, float amount) {
        return Math.round(a + (b - a) * amount);
    }
}
