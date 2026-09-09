package com.yurii.pavlenko.myassistant.tasks.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.yurii.pavlenko.myassistant.tasks.model.Task;

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

    @Query("SELECT COUNT(*) FROM tasks WHERE isCompleted = 1")
    int getCompletedTasksCount();

    @Query("SELECT COUNT(*) FROM tasks")
    int getTotalTasksCount();
}