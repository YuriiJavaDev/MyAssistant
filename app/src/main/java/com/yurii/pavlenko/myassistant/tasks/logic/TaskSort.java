package com.yurii.pavlenko.myassistant.tasks.logic;

import com.yurii.pavlenko.myassistant.tasks.model.Task;

import java.util.Comparator;
import java.util.function.Function;

/** How the list is ordered. The order is the order of the sort_options array in strings.xml. */
public enum TaskSort {
    ALPHA_A_Z(Comparator.comparing(Task::getTitle, String.CASE_INSENSITIVE_ORDER)),
    ALPHA_Z_A(Comparator.comparing(Task::getTitle, String.CASE_INSENSITIVE_ORDER).reversed()),
    STATUS(Comparator.comparing(Task::isCompleted)),
    CREATED(latestFirst(Task::getCreatedAt)),
    EDITED(latestFirst(Task::getUpdatedAt)),
    COMPLETED(latestFirst(Task::getCompletedAt)),
    IMPORTANCE(Comparator.comparingInt(task -> Importance.fromLabel(task.getImportance()).rank)),
    DEADLINE(Comparator.comparing(Task::getDeadline, Comparator.nullsLast(Comparator.naturalOrder())));

    private final Comparator<Task> order;

    TaskSort(Comparator<Task> order) {
        this.order = order;
    }

    public Comparator<Task> comparator() {
        return order;
    }

    /** The sort at a spinner position; A to Z for a position outside the list. */
    public static TaskSort fromPosition(int position) {
        TaskSort[] all = values();
        return position >= 0 && position < all.length ? all[position] : ALPHA_A_Z;
    }

    /** Latest time first; the order is reversed as a whole, so tasks without the time come before all others. */
    private static <T extends Comparable<? super T>> Comparator<Task> latestFirst(Function<Task, T> time) {
        return Comparator.comparing(time, Comparator.nullsLast(Comparator.<T>naturalOrder())).reversed();
    }
}
