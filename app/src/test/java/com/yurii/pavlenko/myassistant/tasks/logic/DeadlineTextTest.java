package com.yurii.pavlenko.myassistant.tasks.logic;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.time.LocalDate;

public class DeadlineTextTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 10, 18);

    @Test
    public void daysAheadAreCountedInWholeDays() {
        assertEquals("21.10.2026 — 3 days left!", DeadlineText.describe(TODAY, LocalDate.of(2026, 10, 21)));
    }

    @Test
    public void oneDayIsSingular() {
        assertEquals("19.10.2026 — 1 day left!", DeadlineText.describe(TODAY, LocalDate.of(2026, 10, 19)));
    }

    @Test
    public void todayIsZeroDaysLeftNotOverdue() {
        assertEquals("18.10.2026 — 0 days left!", DeadlineText.describe(TODAY, TODAY));
        assertFalse(DeadlineText.isOverdue(TODAY, TODAY));
    }

    @Test
    public void pastDeadlineSaysOverdue() {
        assertEquals("17.10.2026 — Overdue by 1 day!", DeadlineText.describe(TODAY, LocalDate.of(2026, 10, 17)));
        assertEquals("13.10.2026 — Overdue by 5 days!", DeadlineText.describe(TODAY, LocalDate.of(2026, 10, 13)));
        assertTrue(DeadlineText.isOverdue(TODAY, LocalDate.of(2026, 10, 17)));
    }

    @Test
    public void noDeadlineGivesNoText() {
        assertNull(DeadlineText.describe(TODAY, null));
    }

    @Test
    public void daysCrossMonthEndAndLeapDay() {
        assertEquals(3, DeadlineText.daysLeft(LocalDate.of(2026, 10, 30), LocalDate.of(2026, 11, 2)));
        assertEquals(2, DeadlineText.daysLeft(LocalDate.of(2028, 2, 28), LocalDate.of(2028, 3, 1)));
        assertEquals(1, DeadlineText.daysLeft(LocalDate.of(2027, 2, 28), LocalDate.of(2027, 3, 1)));
    }
}
