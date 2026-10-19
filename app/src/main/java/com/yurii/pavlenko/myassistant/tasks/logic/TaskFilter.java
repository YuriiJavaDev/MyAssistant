package com.yurii.pavlenko.myassistant.tasks.logic;

import com.yurii.pavlenko.myassistant.tasks.model.Task;

import java.time.LocalDateTime;
import java.util.function.BiPredicate;

/** Which tasks the list shows. The order is the order of the filter_options array in strings.xml. */
public enum TaskFilter {
    ALL((task, now) -> true),
    ACTIVE((task, now) -> !task.isCompleted()),
    COMPLETED((task, now) -> task.isCompleted()),
    DUE_ALERTS(TaskFilter::isReminderDue);

    private final BiPredicate<Task, LocalDateTime> rule;

    TaskFilter(BiPredicate<Task, LocalDateTime> rule) {
        this.rule = rule;
    }

    public boolean matches(Task task, LocalDateTime now) {
        return rule.test(task, now);
    }

    /** The filter at a spinner position; All for a position outside the list. */
    public static TaskFilter fromPosition(int position) {
        TaskFilter[] all = values();
        return position >= 0 && position < all.length ? all[position] : ALL;
    }

    /** A reminder is due when its time has come and the task is not completed. */
    public static boolean isReminderDue(Task task, LocalDateTime now) {
        LocalDateTime reminder = task.getCustomReminderDateTime();
        return !task.isCompleted() && reminder != null && !reminder.isAfter(now);
    }
}
