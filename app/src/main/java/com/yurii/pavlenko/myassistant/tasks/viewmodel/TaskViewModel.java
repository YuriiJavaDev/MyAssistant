package com.yurii.pavlenko.myassistant.tasks.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.yurii.pavlenko.myassistant.tasks.model.Task;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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
        allTasks.addAll(TaskMockDataSource.getInitialTasks());
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
        List<Task> processedTasks = TaskFilterSorter.filterAndSort(allTasks, currentFilter, currentSort);
        displayListLiveData.setValue(processedTasks);
        updateStatistics();
    }

    public void createNewTask(String title, String importance, LocalDate deadline, boolean remindSound) {
        Task newTask = new Task(title);
        newTask.setImportance(importance);
        newTask.setDeadline(deadline);
        newTask.setRemindSoundOneDayBefore(remindSound);

        allTasks.add(newTask);
        applyFilterAndSort();
    }

    public void updateTaskCompletion(Task task, boolean isChecked) {
        task.setCompleted(isChecked);
        task.setCompletedAt(isChecked ? LocalDateTime.now() : null);
        task.setUpdatedAt(LocalDateTime.now());
        applyFilterAndSort();
    }

    public void updateTaskDetails(Task task, String title, String importance, LocalDate deadline, boolean remindSound) {
        task.setTitle(title);
        task.setImportance(importance);
        task.setDeadline(deadline);
        task.setRemindSoundOneDayBefore(remindSound);
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
        String statsText = TaskStatisticsCalculator.calculateStatistics(allTasks);
        statisticsLiveData.setValue(statsText);
    }
}