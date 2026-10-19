package com.yurii.pavlenko.myassistant.tasks.logic;

import static org.junit.Assert.assertEquals;

import com.yurii.pavlenko.myassistant.tasks.model.Task;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class TaskStatisticsCalculatorTest {

    private static List<Task> tasks(int total, int completed) {
        List<Task> tasks = new ArrayList<>();
        for (int i = 0; i < total; i++) {
            tasks.add(new Task("task " + i, "Normal", null, i < completed, false, false, null));
        }
        return tasks;
    }

    @Test
    public void emptyListShowsZeroProgressWithoutDividingByZero() {
        assertEquals("Total: 0  Completed: 0  Left: 0  Progress: 0%",
                TaskStatisticsCalculator.calculateStatistics(Collections.emptyList()));
    }

    @Test
    public void progressIsTheWholePercentRoundedDown() {
        assertEquals("Total: 3  Completed: 1  Left: 2  Progress: 33%", TaskStatisticsCalculator.calculateStatistics(tasks(3, 1)));
        assertEquals("Total: 3  Completed: 2  Left: 1  Progress: 66%", TaskStatisticsCalculator.calculateStatistics(tasks(3, 2)));
    }

    @Test
    public void allCompletedIsOneHundredPercent() {
        assertEquals("Total: 4  Completed: 4  Left: 0  Progress: 100%", TaskStatisticsCalculator.calculateStatistics(tasks(4, 4)));
    }

    @Test
    public void digitsStayWesternWhateverTheDeviceLanguage() {
        Locale saved = Locale.getDefault();
        try {
            Locale.setDefault(new Locale("ar", "EG"));
            assertEquals("Total: 2  Completed: 1  Left: 1  Progress: 50%", TaskStatisticsCalculator.calculateStatistics(tasks(2, 1)));
        } finally {
            Locale.setDefault(saved);
        }
    }
}
