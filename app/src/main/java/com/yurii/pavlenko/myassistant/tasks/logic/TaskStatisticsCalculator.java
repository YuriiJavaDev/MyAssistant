package com.yurii.pavlenko.myassistant.tasks.logic;

import com.yurii.pavlenko.myassistant.tasks.model.Task;

import java.util.List;
import java.util.Locale;

public final class TaskStatisticsCalculator {

    private TaskStatisticsCalculator() {
    }

    public static String calculateStatistics(List<Task> tasks) {
        int total = tasks.size();
        int completed = 0;
        for (Task task : tasks) {
            if (task.isCompleted()) {
                completed++;
            }
        }
        int progress = total > 0 ? (completed * 100) / total : 0;

        return String.format(Locale.US, "Total: %d  Completed: %d  Left: %d  Progress: %d%%", total, completed, total - completed, progress);
    }
}
