package com.yurii.pavlenko.myassistant.tasks.logic;

import static org.junit.Assert.assertEquals;

import com.yurii.pavlenko.myassistant.tasks.model.Task;

import org.junit.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TaskFilterSorterTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 10, 18, 12, 0);

    private static Task task(String title, boolean completed) {
        return new Task(title, "Normal", null, completed, false, false, null);
    }

    private static Task task(String title, String importance) {
        return new Task(title, importance, null, false, false, false, null);
    }

    private static Task withReminder(String title, boolean completed, LocalDateTime reminder) {
        return new Task(title, "Normal", null, completed, false, false, reminder);
    }

    private static List<String> titles(List<Task> tasks) {
        List<String> titles = new ArrayList<>();
        for (Task task : tasks) {
            titles.add(task.getTitle());
        }
        return titles;
    }

    private static List<String> run(List<Task> tasks, TaskFilter filter, TaskSort sort) {
        return titles(TaskFilterSorter.filterAndSort(tasks, filter, sort, NOW));
    }

    @Test
    public void filtersSplitOpenAndCompletedTasks() {
        List<Task> tasks = Arrays.asList(task("open", false), task("done", true));

        assertEquals(Arrays.asList("open", "done"), run(tasks, TaskFilter.ALL, TaskSort.STATUS));
        assertEquals(Arrays.asList("open"), run(tasks, TaskFilter.ACTIVE, TaskSort.STATUS));
        assertEquals(Arrays.asList("done"), run(tasks, TaskFilter.COMPLETED, TaskSort.STATUS));
    }

    @Test
    public void dueAlertsKeepsOnlyOpenTasksWhoseReminderTimeHasCome() {
        List<Task> tasks = Arrays.asList(
                withReminder("past", false, NOW.minusMinutes(1)),
                withReminder("exactlyNow", false, NOW),
                withReminder("future", false, NOW.plusMinutes(1)),
                withReminder("completed", true, NOW.minusHours(3)),
                withReminder("none", false, null));

        assertEquals(Arrays.asList("exactlyNow", "past"), run(tasks, TaskFilter.DUE_ALERTS, TaskSort.ALPHA_A_Z));
    }

    @Test
    public void alphabeticalSortIgnoresCase() {
        List<Task> tasks = Arrays.asList(task("banana", false), task("Apple", false), task("cherry", false));

        assertEquals(Arrays.asList("Apple", "banana", "cherry"), run(tasks, TaskFilter.ALL, TaskSort.ALPHA_A_Z));
        assertEquals(Arrays.asList("cherry", "banana", "Apple"), run(tasks, TaskFilter.ALL, TaskSort.ALPHA_Z_A));
    }

    @Test
    public void statusSortListsOpenTasksFirstAndKeepsTheirOrder() {
        List<Task> tasks = Arrays.asList(task("d1", true), task("o1", false), task("d2", true), task("o2", false));

        assertEquals(Arrays.asList("o1", "o2", "d1", "d2"), run(tasks, TaskFilter.ALL, TaskSort.STATUS));
    }

    @Test
    public void importanceSortPutsUrgentFirstAndUnknownLabelsWithNormal() {
        List<Task> tasks = Arrays.asList(
                task("normal", "Normal"), task("unknown", "Whatever"), task("urgent", "urgent"),
                task("missing", (String) null), task("important", "IMPORTANT"));

        assertEquals(Arrays.asList("urgent", "important", "normal", "unknown", "missing"),
                run(tasks, TaskFilter.ALL, TaskSort.IMPORTANCE));
    }

    @Test
    public void deadlineSortPutsTheNearestFirstAndTasksWithoutDeadlineLast() {
        Task late = task("late", false);
        late.setDeadline(LocalDate.of(2026, 12, 1));
        Task soon = task("soon", false);
        soon.setDeadline(LocalDate.of(2026, 10, 20));
        Task none = task("none", false);

        assertEquals(Arrays.asList("soon", "late", "none"), run(Arrays.asList(none, late, soon), TaskFilter.ALL, TaskSort.DEADLINE));
    }

    @Test
    public void createdSortPutsTheNewestFirst() {
        Task older = task("older", false);
        older.setCreatedAt(LocalDateTime.of(2026, 1, 1, 8, 0));
        Task newer = task("newer", false);
        newer.setCreatedAt(LocalDateTime.of(2026, 6, 1, 8, 0));

        assertEquals(Arrays.asList("newer", "older"), run(Arrays.asList(older, newer), TaskFilter.ALL, TaskSort.CREATED));
    }

    @Test
    public void completedSortListsTheLatestCompletionFirst() {
        Task first = task("first", true);
        first.setCompletedAt(LocalDateTime.of(2026, 3, 1, 9, 0));
        Task second = task("second", true);
        second.setCompletedAt(LocalDateTime.of(2026, 4, 1, 9, 0));

        assertEquals(Arrays.asList("second", "first"), run(Arrays.asList(first, second), TaskFilter.COMPLETED, TaskSort.COMPLETED));
    }

    @Test
    public void inputListIsLeftUntouched() {
        List<Task> tasks = new ArrayList<>(Arrays.asList(task("b", false), task("a", true)));

        TaskFilterSorter.filterAndSort(tasks, TaskFilter.ACTIVE, TaskSort.ALPHA_A_Z, NOW);

        assertEquals(Arrays.asList("b", "a"), titles(tasks));
    }

    @Test
    public void spinnerPositionOutsideTheListFallsBackToTheDefaults() {
        assertEquals(TaskFilter.ALL, TaskFilter.fromPosition(-1));
        assertEquals(TaskFilter.ALL, TaskFilter.fromPosition(TaskFilter.values().length));
        assertEquals(TaskFilter.DUE_ALERTS, TaskFilter.fromPosition(3));
        assertEquals(TaskSort.ALPHA_A_Z, TaskSort.fromPosition(-1));
        assertEquals(TaskSort.ALPHA_A_Z, TaskSort.fromPosition(TaskSort.values().length));
        assertEquals(TaskSort.DEADLINE, TaskSort.fromPosition(7));
    }
}
