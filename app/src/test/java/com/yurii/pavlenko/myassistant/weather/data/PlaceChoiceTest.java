package com.yurii.pavlenko.myassistant.weather.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

public class PlaceChoiceTest {

    private static Place place(String name, double importance) {
        return new Place(name, "", "Country", 0, 0, importance);
    }

    @Test
    public void noMatchesGiveNoAnswer() {
        assertTrue(PlaceChoice.obvious(Collections.<Place>emptyList()) == null);
    }

    @Test
    public void singleMatchIsTaken() {
        Place only = place("Kyiv", 0.2);
        assertEquals(only, PlaceChoice.obvious(Collections.singletonList(only)));
    }

    @Test
    public void bigCityBeatsSmallPlacesWithSimilarNames() {
        Place big = place("Kharkiv", 0.7);
        assertEquals(big, PlaceChoice.obvious(Arrays.asList(big, place("Kharkivka", 0.25), place("Kharkivsk", 0.2))));
    }

    @Test
    public void equallyLikelyPlacesAreLeftToTheUser() {
        assertTrue(PlaceChoice.obvious(Arrays.asList(place("Paris, France", 0.9), place("Paris, Texas", 0.6))) == null);
    }

    @Test
    public void fullNameListsRegionAndCountryWithoutRepeats() {
        assertEquals("Paris, Texas, United States", new Place("Paris", "Texas", "United States", 0, 0, 0).fullName());
        assertEquals("Kyiv, Ukraine", new Place("Kyiv", "Kyiv", "Ukraine", 0, 0, 0).fullName());
    }
}
