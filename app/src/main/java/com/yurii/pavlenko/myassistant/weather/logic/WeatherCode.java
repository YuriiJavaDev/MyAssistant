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

    public static String describe(int code) {
        switch (code) {
            case 0: return "Clear sky";
            case 1: return "Mainly clear";
            case 2: return "Partly cloudy";
            case 3: return "Overcast";
            case 45: return "Fog";
            case 48: return "Rime fog";
            case 51: return "Light drizzle";
            case 53: return "Drizzle";
            case 55: return "Dense drizzle";
            case 56: return "Light freezing drizzle";
            case 57: return "Freezing drizzle";
            case 61: return "Slight rain";
            case 63: return "Rain";
            case 65: return "Heavy rain";
            case 66: return "Light freezing rain";
            case 67: return "Freezing rain";
            case 71: return "Slight snow";
            case 73: return "Snow";
            case 75: return "Heavy snow";
            case 77: return "Snow grains";
            case 80: return "Light rain showers";
            case 81: return "Rain showers";
            case 82: return "Violent rain showers";
            case 85: return "Light snow showers";
            case 86: return "Heavy snow showers";
            case 95: return "Thunderstorm";
            case 96: return "Thunderstorm with hail";
            case 99: return "Thunderstorm with heavy hail";
            default: return "Unknown";
        }
    }
}
