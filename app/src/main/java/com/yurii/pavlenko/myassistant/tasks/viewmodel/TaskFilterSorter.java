package com.yurii.pavlenko.myassistant.tasks.viewmodel;

import com.yurii.pavlenko.myassistant.tasks.model.Task;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class TaskFilterSorter {

    // Filter keys must match the labels of filter_options in strings.xml
    public static final String FILTER_ALL = "All Tasks";
    public static final String FILTER_ACTIVE = "Active";
    public static final String FILTER_COMPLETED = "Completed";
    public static final String FILTER_DUE_REMINDERS = "Due Reminders";

    public static List<Task> filterAndSort(List<Task> tasks, String filter, String sort) {
        List<Task> filtered = applyFilter(tasks, filter);
        applySort(filtered, sort);
        return filtered;
    }

    /** A reminder is due when its time has come and the task is not completed. */
    public static boolean isReminderDue(Task task, LocalDateTime now) {
        LocalDateTime reminder = task.getCustomReminderDateTime();
        return !task.isCompleted() && reminder != null && !reminder.isAfter(now);
    }

    private static List<Task> applyFilter(List<Task> tasks, String filter) {
        switch (filter) {
            case FILTER_ACTIVE:
                return tasks.stream().filter(t -> !t.isCompleted()).collect(Collectors.toList());
            case FILTER_COMPLETED:
                return tasks.stream().filter(Task::isCompleted).collect(Collectors.toList());
            case FILTER_DUE_REMINDERS: {
                LocalDateTime now = LocalDateTime.now();
                return tasks.stream().filter(t -> isReminderDue(t, now)).collect(Collectors.toList());
            }
            case FILTER_ALL:
            default:
                return new ArrayList<>(tasks);
        }
    }

    private static void applySort(List<Task> tasks, String sort) {
        switch (sort) {
            case "Alpha A-Z":
                tasks.sort(Comparator.comparing(Task::getTitle, String.CASE_INSENSITIVE_ORDER));
                break;
            case "Alpha Z-A":
                tasks.sort(Comparator.comparing(Task::getTitle, String.CASE_INSENSITIVE_ORDER).reversed());
                break;
            case "Status":
                tasks.sort(Comparator.comparing(Task::isCompleted));
                break;
            case "Created":
                tasks.sort(Comparator.comparing(Task::getCreatedAt, Comparator.nullsLast(Comparator.naturalOrder())).reversed());
                break;
            case "Edited":
                tasks.sort(Comparator.comparing(Task::getUpdatedAt, Comparator.nullsLast(Comparator.naturalOrder())).reversed());
                break;
            case "Completed":
                tasks.sort(Comparator.comparing(Task::getCompletedAt, Comparator.nullsLast(Comparator.naturalOrder())).reversed());
                break;
            case "Importance":
                tasks.sort(Comparator.comparingInt(t -> getImportanceWeight(t.getImportance())));
                break;
            case "Deadline":
                tasks.sort(Comparator.comparing(Task::getDeadline, Comparator.nullsLast(Comparator.naturalOrder())));
                break;
        }
    }

    private static int getImportanceWeight(String importance) {
        if (importance == null) return 3;
        switch (importance.toLowerCase()) {
            case "urgent":
                return 1;
            case "important":
                return 2;
            case "normal":
            default:
                return 3;
        }
    }
}