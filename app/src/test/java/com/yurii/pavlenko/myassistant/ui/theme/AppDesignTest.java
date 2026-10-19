package com.yurii.pavlenko.myassistant.ui.theme;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

public class AppDesignTest {

    @Test
    public void everyDesignIsFoundByItsOwnId() {
        for (AppDesign design : AppDesign.values()) {
            assertSame(design, AppDesign.fromId(design.id));
        }
    }

    @Test
    public void missingOrUnknownIdFallsBackToClassic() {
        assertSame(AppDesign.CLASSIC, AppDesign.fromId(null));
        assertSame(AppDesign.CLASSIC, AppDesign.fromId(""));
        assertSame(AppDesign.CLASSIC, AppDesign.fromId("removed_in_a_later_version"));
    }

    @Test
    public void idIsCaseSensitiveBecauseItIsStoredAsWritten() {
        assertSame(AppDesign.CLASSIC, AppDesign.fromId("TOON"));
        assertSame(AppDesign.TOON, AppDesign.fromId("toon"));
    }

    @Test
    public void thereAreTenDesignsAndClassicComesFirst() {
        assertEquals(10, AppDesign.values().length);
        assertSame(AppDesign.CLASSIC, AppDesign.values()[0]);
    }

    @Test
    public void onlyClassicRunsOnTheBaseThemeAlone() {
        assertFalse(AppDesign.CLASSIC.hasOverlay());
        for (AppDesign design : AppDesign.values()) {
            if (design != AppDesign.CLASSIC) {
                assertTrue(design.id, design.hasOverlay());
            }
        }
    }

    @Test
    public void idsOverlaysAndTitlesAreDistinct() {
        Set<String> ids = new HashSet<>();
        Set<Integer> overlays = new HashSet<>();
        Set<Integer> titles = new HashSet<>();
        for (AppDesign design : AppDesign.values()) {
            assertTrue("duplicate id " + design.id, ids.add(design.id));
            assertTrue("duplicate title of " + design.id, titles.add(design.nameRes));
            if (design.hasOverlay()) {
                assertTrue("duplicate overlay of " + design.id, overlays.add(design.overlay));
            }
        }
    }
}
