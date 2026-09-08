package com.yurii.pavlenko.myassistant.tasks.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Task model representing a single task item with details, importance, deadline, and reminders.
 * Created: 2026-09-08
 */
public class Task {
    private UUID id;
    private String title;
    private boolean completed;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime completedAt;
    private String importance;
    private LocalDate deadline;
    private boolean remindSoundOneDayBefore;

    public Task() {}

    // Updated constructor including deadline and sound reminder parameters
    public Task(UUID id, String title, boolean completed, LocalDateTime createdAt,
                LocalDateTime updatedAt, LocalDateTime completedAt, String importance,
                LocalDate deadline, boolean remindSoundOneDayBefore) {
        this.id = id != null ? id : UUID.randomUUID();
        this.title = title;
        this.completed = completed;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.completedAt = completedAt;
        this.importance = (importance == null) ? "Normal" : importance;
        this.deadline = deadline;
        this.remindSoundOneDayBefore = remindSoundOneDayBefore;
    }

    public Task(String title) {
        this.id = UUID.randomUUID();
        this.title = title;
        this.completed = false;
        this.createdAt = LocalDateTime.now();
        this.importance = "Normal";
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public boolean isCompleted() { return completed; }
    public void setCompleted(boolean completed) { this.completed = completed; }

    public String getImportance() { return importance; }
    public void setImportance(String importance) { this.importance = importance; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }

    public LocalDate getDeadline() { return deadline; }
    public void setDeadline(LocalDate deadline) { this.deadline = deadline; }

    public boolean isRemindSoundOneDayBefore() { return remindSoundOneDayBefore; }
    public void setRemindSoundOneDayBefore(boolean remindSoundOneDayBefore) {
        this.remindSoundOneDayBefore = remindSoundOneDayBefore;
    }
}