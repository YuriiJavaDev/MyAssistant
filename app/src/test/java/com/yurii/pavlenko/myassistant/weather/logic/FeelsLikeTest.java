package com.yurii.pavlenko.myassistant.weather.logic;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class FeelsLikeTest {

    @Test
    public void humidHeatFeelsHotter() {
        assertEquals(33.0, FeelsLike.apparent(30, 50, 0), 0.5);
        assertTrue(FeelsLike.apparent(30, 90, 0) > FeelsLike.apparent(30, 30, 0));
    }

    @Test
    public void windMakesItFeelColder() {
        assertEquals(-5.9, FeelsLike.apparent(0, 80, 5), 0.5);
        assertTrue(FeelsLike.apparent(10, 50, 8) < FeelsLike.apparent(10, 50, 0));
    }
}
