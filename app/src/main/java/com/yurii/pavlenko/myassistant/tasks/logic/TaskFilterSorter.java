package com.yurii.pavlenko.myassistant.tasks.logic;

import com.yurii.pavlenko.myassistant.tasks.model.Task;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public final class TaskFilterSorter {

    private TaskFilterSorter() {
    }

    /** A new list with the tasks that pass the filter, in the given order; the input list is left as it is. */
    public static List<Task> filterAndSort(List<Task> tasks, TaskFilter filter, TaskSort sort, LocalDateTime now) {
        List<Task> result = new ArrayList<>();
        for (Task task : tasks) {
            if (filter.matches(task, now)) {
                result.add(task);
            }
        }
        result.sort(sort.comparator());
        return result;
    }
}
