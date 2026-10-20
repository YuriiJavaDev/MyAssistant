package com.yurii.pavlenko.myassistant.tasks.logic;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.time.LocalDate;

public class DeadlineTextTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 10, 18);

    @Test
    public void daysAheadAreCountedInWholeDays() {
        assertEquals(3, DeadlineText.daysLeft(TODAY, LocalDate.of(2026, 10, 21)));
        assertFalse(DeadlineText.isOverdue(TODAY, LocalDate.of(2026, 10, 21)));
    }

    @Test
    public void tomorrowIsOneDayLeft() {
        assertEquals(1, DeadlineText.daysLeft(TODAY, LocalDate.of(2026, 10, 19)));
    }

    @Test
    public void todayIsZeroDaysAndNotOverdue() {
        assertEquals(0, DeadlineText.daysLeft(TODAY, TODAY));
        assertFalse(DeadlineText.isOverdue(TODAY, TODAY));
    }

    @Test
    public void pastDeadlineIsNegativeAndOverdue() {
        assertEquals(-1, DeadlineText.daysLeft(TODAY, LocalDate.of(2026, 10, 17)));
        assertEquals(-5, DeadlineText.daysLeft(TODAY, LocalDate.of(2026, 10, 13)));
        assertTrue(DeadlineText.isOverdue(TODAY, LocalDate.of(2026, 10, 17)));
    }

    @Test
    public void daysCrossMonthEndAndLeapDay() {
        assertEquals(3, DeadlineText.daysLeft(LocalDate.of(2026, 10, 30), LocalDate.of(2026, 11, 2)));
        assertEquals(2, DeadlineText.daysLeft(LocalDate.of(2028, 2, 28), LocalDate.of(2028, 3, 1)));
        assertEquals(1, DeadlineText.daysLeft(LocalDate.of(2027, 2, 28), LocalDate.of(2027, 3, 1)));
    }
}
