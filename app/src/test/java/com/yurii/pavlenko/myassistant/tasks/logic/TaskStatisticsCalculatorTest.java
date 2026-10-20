package com.yurii.pavlenko.myassistant.tasks.logic;

import static org.junit.Assert.assertEquals;

import com.yurii.pavlenko.myassistant.tasks.model.Task;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TaskStatisticsCalculatorTest {

    private static List<Task> tasks(int total, int completed) {
        List<Task> tasks = new ArrayList<>();
        for (int i = 0; i < total; i++) {
            tasks.add(new Task("task " + i, "Normal", null, i < completed, false, false, null));
        }
        return tasks;
    }

    private static void assertStats(int total, int completed, int left, int progress, TaskStatistics stats) {
        assertEquals(total, stats.total);
        assertEquals(completed, stats.completed);
        assertEquals(left, stats.left);
        assertEquals(progress, stats.progress);
    }

    @Test
    public void emptyListShowsZeroProgressWithoutDividingByZero() {
        assertStats(0, 0, 0, 0, TaskStatisticsCalculator.calculateStatistics(Collections.emptyList()));
    }

    @Test
    public void progressIsTheWholePercentRoundedDown() {
        assertStats(3, 1, 2, 33, TaskStatisticsCalculator.calculateStatistics(tasks(3, 1)));
        assertStats(3, 2, 1, 66, TaskStatisticsCalculator.calculateStatistics(tasks(3, 2)));
    }

    @Test
    public void allCompletedIsOneHundredPercent() {
        assertStats(4, 4, 0, 100, TaskStatisticsCalculator.calculateStatistics(tasks(4, 4)));
    }
}
