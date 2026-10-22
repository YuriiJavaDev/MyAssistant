package com.yurii.pavlenko.myassistant.about.logic;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class FeedbackMessageTest {

    private static final FeedbackMessage.Details DETAILS =
            new FeedbackMessage.Details("1.0", 1, "14", 34, "samsung SM-S918B", "uk");

    @Test
    public void subjectNamesTheAppAndTheVersion() {
        assertEquals("[MyAssistant] Feedback 1.0", FeedbackMessage.subject("1.0"));
    }

    @Test
    public void bodyWithoutDetailsIsTheTrimmedText() {
        assertEquals("It crashes", FeedbackMessage.body("  It crashes \n", false, DETAILS));
    }

    @Test
    public void bodyWithDetailsPutsThemUnderALine() {
        assertEquals("It crashes\n\n---\nApp: 1.0 (1)\nAndroid: 14 (API 34)\nPhone: samsung SM-S918B\nLanguage: uk",
                FeedbackMessage.body("It crashes", true, DETAILS));
    }
}
