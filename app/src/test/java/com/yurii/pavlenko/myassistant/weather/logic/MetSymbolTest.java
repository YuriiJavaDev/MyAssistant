package com.yurii.pavlenko.myassistant.weather.logic;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

public class MetSymbolTest {

    @Test
    public void dropsTheTimeOfDayEnding() {
        assertEquals("lightrainshowers", MetSymbol.base("lightrainshowers_day"));
        assertEquals("cloudy", MetSymbol.base("cloudy"));
    }

    @Test
    public void readsTheTimeOfDay() {
        assertEquals(Boolean.TRUE, MetSymbol.isDay("clearsky_day"));
        assertEquals(Boolean.FALSE, MetSymbol.isDay("clearsky_night"));
        assertEquals(Boolean.TRUE, MetSymbol.isDay("fair_polartwilight"));
        assertNull(MetSymbol.isDay("cloudy"));
    }

    @Test
    public void mapsSymbolsToWmoCodes() {
        assertEquals(0, MetSymbol.wmoCode("clearsky_night"));
        assertEquals(1, MetSymbol.wmoCode("fair_day"));
        assertEquals(2, MetSymbol.wmoCode("partlycloudy_day"));
        assertEquals(3, MetSymbol.wmoCode("cloudy"));
        assertEquals(45, MetSymbol.wmoCode("fog"));
        assertEquals(63, MetSymbol.wmoCode("rain"));
        assertEquals(82, MetSymbol.wmoCode("heavyrainshowers_day"));
        assertEquals(67, MetSymbol.wmoCode("sleet"));
        assertEquals(75, MetSymbol.wmoCode("heavysnow"));
        assertEquals(86, MetSymbol.wmoCode("snowshowers_night"));
    }

    @Test
    public void everyThunderSymbolIsAThunderstorm() {
        assertEquals(95, MetSymbol.wmoCode("rainandthunder"));
        assertEquals(95, MetSymbol.wmoCode("heavysnowshowersandthunder_day"));
    }

    @Test
    public void anUnknownSymbolIsTreatedAsOvercast() {
        assertEquals(3, MetSymbol.wmoCode("somethingnew"));
    }

    @Test
    public void everyCodeItGivesHasACondition() {
        String[] symbols = {"clearsky", "fair", "partlycloudy", "cloudy", "fog", "lightrain", "rain", "heavyrain",
                "lightrainshowers", "rainshowers", "heavyrainshowers", "lightsleet", "sleet", "lightsnow", "snow",
                "heavysnow", "lightsnowshowers", "snowshowers", "rainandthunder"};
        for (String symbol : symbols) {
            int code = MetSymbol.wmoCode(symbol);
            assertEquals(symbol, WeatherCode.describe(code) == WeatherDescription.UNKNOWN, false);
        }
    }
}
