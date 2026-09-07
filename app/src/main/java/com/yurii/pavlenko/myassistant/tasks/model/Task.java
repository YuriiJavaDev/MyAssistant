package com.yurii.pavlenko.myassistant.tasks.model;

import java.time.LocalDateTime;
import java.util.UUID;

public class Task {
    private UUID id;
    private String title;
    private boolean completed;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime completedAt;
    private String importance;

    public Task() {}

    public Task(UUID id, String title, boolean completed, LocalDateTime createdAt,
                LocalDateTime updatedAt, LocalDateTime completedAt, String importance) {
        this.id = id != null ? id : UUID.randomUUID();
        this.title = title;
        this.completed = completed;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.completedAt = completedAt;
        this.importance = (importance == null) ? "Normal" : importance;
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
}