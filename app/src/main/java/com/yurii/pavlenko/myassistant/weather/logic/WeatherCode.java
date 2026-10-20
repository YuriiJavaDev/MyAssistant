package com.yurii.pavlenko.myassistant.weather.logic;

/** Interprets the WMO weather codes that the forecast service reports. */
public final class WeatherCode {

    private WeatherCode() {
    }

    public static WeatherCondition condition(int code) {
        switch (code) {
            case 0:
            case 1:
                return WeatherCondition.CLEAR;
            case 2:
                return WeatherCondition.PARTLY_CLOUDY;
            case 3:
                return WeatherCondition.CLOUDY;
            case 45:
            case 48:
                return WeatherCondition.FOG;
            case 51:
            case 53:
            case 55:
            case 56:
            case 57:
                return WeatherCondition.DRIZZLE;
            case 61:
            case 63:
            case 65:
            case 66:
            case 67:
            case 80:
            case 81:
            case 82:
                return WeatherCondition.RAIN;
            case 71:
            case 73:
            case 75:
            case 77:
            case 85:
            case 86:
                return WeatherCondition.SNOW;
            case 95:
            case 96:
            case 99:
                return WeatherCondition.STORM;
            default:
                return WeatherCondition.CLOUDY;
        }
    }

    public static WeatherDescription describe(int code) {
        switch (code) {
            case 0: return WeatherDescription.CLEAR_SKY;
            case 1: return WeatherDescription.MAINLY_CLEAR;
            case 2: return WeatherDescription.PARTLY_CLOUDY;
            case 3: return WeatherDescription.OVERCAST;
            case 45: return WeatherDescription.FOG;
            case 48: return WeatherDescription.RIME_FOG;
            case 51: return WeatherDescription.LIGHT_DRIZZLE;
            case 53: return WeatherDescription.DRIZZLE;
            case 55: return WeatherDescription.DENSE_DRIZZLE;
            case 56: return WeatherDescription.LIGHT_FREEZING_DRIZZLE;
            case 57: return WeatherDescription.FREEZING_DRIZZLE;
            case 61: return WeatherDescription.SLIGHT_RAIN;
            case 63: return WeatherDescription.RAIN;
            case 65: return WeatherDescription.HEAVY_RAIN;
            case 66: return WeatherDescription.LIGHT_FREEZING_RAIN;
            case 67: return WeatherDescription.FREEZING_RAIN;
            case 71: return WeatherDescription.SLIGHT_SNOW;
            case 73: return WeatherDescription.SNOW;
            case 75: return WeatherDescription.HEAVY_SNOW;
            case 77: return WeatherDescription.SNOW_GRAINS;
            case 80: return WeatherDescription.LIGHT_RAIN_SHOWERS;
            case 81: return WeatherDescription.RAIN_SHOWERS;
            case 82: return WeatherDescription.VIOLENT_RAIN_SHOWERS;
            case 85: return WeatherDescription.LIGHT_SNOW_SHOWERS;
            case 86: return WeatherDescription.HEAVY_SNOW_SHOWERS;
            case 95: return WeatherDescription.THUNDERSTORM;
            case 96: return WeatherDescription.THUNDERSTORM_WITH_HAIL;
            case 99: return WeatherDescription.THUNDERSTORM_WITH_HEAVY_HAIL;
            default: return WeatherDescription.UNKNOWN;
        }
    }
}
