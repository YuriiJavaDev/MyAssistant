package com.yurii.pavlenko.myassistant.weather.logic;

/**
 * The temperature it feels like, which the forecast service does not give. This is the apparent temperature of
 * the Australian Bureau of Meteorology (Steadman), which accounts for humidity and wind in the shade.
 */
public final class FeelsLike {

    private FeelsLike() {
    }

    /**
     * @param celsius air temperature in degrees Celsius
     * @param humidityPercent relative humidity, 0 to 100
     * @param windMetresPerSecond wind speed
     */
    public static double apparent(double celsius, double humidityPercent, double windMetresPerSecond) {
        double vapourPressure = humidityPercent / 100.0 * 6.105 * Math.exp(17.27 * celsius / (237.7 + celsius));
        return celsius + 0.33 * vapourPressure - 0.70 * windMetresPerSecond - 4.00;
    }
}
