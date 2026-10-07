package com.yurii.pavlenko.myassistant.tasks.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.yurii.pavlenko.myassistant.tasks.database.AppDatabase;
import com.yurii.pavlenko.myassistant.tasks.database.TaskDao;
import com.yurii.pavlenko.myassistant.tasks.model.Task;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.LongConsumer;

/**
 * Repository component to abstract data operations from the ViewModel.
 */
public class TaskRepository {

    private final TaskDao taskDao;
    private final LiveData<List<Task>> allTasksLiveData;
    private final ExecutorService executorService;

    public TaskRepository(Application application) {
        // Initialize Room database instance and DAO
        AppDatabase database = AppDatabase.getInstance(application);
        taskDao = database.taskDao();

        // Fetch live tasks stream from database
        allTasksLiveData = taskDao.getAllTasks();

        // Executor service for handling background database write operations
        executorService = Executors.newSingleThreadExecutor();
    }

    /**
     * Returns LiveData containing the list of all tasks.
     */
    public LiveData<List<Task>> getAllTasksLiveData() {
        return allTasksLiveData;
    }

    /**
     * Inserts a new task into the database on a background thread.
     */
    public void insert(Task task) {
        insert(task, null);
    }

    /**
     * Inserts a new task on a background thread. The generated ID is written back into the task
     * and passed to the callback, which runs on the same background thread right after the insert.
     */
    public void insert(Task task, LongConsumer onInserted) {
        executorService.execute(() -> {
            long newId = taskDao.insertTask(task);
            task.setId(newId);
            if (onInserted != null) {
                onInserted.accept(newId);
            }
        });
    }

    /**
     * Updates an existing task in the database on a background thread.
     */
    public void update(Task task) {
        executorService.execute(() -> taskDao.updateTask(task));
    }

    /**
     * Deletes a task from the database on a background thread.
     */
    public void delete(Task task) {
        executorService.execute(() -> taskDao.deleteTask(task));
    }

    /**
     * Deletes all completed tasks from the database on a background thread.
     */
    public void deleteCompletedTasks() {
        executorService.execute(taskDao::deleteCompletedTasks);
    }

    /**
     * Clears all tasks from the database on a background thread.
     */
    public void clearAllTasks() {
        executorService.execute(taskDao::clearAllTasks);
    }
}