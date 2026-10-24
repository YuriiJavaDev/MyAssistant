package com.yurii.pavlenko.myassistant.weather.logic;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.time.Instant;

public class SunTest {

    private static long utc(String time) {
        return Instant.parse(time).toEpochMilli();
    }

    @Test
    public void itIsDayAtNoonAndNightAtMidnightInKharkiv() {
        assertTrue(Sun.isUp(49.99, 36.23, utc("2026-10-24T09:00:00Z")));
        assertFalse(Sun.isUp(49.99, 36.23, utc("2026-10-24T22:00:00Z")));
    }

    @Test
    public void followsTheSeasonsAtTheSameClockTime() {
        assertTrue(Sun.isUp(49.99, 36.23, utc("2026-06-21T17:30:00Z")));
        assertFalse(Sun.isUp(49.99, 36.23, utc("2026-12-21T17:30:00Z")));
    }

    @Test
    public void thereIsNoSunInTheMiddleOfAPolarWinter() {
        assertFalse(Sun.isUp(69.65, 18.96, utc("2026-12-21T11:00:00Z")));
        assertTrue(Sun.isUp(69.65, 18.96, utc("2026-06-21T23:00:00Z")));
    }

    @Test
    public void worksOnTheOtherSideOfTheWorld() {
        assertTrue(Sun.isUp(-33.87, 151.21, utc("2026-10-24T01:00:00Z")));
        assertFalse(Sun.isUp(-33.87, 151.21, utc("2026-10-24T14:00:00Z")));
    }
}
