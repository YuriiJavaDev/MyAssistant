package com.yurii.pavlenko.myassistant.steps.logic;

import java.util.List;

/** Adds up the recorded days that fall inside a range. */
public final class PeriodSummarizer {

    private PeriodSummarizer() {
    }

    public static PeriodSummary summarize(List<DayTotals> days, long fromEpochDay, long toEpochDay, StepLengths lengths) {
        long walk = 0;
        long run = 0;
        int daysWithSteps = 0;
        long bestDay = fromEpochDay;
        int bestSteps = 0;
        for (DayTotals day : days) {
            if (day.getEpochDay() < fromEpochDay || day.getEpochDay() > toEpochDay) {
                continue;
            }
            walk += day.getWalkSteps();
            run += day.getRunSteps();
            if (day.totalSteps() > 0) {
                daysWithSteps++;
            }
            if (day.totalSteps() > bestSteps) {
                bestSteps = day.totalSteps();
                bestDay = day.getEpochDay();
            }
        }
        int dayCount = (int) (toEpochDay - fromEpochDay + 1);
        return new PeriodSummary(dayCount, daysWithSteps, walk, run, lengths.distanceMeters(walk, run), bestDay, bestSteps);
    }
}
