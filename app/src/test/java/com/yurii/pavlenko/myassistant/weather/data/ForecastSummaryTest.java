package com.yurii.pavlenko.myassistant.weather.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class ForecastSummaryTest {

    private static final long START = Instant.parse("2026-10-24T09:00:00Z").toEpochMilli();
    private static final long HOUR = 3_600_000L;

    private static List<ForecastSample> hours(double... temperatures) {
        List<ForecastSample> list = new ArrayList<>();
        for (int i = 0; i < temperatures.length; i++) {
            list.add(new ForecastSample(START + i * HOUR, temperatures[i], 60, 3, i == 0 ? "rain_day" : ""));
        }
        return list;
    }

    @Test
    public void takesTheCurrentValuesFromTheFirstHour() {
        CurrentWeather weather = ForecastSummary.of(hours(12.5, 14, 11), 49.99, 36.23);
        assertEquals(12.5, weather.getTemperature(), 0.001);
        assertEquals(60, weather.getHumidity());
        assertEquals(3, weather.getWindSpeed(), 0.001);
        assertEquals(63, weather.getWeatherCode());
        assertTrue(weather.isDay());
    }

    @Test
    public void rangeCoversTheNextDayOnly() {
        double[] values = new double[30];
        java.util.Arrays.fill(values, 10);
        values[5] = 18;
        values[10] = 4;
        values[27] = 40;
        CurrentWeather weather = ForecastSummary.of(hours(values), 49.99, 36.23);
        assertEquals(18, weather.getTodayMax(), 0.001);
        assertEquals(4, weather.getTodayMin(), 0.001);
    }

    @Test
    public void usesTheSunWhenTheSymbolDoesNotSayAndFallsBackToOvercastWithoutOne() {
        List<ForecastSample> night = new ArrayList<>();
        night.add(new ForecastSample(Instant.parse("2026-10-24T22:00:00Z").toEpochMilli(), 5, 80, 1, "cloudy"));
        assertFalse(ForecastSummary.of(night, 49.99, 36.23).isDay());
        List<ForecastSample> bare = new ArrayList<>();
        bare.add(new ForecastSample(START, 5, 80, 1, ""));
        CurrentWeather weather = ForecastSummary.of(bare, 49.99, 36.23);
        assertEquals(3, weather.getWeatherCode());
        assertTrue(weather.isDay());
    }

    @Test
    public void aNightSymbolMakesItNight() {
        List<ForecastSample> one = new ArrayList<>();
        one.add(new ForecastSample(START, 5, 80, 1, "clearsky_night"));
        assertFalse(ForecastSummary.of(one, 49.99, 36.23).isDay());
    }
}
