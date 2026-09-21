package com.yurii.pavlenko.myassistant.tasks.model;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity(tableName = "tasks")
public class Task implements Serializable {

    @PrimaryKey(autoGenerate = true)
    private long id;

    private String title;
    private boolean isCompleted;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;
    private LocalDateTime updatedAt;
    private String importance;
    private LocalDate deadline;
    private boolean remindSoundOneDayBefore;
    private boolean showTimestamps;
    private LocalDate customReminderDate; // Поле для даты кастомного напоминания

    // Room uses this constructor to recreate the object from database
    public Task(long id, String title, boolean isCompleted, LocalDateTime createdAt,
                LocalDateTime completedAt, LocalDateTime updatedAt, String importance,
                LocalDate deadline, boolean remindSoundOneDayBefore, boolean showTimestamps,
                LocalDate customReminderDate) {
        this.id = id;
        this.title = title;
        this.isCompleted = isCompleted;
        this.createdAt = createdAt;
        this.completedAt = completedAt;
        this.updatedAt = updatedAt;
        this.importance = importance;
        this.deadline = deadline;
        this.remindSoundOneDayBefore = remindSoundOneDayBefore;
        this.showTimestamps = showTimestamps;
        this.customReminderDate = customReminderDate;
    }

    // Convenient constructor for creating new tasks in code (ignored by Room)
    @Ignore
    public Task(String title, String importance, LocalDate deadline, boolean isCompleted,
                boolean remindSoundOneDayBefore, boolean showTimestamps, LocalDate customReminderDate) {
        this.title = title;
        this.importance = importance;
        this.deadline = deadline;
        this.isCompleted = isCompleted;
        this.remindSoundOneDayBefore = remindSoundOneDayBefore;
        this.showTimestamps = showTimestamps;
        this.customReminderDate = customReminderDate;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    public void setCompleted(boolean completed) {
        isCompleted = completed;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getImportance() {
        return importance;
    }

    public void setImportance(String importance) {
        this.importance = importance;
    }

    public LocalDate getDeadline() {
        return deadline;
    }

    public void setDeadline(LocalDate deadline) {
        this.deadline = deadline;
    }

    public boolean isRemindSoundOneDayBefore() {
        return remindSoundOneDayBefore;
    }

    public void setRemindSoundOneDayBefore(boolean remindSoundOneDayBefore) {
        this.remindSoundOneDayBefore = remindSoundOneDayBefore;
    }

    public boolean isShowTimestamps() {
        return showTimestamps;
    }

    public void setShowTimestamps(boolean showTimestamps) {
        this.showTimestamps = showTimestamps;
    }

    public LocalDate getCustomReminderDate() {
        return customReminderDate;
    }

    public void setCustomReminderDate(LocalDate customReminderDate) {
        this.customReminderDate = customReminderDate;
    }
}