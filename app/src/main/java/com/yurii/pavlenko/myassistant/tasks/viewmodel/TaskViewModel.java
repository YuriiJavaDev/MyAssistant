package com.yurii.pavlenko.myassistant.tasks.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;

import com.yurii.pavlenko.myassistant.tasks.model.Task;
import com.yurii.pavlenko.myassistant.tasks.repository.TaskRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class TaskViewModel extends AndroidViewModel {

    private final TaskRepository repository;
    private final MediatorLiveData<List<Task>> displayListLiveData = new MediatorLiveData<>();
    private final MutableLiveData<String> statisticsLiveData = new MutableLiveData<>();

    private String currentFilter = "All Tasks";
    private String currentSort = "Alpha A-Z";

    private List<Task> cachedRawTasks = new ArrayList<>();

    public TaskViewModel(@NonNull Application application) {
        super(application);
        repository = new TaskRepository(application);

        // Observe raw tasks from repository and apply filtering/sorting whenever data changes
        LiveData<List<Task>> rawTasksSource = repository.getAllTasksLiveData();
        displayListLiveData.addSource(rawTasksSource, tasks -> {
            if (tasks != null) {
                cachedRawTasks = tasks;
            } else {
                cachedRawTasks = new ArrayList<>();
            }
            applyFilterAndSort();
        });
    }

    public LiveData<List<Task>> getDisplayList() {
        return displayListLiveData;
    }

    public LiveData<String> getStatistics() {
        return statisticsLiveData;
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
        List<Task> processedTasks = TaskFilterSorter.filterAndSort(cachedRawTasks, currentFilter, currentSort);
        displayListLiveData.setValue(processedTasks);
        updateStatistics();
    }

    public void createNewTask(String title, String importance, LocalDate deadline, boolean remindSound, boolean showTimestamps) {
        Task newTask = new Task(title, importance, deadline, false, remindSound, showTimestamps);
        repository.insert(newTask);
    }

    public void updateTaskCompletion(Task task, boolean isChecked) {
        task.setCompleted(isChecked);
        task.setCompletedAt(isChecked ? LocalDateTime.now() : null);
        task.setUpdatedAt(LocalDateTime.now());
        repository.update(task);
    }

    public void updateTaskDetails(Task task, String title, String importance, LocalDate deadline, boolean remindSound, boolean showTimestamps) {
        task.setTitle(title);
        task.setImportance(importance);
        task.setDeadline(deadline);
        task.setRemindSoundOneDayBefore(remindSound);
        task.setShowTimestamps(showTimestamps);
        task.setUpdatedAt(LocalDateTime.now());
        repository.update(task);
    }

    public void deleteTask(Task task) {
        repository.delete(task);
    }

    public boolean hasCompletedTasks() {
        return cachedRawTasks.stream().anyMatch(Task::isCompleted);
    }

    public void deleteCompletedTasks() {
        repository.deleteCompletedTasks();
    }

    public boolean hasTasks() {
        return !cachedRawTasks.isEmpty();
    }

    public void clearAllTasks() {
        repository.clearAllTasks();
    }

    private void updateStatistics() {
        String statsText = TaskStatisticsCalculator.calculateStatistics(cachedRawTasks);
        statisticsLiveData.setValue(statsText);
    }
}