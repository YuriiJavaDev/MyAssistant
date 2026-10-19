package com.yurii.pavlenko.myassistant.tasks.logic;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class ReminderRulesTest {

    private static final LocalDate DEADLINE = LocalDate.of(2026, 10, 20);
    private static final LocalDateTime NOW = LocalDateTime.of(2026, 10, 18, 12, 0, 0);

    @Test
    public void reminderUntilTheLastSecondOfTheDeadlineDayIsAllowed() {
        assertFalse(ReminderRules.isLaterThanDeadline(LocalDateTime.of(2026, 10, 20, 23, 59, 59), DEADLINE));
        assertFalse(ReminderRules.isLaterThanDeadline(LocalDateTime.of(2026, 10, 20, 0, 0, 0), DEADLINE));
    }

    @Test
    public void reminderFromMidnightAfterTheDeadlineIsTooLate() {
        assertTrue(ReminderRules.isLaterThanDeadline(LocalDateTime.of(2026, 10, 21, 0, 0, 0), DEADLINE));
    }

    @Test
    public void withoutReminderOrDeadlineNothingIsTooLate() {
        assertFalse(ReminderRules.isLaterThanDeadline(null, DEADLINE));
        assertFalse(ReminderRules.isLaterThanDeadline(LocalDateTime.of(2030, 1, 1, 0, 0), null));
    }

    @Test
    public void dayAfterTheDeadlineIsRejectedAndTheDeadlineDayIsNot() {
        assertTrue(ReminderRules.isDayAfterDeadline(LocalDate.of(2026, 10, 21), DEADLINE));
        assertFalse(ReminderRules.isDayAfterDeadline(DEADLINE, DEADLINE));
        assertFalse(ReminderRules.isDayAfterDeadline(LocalDate.of(2026, 10, 19), DEADLINE));
        assertFalse(ReminderRules.isDayAfterDeadline(LocalDate.of(2030, 1, 1), null));
    }

    @Test
    public void openTaskWithPassedReminderIsStale() {
        assertTrue(ReminderRules.isStale(NOW.minusSeconds(1), false, NOW));
    }

    @Test
    public void reminderAtThisVeryMomentIsNotStaleYet() {
        assertFalse(ReminderRules.isStale(NOW, false, NOW));
        assertFalse(ReminderRules.isStale(NOW.plusSeconds(1), false, NOW));
    }

    @Test
    public void completedTaskOrMissingReminderIsNeverStale() {
        assertFalse(ReminderRules.isStale(NOW.minusDays(3), true, NOW));
        assertFalse(ReminderRules.isStale(null, false, NOW));
    }
}
