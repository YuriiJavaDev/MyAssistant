package com.yurii.pavlenko.myassistant.steps.viewmodel;

import com.yurii.pavlenko.myassistant.steps.logic.DayTotals;
import com.yurii.pavlenko.myassistant.steps.logic.PeriodSummary;

import java.util.List;

/** The summary of the chosen period together with the days that have steps, newest first. */
public final class PeriodStats {

    private final long fromEpochDay;
    private final long toEpochDay;
    private final PeriodSummary summary;
    private final List<DayTotals> days;

    PeriodStats(long fromEpochDay, long toEpochDay, PeriodSummary summary, List<DayTotals> days) {
        this.fromEpochDay = fromEpochDay;
        this.toEpochDay = toEpochDay;
        this.summary = summary;
        this.days = days;
    }

    public long getFromEpochDay() {
        return fromEpochDay;
    }

    public long getToEpochDay() {
        return toEpochDay;
    }

    public PeriodSummary getSummary() {
        return summary;
    }

    public List<DayTotals> getDays() {
        return days;
    }
}
