package com.yurii.pavlenko.myassistant.weather.logic;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class BeachScenarioTest {

    private static BeachScenario scene(WeatherCondition condition, boolean day, double celsius, double wind) {
        return BeachScenario.of(condition, day, celsius, wind);
    }

    @Test
    public void hotSunnyDayHasSunbathersAndButterflies() {
        BeachScenario s = scene(WeatherCondition.CLEAR, true, 28, 2);
        assertEquals(Crowd.SUNBATHERS, s.getCrowd());
        assertEquals(Outfit.LIGHT, s.getOutfit());
        assertTrue(s.areParasolsOpen());
        assertTrue(s.hasButterflies());
        assertFalse(s.hasGulls());
        assertFalse(s.hasIceFloes());
        assertTrue(s.hasFlowers());
    }

    @Test
    public void nineDegreesMeansStrollersInJackets() {
        BeachScenario s = scene(WeatherCondition.CLEAR, true, 9, 2);
        assertEquals(Crowd.STROLLERS, s.getCrowd());
        assertEquals(Outfit.JACKET, s.getOutfit());
        assertFalse(s.hasButterflies());
        assertTrue(s.hasFlowers());
    }

    @Test
    public void frostMeansIceAndFurs() {
        BeachScenario s = scene(WeatherCondition.CLEAR, true, -6, 1);
        assertEquals(Crowd.STROLLERS, s.getCrowd());
        assertEquals(Outfit.FUR, s.getOutfit());
        assertTrue(s.hasIceFloes());
        assertFalse(s.hasFlowers());
        assertFalse(s.hasButterflies());
    }

    @Test
    public void coldAboveFreezingMeansCoats() {
        assertEquals(Outfit.COAT, scene(WeatherCondition.CLOUDY, true, 3, 1).getOutfit());
    }

    @Test
    public void rainSendsPeopleAwayUnderUmbrellasAndClosesParasols() {
        BeachScenario s = scene(WeatherCondition.RAIN, true, 14, 3);
        assertEquals(Crowd.UMBRELLA_WALKERS, s.getCrowd());
        assertFalse(s.areParasolsOpen());
    }

    @Test
    public void stormEmptiesTheBeachAndRaisesTheSea() {
        BeachScenario s = scene(WeatherCondition.STORM, true, 20, 9);
        assertEquals(Crowd.NONE, s.getCrowd());
        assertFalse(s.areParasolsOpen());
        assertEquals(1.0, s.getSeaRoughness(), 1e-9);
        assertFalse(s.hasGulls());
    }

    @Test
    public void strongWindClosesParasolsAndBringsGullsInsteadOfButterflies() {
        BeachScenario s = scene(WeatherCondition.CLEAR, true, 25, 9);
        assertFalse(s.areParasolsOpen());
        assertTrue(s.hasGulls());
        assertFalse(s.hasButterflies());
        assertEquals(Crowd.STROLLERS, s.getCrowd());
        assertTrue(s.getSeaRoughness() > 0.5);
    }

    @Test
    public void galeClearsTheBeach() {
        assertEquals(Crowd.NONE, scene(WeatherCondition.CLEAR, true, 25, 14).getCrowd());
    }

    @Test
    public void breezeBringsGullsButParasolsStayOpen() {
        BeachScenario s = scene(WeatherCondition.PARTLY_CLOUDY, true, 22, 6);
        assertTrue(s.hasGulls());
        assertTrue(s.areParasolsOpen());
        assertFalse(s.hasButterflies());
    }

    @Test
    public void nobodyIsOnTheBeachAtNightAndParasolsAreClosed() {
        BeachScenario s = scene(WeatherCondition.CLEAR, false, 24, 1);
        assertEquals(Crowd.NONE, s.getCrowd());
        assertFalse(s.areParasolsOpen());
        assertFalse(s.hasButterflies());
        assertFalse(s.hasGulls());
    }

    @Test
    public void snowfallMeansNoFlowers() {
        assertFalse(scene(WeatherCondition.SNOW, true, 1, 1).hasFlowers());
    }
}
