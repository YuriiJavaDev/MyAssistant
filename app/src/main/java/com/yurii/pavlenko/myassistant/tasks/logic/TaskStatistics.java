package com.yurii.pavlenko.myassistant.tasks.logic;

/** The counts shown above the task list; progress is the whole percent of completed tasks. */
public final class TaskStatistics {

    public final int total;
    public final int completed;
    public final int left;
    public final int progress;

    public TaskStatistics(int total, int completed) {
        this.total = total;
        this.completed = completed;
        this.left = total - completed;
        this.progress = total > 0 ? (completed * 100) / total : 0;
    }
}
