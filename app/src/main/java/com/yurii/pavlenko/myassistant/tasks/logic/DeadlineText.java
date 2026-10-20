package com.yurii.pavlenko.myassistant.tasks.logic;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/** The days-left arithmetic of a task deadline. */
public final class DeadlineText {

    private DeadlineText() {
    }

    /** Whole days from today to the deadline; negative once the deadline has passed. */
    public static long daysLeft(LocalDate today, LocalDate deadline) {
        return ChronoUnit.DAYS.between(today, deadline);
    }

    public static boolean isOverdue(LocalDate today, LocalDate deadline) {
        return daysLeft(today, deadline) < 0;
    }
}
