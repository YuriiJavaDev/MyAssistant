package com.yurii.pavlenko.myassistant.tasks.logic;

import com.yurii.pavlenko.myassistant.tasks.model.Task;

import java.time.LocalDateTime;

/** What happens to a task's stamps when its state changes. */
public final class TaskLifecycle {

    private TaskLifecycle() {
    }

    /** Marks the task completed or open again; the completion time is kept only while it is completed. */
    public static void setCompleted(Task task, boolean completed, LocalDateTime now) {
        task.setCompleted(completed);
        task.setCompletedAt(completed ? now : null);
        task.setUpdatedAt(now);
    }

    /** True when the task still needs an alarm: it is open and has a reminder. */
    public static boolean needsAlarm(Task task) {
        return !task.isCompleted() && task.getCustomReminderDateTime() != null;
    }
}
