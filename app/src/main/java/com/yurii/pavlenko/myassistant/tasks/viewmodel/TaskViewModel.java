package com.yurii.pavlenko.myassistant.tasks.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.yurii.pavlenko.myassistant.tasks.model.Task;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class TaskViewModel extends ViewModel {

    private final List<Task> allTasks = new ArrayList<>();
    private final MutableLiveData<List<Task>> displayListLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> statisticsLiveData = new MutableLiveData<>();

    private String currentFilter = "All Tasks";
    private String currentSort = "Alpha A-Z";

    public TaskViewModel() {
        if (allTasks.isEmpty()) {
            loadInitialData();
        }
    }

    public LiveData<List<Task>> getDisplayList() {
        return displayListLiveData;
    }

    public LiveData<String> getStatistics() {
        return statisticsLiveData;
    }

    private void loadInitialData() {
        allTasks.add(new Task(UUID.randomUUID(), "Drink a cup of coffee", false, LocalDateTime.now().minusHours(3), null, null, "Normal"));
        allTasks.add(new Task(UUID.randomUUID(), "Read Java textbook", false, LocalDateTime.now().minusHours(2), null, null, "Important"));
        allTasks.add(new Task(UUID.randomUUID(), "Sleep before and after lunch", false, LocalDateTime.now().minusHours(1), null, null, "Urgent"));
        applyFilterAndSort();
    }

    public void setFilter(String filter) {
        this.currentFilter = filter;
        applyFilterAndSort();
    }

    public void setSort(String sort) {
        this.currentSort = sort;
        applyFilterAndSort();
    }

    public void applyFilterAndSort() {
        List<Task> filtered;
        switch (currentFilter) {
            case "Active":
                filtered = allTasks.stream().filter(t -> !t.isCompleted()).collect(Collectors.toList());
                break;
            case "Completed":
                filtered = allTasks.stream().filter(Task::isCompleted).collect(Collectors.toList());
                break;
            case "All Tasks":
            default:
                filtered = new ArrayList<>(allTasks);
                break;
        }

        switch (currentSort) {
            case "Alpha A-Z":
                filtered.sort(Comparator.comparing(Task::getTitle, String.CASE_INSENSITIVE_ORDER));
                break;
            case "Alpha Z-A":
                filtered.sort(Comparator.comparing(Task::getTitle, String.CASE_INSENSITIVE_ORDER).reversed());
                break;
            case "Status":
                filtered.sort(Comparator.comparing(Task::isCompleted));
                break;
            case "Created":
                filtered.sort(Comparator.comparing(Task::getCreatedAt, Comparator.nullsLast(Comparator.naturalOrder())).reversed());
                break;
            case "Edited":
                filtered.sort(Comparator.comparing(Task::getUpdatedAt, Comparator.nullsLast(Comparator.naturalOrder())).reversed());
                break;
            case "Completed":
                filtered.sort(Comparator.comparing(Task::getCompletedAt, Comparator.nullsLast(Comparator.naturalOrder())).reversed());
                break;
            case "Importance":
                filtered.sort(Comparator.comparingInt(t -> getImportanceWeight(t.getImportance())));
                break;
        }

        displayListLiveData.setValue(filtered);
        updateStatistics();
    }

    private int getImportanceWeight(String importance) {
        if (importance == null) return 3;
        switch (importance.toLowerCase()) {
            case "urgent": return 1;
            case "important": return 2;
            case "normal": default: return 3;
        }
    }

    public void createNewTask(String title, String importance) {
        Task newTask = new Task(UUID.randomUUID(), title, false, LocalDateTime.now(), null, null, importance);
        allTasks.add(0, newTask);
        applyFilterAndSort();
    }

    public void updateTaskCompletion(Task task, boolean isChecked) {
        task.setCompleted(isChecked);
        task.setCompletedAt(isChecked ? LocalDateTime.now() : null);
        task.setUpdatedAt(LocalDateTime.now());
        applyFilterAndSort();
    }

    public void updateTaskDetails(Task task, String title, String importance) {
        task.setTitle(title);
        task.setImportance(importance);
        task.setUpdatedAt(LocalDateTime.now());
        applyFilterAndSort();
    }

    public void deleteTask(Task task) {
        allTasks.remove(task);
        applyFilterAndSort();
    }

    public boolean hasCompletedTasks() {
        return allTasks.stream().anyMatch(Task::isCompleted);
    }

    public void deleteCompletedTasks() {
        allTasks.removeIf(Task::isCompleted);
        applyFilterAndSort();
    }

    public boolean hasTasks() {
        return !allTasks.isEmpty();
    }

    public void clearAllTasks() {
        allTasks.clear();
        applyFilterAndSort();
    }

    private void updateStatistics() {
        int total = allTasks.size();
        int completed = (int) allTasks.stream().filter(Task::isCompleted).count();
        int left = total - completed;
        int progress = total > 0 ? (completed * 100) / total : 0;

        String statsText = String.format("Total: %d  Completed: %d  Left: %d  Progress: %d%%", total, completed, left, progress);
        statisticsLiveData.setValue(statsText);
    }
}