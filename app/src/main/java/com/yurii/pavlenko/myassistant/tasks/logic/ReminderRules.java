package com.yurii.pavlenko.myassistant.tasks.logic;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/** What a reminder may be, relative to its deadline and to the present. */
public final class ReminderRules {

    /** A deadline lasts to the end of its day. */
    private static final LocalTime END_OF_DAY = LocalTime.of(23, 59, 59);

    private ReminderRules() {
    }

    /** True when the reminder falls after the end of the deadline day; false if either one is missing. */
    public static boolean isLaterThanDeadline(LocalDateTime reminder, LocalDate deadline) {
        return reminder != null && deadline != null && reminder.isAfter(deadline.atTime(END_OF_DAY));
    }

    /** True when the whole day is after the deadline day, so no time on it can be chosen. */
    public static boolean isDayAfterDeadline(LocalDate day, LocalDate deadline) {
        return deadline != null && day.isAfter(deadline);
    }

    /** A reminder that has already passed on a task that is still open. */
    public static boolean isStale(LocalDateTime reminder, boolean taskCompleted, LocalDateTime now) {
        return reminder != null && !taskCompleted && reminder.isBefore(now);
    }
}
