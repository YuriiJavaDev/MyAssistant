package com.yurii.pavlenko.myassistant.tasks.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.yurii.pavlenko.myassistant.tasks.model.Task;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Data Access Object (DAO) for managing Task entities in the local Room database.
 *
 * @date 2026-10-01
 */
@Dao
public interface TaskDao {

    /**
     * Step 1: Retrieve all tasks as LiveData for reactive UI observation.
     */
    @Query("SELECT * FROM tasks")
    LiveData<List<Task>> getAllTasks();

    /**
     * Step 2: Insert a new task record into the database.
     */
    @Insert
    long insertTask(Task task);

    /**
     * Step 3: Update an existing task record in the database.
     */
    @Update
    void updateTask(Task task);

    /**
     * Step 4: Delete a specific task from the database.
     */
    @Delete
    void deleteTask(Task task);

    /**
     * Step 5: Delete all tasks that are marked as completed.
     */
    @Query("DELETE FROM tasks WHERE isCompleted = 1")
    void deleteCompletedTasks();

    /**
     * Step 6: Clear all task records from the table.
     */
    @Query("DELETE FROM tasks")
    void clearAllTasks();

    /**
     * Step 7: Get total count of completed tasks.
     */
    @Query("SELECT COUNT(*) FROM tasks WHERE isCompleted = 1")
    int getCompletedTasksCount();

    /**
     * Step 8: Get total count of all stored tasks.
     */
    @Query("SELECT COUNT(*) FROM tasks")
    int getTotalTasksCount();

    /**
     * Step 9: Fetch all tasks whose custom reminder time is due or has already passed.
     *
     * @param currentTime The current timestamp to compare against.
     * @return List of tasks requiring deadline alerts.
     */
    @Query("SELECT * FROM tasks WHERE customReminderDateTime <= :currentTime AND isCompleted = 0")
    List<Task> getDueTasks(LocalDateTime currentTime);
}