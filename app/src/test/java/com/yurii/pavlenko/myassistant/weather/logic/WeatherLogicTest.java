package com.yurii.pavlenko.myassistant.weather.logic;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

public class WeatherLogicTest {

    private static long utc(int year, int month, int day, int hour, int minute) {
        return LocalDateTime.of(year, month, day, hour, minute).toInstant(ZoneOffset.UTC).toEpochMilli();
    }

    @Test
    public void wmoCodesMapToSceneConditions() {
        assertEquals(WeatherCondition.CLEAR, WeatherCode.condition(0));
        assertEquals(WeatherCondition.PARTLY_CLOUDY, WeatherCode.condition(2));
        assertEquals(WeatherCondition.CLOUDY, WeatherCode.condition(3));
        assertEquals(WeatherCondition.FOG, WeatherCode.condition(45));
        assertEquals(WeatherCondition.DRIZZLE, WeatherCode.condition(53));
        assertEquals(WeatherCondition.RAIN, WeatherCode.condition(63));
        assertEquals(WeatherCondition.RAIN, WeatherCode.condition(81));
        assertEquals(WeatherCondition.SNOW, WeatherCode.condition(73));
        assertEquals(WeatherCondition.STORM, WeatherCode.condition(95));
    }

    @Test
    public void unknownCodeIsShownAsOvercast() {
        assertEquals(WeatherCondition.CLOUDY, WeatherCode.condition(1234));
        assertEquals(WeatherDescription.UNKNOWN, WeatherCode.describe(1234));
    }

    @Test
    public void wmoCodesAreDescribed() {
        assertEquals(WeatherDescription.CLEAR_SKY, WeatherCode.describe(0));
        assertEquals(WeatherDescription.OVERCAST, WeatherCode.describe(3));
        assertEquals(WeatherDescription.THUNDERSTORM_WITH_HEAVY_HAIL, WeatherCode.describe(99));
    }

    @Test
    public void temperatureIsConvertedAndRounded() {
        assertEquals("21°C", TemperatureUnit.CELSIUS.format(20.6));
        assertEquals("-4°C", TemperatureUnit.CELSIUS.format(-3.6));
        assertEquals("68°F", TemperatureUnit.FAHRENHEIT.format(20));
        assertEquals("32°F", TemperatureUnit.FAHRENHEIT.format(0));
        assertEquals("24°", TemperatureUnit.CELSIUS.formatShort(24.2));
    }

    @Test
    public void windIsConverted() {
        assertEquals("3.2 m/s", WindUnit.METRES_PER_SECOND.format(3.2));
        assertEquals("18.0 km/h", WindUnit.KILOMETRES_PER_HOUR.format(5));
    }

    @Test
    public void knownFullMoonIsAlmostFullyLit() {
        MoonPhase full = MoonPhase.at(utc(2025, 10, 7, 3, 47));
        assertTrue(full.illumination() > 0.97);
        assertEquals(MoonPhase.Phase.FULL_MOON, full.phase());
    }

    @Test
    public void knownNewMoonIsAlmostDark() {
        MoonPhase dark = MoonPhase.at(utc(2025, 10, 21, 12, 25));
        assertTrue(dark.illumination() < 0.03);
        assertEquals(MoonPhase.Phase.NEW_MOON, dark.phase());
    }

    @Test
    public void moonWaxesBeforeFullAndWanesAfter() {
        assertTrue(MoonPhase.at(utc(2025, 10, 1, 0, 0)).isWaxing());
        assertFalse(MoonPhase.at(utc(2025, 10, 12, 0, 0)).isWaxing());
        assertEquals(MoonPhase.Phase.WAXING_GIBBOUS, MoonPhase.at(utc(2025, 10, 3, 12, 0)).phase());
        assertEquals(MoonPhase.Phase.LAST_QUARTER, MoonPhase.at(utc(2025, 10, 13, 18, 12)).phase());
    }
}
