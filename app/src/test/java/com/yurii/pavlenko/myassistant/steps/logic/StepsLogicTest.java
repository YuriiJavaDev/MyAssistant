package com.yurii.pavlenko.myassistant.steps.logic;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.List;

public class StepsLogicTest {

    private static final ZoneId ZONE = ZoneId.of("Europe/Kyiv");
    private static final long MINUTE = 60_000L;

    private static long at(int month, int day, int hour, int minute) {
        return LocalDateTime.of(2026, month, day, hour, minute).atZone(ZONE).toInstant().toEpochMilli();
    }

    @Test
    public void firstReadingHasNothingToCompareWith() {
        assertTrue(CounterTracker.between(null, new StepSample(500, 1000)) == null);
    }

    @Test
    public void counterDeltaIsTheNewSteps() {
        StepBurst burst = CounterTracker.between(new StepSample(1000, 0), new StepSample(1450, 10 * MINUTE));
        assertEquals(450, burst.getSteps());
    }

    @Test
    public void counterResetAfterRebootCountsAllNewSteps() {
        StepBurst burst = CounterTracker.between(new StepSample(90_000, 0), new StepSample(300, 10 * MINUTE));
        assertEquals(300, burst.getSteps());
    }

    @Test
    public void noNewStepsGivesNoBurst() {
        assertTrue(CounterTracker.between(new StepSample(10, 0), new StepSample(10, MINUTE)) == null);
    }

    @Test
    public void cadenceIsStepsPerMinute() {
        assertEquals(120, new StepBurst(1200, 0, 10 * MINUTE).cadence(), 1e-9);
    }

    @Test
    public void slowCadenceIsWalkingAndFastIsRunning() {
        long from = at(5, 10, 10, 0);
        StepBurst walk = new StepBurst(1000, from, from + 10 * MINUTE);
        StepBurst run = new StepBurst(1600, from, from + 10 * MINUTE);

        DayTotals walked = DaySplitter.split(walk, 135, ZONE).get(0);
        DayTotals ran = DaySplitter.split(run, 135, ZONE).get(0);

        assertEquals(1000, walked.getWalkSteps());
        assertEquals(0, walked.getRunSteps());
        assertEquals(1600, ran.getRunSteps());
        assertEquals(0, ran.getWalkSteps());
    }

    @Test
    public void intervalAcrossMidnightIsSplitByTimeAndKeepsEverySingleStep() {
        StepBurst burst = new StepBurst(1001, at(5, 10, 23, 45), at(5, 11, 0, 15));

        List<DayTotals> shares = DaySplitter.split(burst, 135, ZONE);

        assertEquals(2, shares.size());
        assertEquals(LocalDate.of(2026, 5, 10).toEpochDay(), shares.get(0).getEpochDay());
        assertEquals(LocalDate.of(2026, 5, 11).toEpochDay(), shares.get(1).getEpochDay());
        assertEquals(1001, shares.get(0).totalSteps() + shares.get(1).totalSteps());
        assertEquals(500, shares.get(0).totalSteps(), 1);
    }

    @Test
    public void intervalLongerThanADaySpansAllDays() {
        StepBurst burst = new StepBurst(3000, at(5, 10, 12, 0), at(5, 12, 12, 0));

        List<DayTotals> shares = DaySplitter.split(burst, 135, ZONE);

        assertEquals(3, shares.size());
        int sum = 0;
        for (DayTotals s : shares) {
            sum += s.totalSteps();
        }
        assertEquals(3000, sum);
    }

    @Test
    public void distanceUsesWalkingAndRunningLengths() {
        StepLengths lengths = new StepLengths(70, 100, 135);
        assertEquals(1000 * 0.7 + 500 * 1.0, lengths.distanceMeters(1000, 500), 1e-9);
    }

    @Test
    public void summaryIncludesBothEndsAndAveragesOverEveryDay() {
        long d1 = LocalDate.of(2026, 5, 1).toEpochDay();
        List<DayTotals> days = Arrays.asList(
                new DayTotals(d1 - 1, 9999, 0),
                new DayTotals(d1, 4000, 0),
                new DayTotals(d1 + 2, 2000, 2000),
                new DayTotals(d1 + 4, 7777, 0));

        PeriodSummary s = PeriodSummarizer.summarize(days, d1, d1 + 3, new StepLengths(75, 105, 135));

        assertEquals(4, s.getDayCount());
        assertEquals(2, s.getDaysWithSteps());
        assertEquals(8000, s.totalSteps());
        assertEquals(6000, s.getWalkSteps());
        assertEquals(2000, s.getRunSteps());
        assertEquals(2000, s.averageStepsPerDay());
        assertEquals(4000, s.getBestDaySteps());
        assertEquals(d1, s.getBestDayEpoch());
        assertEquals(6000 * 0.75 + 2000 * 1.05, s.getDistanceMeters(), 1e-9);
    }

    @Test
    public void singleDayRangeWithoutDataIsAllZero() {
        long d = LocalDate.of(2026, 5, 1).toEpochDay();
        PeriodSummary s = PeriodSummarizer.summarize(Arrays.<DayTotals>asList(), d, d, StepLengths.defaults());
        assertEquals(1, s.getDayCount());
        assertEquals(0, s.totalSteps());
        assertEquals(0, s.averageStepsPerDay());
    }
}
