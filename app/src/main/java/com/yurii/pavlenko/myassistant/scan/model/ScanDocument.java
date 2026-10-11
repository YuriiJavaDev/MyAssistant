package com.yurii.pavlenko.myassistant.scan.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

/** A named set of scanned pages that is exported as one PDF. */
@Entity(tableName = "scan_documents")
public class ScanDocument {

    @PrimaryKey(autoGenerate = true)
    private long id;
    private String title;
    private long createdAt;

    public ScanDocument(String title, long createdAt) {
        this.title = title;
        this.createdAt = createdAt;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public long getCreatedAt() { return createdAt; }
}
