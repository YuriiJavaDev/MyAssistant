package com.yurii.pavlenko.myassistant.tasks.logic;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Locale;

/** The days-left arithmetic and the deadline wording shown on a task. */
public final class DeadlineText {

    private static final DateTimeFormatter DAY = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    private DeadlineText() {
    }

    /** Whole days from today to the deadline; negative once the deadline has passed. */
    public static long daysLeft(LocalDate today, LocalDate deadline) {
        return ChronoUnit.DAYS.between(today, deadline);
    }

    public static boolean isOverdue(LocalDate today, LocalDate deadline) {
        return daysLeft(today, deadline) < 0;
    }

    /** For example "05.11.2026 — 3 days left!" or "01.11.2026 — Overdue by 1 day!"; null without a deadline. */
    public static String describe(LocalDate today, LocalDate deadline) {
        if (deadline == null) {
            return null;
        }
        long daysLeft = daysLeft(today, deadline);
        if (daysLeft < 0) {
            return String.format(Locale.US, "%s — Overdue by %d %s!", deadline.format(DAY), -daysLeft, dayWord(-daysLeft));
        }
        return String.format(Locale.US, "%s — %d %s left!", deadline.format(DAY), daysLeft, dayWord(daysLeft));
    }

    private static String dayWord(long days) {
        return days == 1 ? "day" : "days";
    }
}
