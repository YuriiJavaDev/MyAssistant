package com.yurii.pavlenko.myassistant.tasks.logic;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

import java.util.Locale;

public class ImportanceTest {

    @Test
    public void labelsAreTheStoredWordsInSpinnerOrder() {
        assertArrayEquals(new String[]{"Normal", "Important", "Urgent"}, Importance.labels());
    }

    @Test
    public void labelIsFoundWhateverItsCase() {
        assertEquals(Importance.URGENT, Importance.fromLabel("urgent"));
        assertEquals(Importance.IMPORTANT, Importance.fromLabel("IMPORTANT"));
        assertEquals(Importance.NORMAL, Importance.fromLabel("Normal"));
    }

    @Test
    public void missingOrUnknownLabelMeansNormal() {
        assertEquals(Importance.NORMAL, Importance.fromLabel(null));
        assertEquals(Importance.NORMAL, Importance.fromLabel(""));
        assertEquals(Importance.NORMAL, Importance.fromLabel("critical"));
    }

    @Test
    public void lookupDoesNotDependOnTheDeviceLanguage() {
        Locale saved = Locale.getDefault();
        try {
            Locale.setDefault(new Locale("tr", "TR"));
            assertEquals(Importance.IMPORTANT, Importance.fromLabel("IMPORTANT"));
            assertEquals(Importance.IMPORTANT, Importance.fromLabel("important"));
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
