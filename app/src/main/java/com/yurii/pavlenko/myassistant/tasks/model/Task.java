package com.yurii.pavlenko.myassistant.tasks.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public class Task {
    private UUID id;
    private String title;
    private boolean isCompleted;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;
    private LocalDateTime updatedAt;
    private String importance;
    private LocalDate deadline;
    private boolean remindSoundOneDayBefore;
    private boolean showTimestamps;

    // Full constructor
    public Task(UUID id, String title, boolean isCompleted, LocalDateTime createdAt,
                LocalDateTime completedAt, LocalDateTime updatedAt, String importance,
                LocalDate deadline, boolean remindSoundOneDayBefore, boolean showTimestamps) {
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
    }

    // Constructor for compatibility with MockDataSource
    public Task(UUID id, String title, boolean isCompleted, LocalDateTime createdAt,
                LocalDateTime completedAt, LocalDateTime updatedAt, String importance,
                LocalDate deadline, boolean showTimestamps) {
        this.id = id;
        this.title = title;
        this.isCompleted = isCompleted;
        this.createdAt = createdAt;
        this.completedAt = completedAt;
        this.updatedAt = updatedAt;
        this.importance = importance;
        this.deadline = deadline;
        this.remindSoundOneDayBefore = false;
        this.showTimestamps = showTimestamps;
    }

    // Convenient constructor for creating new tasks
    public Task(String title) {
        this.id = UUID.randomUUID();
        this.title = title;
        this.isCompleted = false;
        this.createdAt = LocalDateTime.now();
        this.importance = "Normal";
        this.remindSoundOneDayBefore = false;
        this.showTimestamps = true;
    }

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
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
}