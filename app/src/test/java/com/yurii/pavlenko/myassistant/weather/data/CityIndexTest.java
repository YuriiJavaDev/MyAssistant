package com.yurii.pavlenko.myassistant.weather.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class CityIndexTest {

    private static final String LIST = ""
            + "Paris\tFR\t48.85341\t2.3488\t2138551\t|paris|parigi|париж|\n"
            + "Paris\tUS\t33.66094\t-95.55551\t24899\t|paris|\n"
            + "Parishville\tUS\t44.62\t-74.8\t800\t|parishville|\n"
            + "Kyiv\tUA\t50.45466\t30.5238\t2797553\t|kyiv|kiev|киів|киев|\n";

    private static InputStream list() {
        return new ByteArrayInputStream(LIST.getBytes(StandardCharsets.UTF_8));
    }

    @Test
    public void wholeNamesComeBeforeNamesThatOnlyStartLikeTheQuery() throws IOException {
        List<Place> found = CityIndex.search(list(), "Paris", 5);
        assertEquals(3, found.size());
        assertEquals("France", found.get(0).getCountry());
        assertEquals("United States", found.get(1).getCountry());
        assertEquals("Parishville", found.get(2).getName());
    }

    @Test
    public void findsACityByItsNameInAnotherLanguage() throws IOException {
        assertEquals("Kyiv", CityIndex.search(list(), "Киев", 5).get(0).getName());
        assertEquals("Kyiv", CityIndex.search(list(), "київ", 5).get(0).getName());
    }

    @Test
    public void ignoresCaseAndAccents() {
        assertEquals("paris", CityIndex.normalize("PÀRIS"));
        assertEquals("krakow", CityIndex.normalize("Kraków"));
    }

    @Test
    public void aSingleLetterFindsNothing() throws IOException {
        assertTrue(CityIndex.search(list(), "p", 5).isEmpty());
    }

    @Test
    public void respectsTheLimit() throws IOException {
        assertEquals(1, CityIndex.search(list(), "Paris", 1).size());
    }

    @Test
    public void bigCitiesAreMoreImportantThanSmallOnes() throws IOException {
        List<Place> found = CityIndex.search(list(), "Paris", 5);
        assertTrue(found.get(0).getImportance() > found.get(1).getImportance() * 2);
    }

    @Test
    public void findsTheNearestCityWithinReach() throws IOException {
        Place near = CityIndex.nearest(list(), 48.9, 2.4, 60);
        assertNotNull(near);
        assertEquals("Paris", near.getName());
        assertEquals("France", near.getCountry());
        assertNull(CityIndex.nearest(list(), 0, 0, 60));
    }

    @Test
    public void readsTheBundledList() throws IOException {
        File file = new File("src/main/assets/cities.tsv");
        for (String query : new String[]{"Харьков", "Мерефа", "ראשון לציון", "פתח תקווה", "Kharkiv"}) {
            try (InputStream in = new FileInputStream(file)) {
                assertTrue(query, !CityIndex.search(in, query, 5).isEmpty());
            }
        }
        try (InputStream in = new FileInputStream(file)) {
            Place place = CityIndex.nearest(in, 49.99, 36.23, 60);
            assertNotNull(place);
            assertEquals("Kharkiv", place.getName());
            assertEquals("Ukraine", place.getCountry());
        }
    }
}
