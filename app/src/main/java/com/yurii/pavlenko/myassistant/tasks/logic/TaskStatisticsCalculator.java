package com.yurii.pavlenko.myassistant.tasks.logic;

import com.yurii.pavlenko.myassistant.tasks.model.Task;

import java.util.List;

public final class TaskStatisticsCalculator {

    private TaskStatisticsCalculator() {
    }

    public static TaskStatistics calculateStatistics(List<Task> tasks) {
        int completed = 0;
        for (Task task : tasks) {
            if (task.isCompleted()) {
                completed++;
            }
        }
        return new TaskStatistics(tasks.size(), completed);
    }
}
