package com.yurii.pavlenko.myassistant.tasks.logic;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import java.util.Locale;

public class ImportanceTest {

    @Test
    public void keysAreTheStoredWordsInSpinnerOrder() {
        assertEquals("Normal", Importance.values()[0].key);
        assertEquals("Important", Importance.values()[1].key);
        assertEquals("Urgent", Importance.values()[2].key);
    }

    @Test
    public void keyIsFoundWhateverItsCase() {
        assertEquals(Importance.URGENT, Importance.fromKey("urgent"));
        assertEquals(Importance.IMPORTANT, Importance.fromKey("IMPORTANT"));
        assertEquals(Importance.NORMAL, Importance.fromKey("Normal"));
    }

    @Test
    public void missingOrUnknownKeyMeansNormal() {
        assertEquals(Importance.NORMAL, Importance.fromKey(null));
        assertEquals(Importance.NORMAL, Importance.fromKey(""));
        assertEquals(Importance.NORMAL, Importance.fromKey("critical"));
    }

    @Test
    public void lookupDoesNotDependOnTheDeviceLanguage() {
        Locale saved = Locale.getDefault();
        try {
            Locale.setDefault(new Locale("tr", "TR"));
            assertEquals(Importance.IMPORTANT, Importance.fromKey("IMPORTANT"));
            assertEquals(Importance.IMPORTANT, Importance.fromKey("important"));
        } finally {
            Locale.setDefault(saved);
        }
    }

    @Test
    public void rankPutsUrgentBeforeImportantBeforeNormal() {
        assertEquals(1, Importance.URGENT.rank);
        assertEquals(2, Importance.IMPORTANT.rank);
        assertEquals(3, Importance.NORMAL.rank);
    }
}
