package com.yurii.pavlenko.myassistant.steps.logic;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

/** Assigns the steps of an interval to calendar days, walking or running by the interval's cadence. */
public final class DaySplitter {

    private DaySplitter() {
    }

    /** An interval that crosses midnight is divided between the days in proportion to the time spent in each. */
    public static List<DayTotals> split(StepBurst burst, double runCadenceThreshold, ZoneId zone) {
        boolean running = burst.cadence() >= runCadenceThreshold;
        List<DayTotals> shares = new ArrayList<>();

        long totalMillis = burst.getToMillis() - burst.getFromMillis();
        long cursor = burst.getFromMillis();
        int assigned = 0;
        while (cursor < burst.getToMillis()) {
            LocalDate day = Instant.ofEpochMilli(cursor).atZone(zone).toLocalDate();
            long dayEnd = day.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli();
            long segmentEnd = Math.min(dayEnd, burst.getToMillis());
            boolean last = segmentEnd == burst.getToMillis();

            int steps = last
                    ? burst.getSteps() - assigned
                    : (int) Math.round(burst.getSteps() * ((segmentEnd - cursor) / (double) totalMillis));
            assigned += steps;
            shares.add(new DayTotals(day.toEpochDay(), running ? 0 : steps, running ? steps : 0));
            cursor = segmentEnd;
        }
        return shares;
    }
}
