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

@Dao
public interface TaskDao {

    @Query("SELECT * FROM tasks")
    LiveData<List<Task>> getAllTasks();

    @Insert
    long insertTask(Task task);

    @Update
    void updateTask(Task task);

    @Delete
    void deleteTask(Task task);

    @Query("DELETE FROM tasks WHERE isCompleted = 1")
    void deleteCompletedTasks();

    @Query("DELETE FROM tasks")
    void clearAllTasks();

    /** Counts not completed tasks whose reminder time has already come. */
    @Query("SELECT COUNT(*) FROM tasks WHERE customReminderDateTime <= :currentTime AND isCompleted = 0")
    int getDueTasksCount(LocalDateTime currentTime);

    /** Tasks whose alarms must be re-registered after a device reboot or an app update. */
    @Query("SELECT * FROM tasks WHERE customReminderDateTime IS NOT NULL AND isCompleted = 0")
    List<Task> getTasksWithActiveReminder();
}