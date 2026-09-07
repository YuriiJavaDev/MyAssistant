package com.yurii.pavlenko.myassistant.tasks.viewmodel;

import com.yurii.pavlenko.myassistant.tasks.model.Task;

import java.util.List;

public class TaskStatisticsCalculator {

    public static String calculateStatistics(List<Task> tasks) {
        int total = tasks.size();
        int completed = (int) tasks.stream().filter(Task::isCompleted).count();
        int left = total - completed;
        int progress = total > 0 ? (completed * 100) / total : 0;

        return String.format("Total: %d  Completed: %d  Left: %d  Progress: %d%%", total, completed, left, progress);
    }
}