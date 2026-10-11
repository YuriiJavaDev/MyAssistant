package com.yurii.pavlenko.myassistant.scan.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.yurii.pavlenko.myassistant.scan.imaging.PaperFormat;

/** A saved scan: the JPEG lives in the app's files directory, only its description is stored in the database. */
@Entity(tableName = "scans")
public class ScanItem {

    @PrimaryKey(autoGenerate = true)
    private long id;
    private String title;
    private String imagePath;
    private String paperFormat;
    private int width;
    private int height;
    private long createdAt;

    public ScanItem(String title, String imagePath, String paperFormat, int width, int height, long createdAt) {
        this.title = title;
        this.imagePath = imagePath;
        this.paperFormat = paperFormat;
        this.width = width;
        this.height = height;
        this.createdAt = createdAt;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getImagePath() { return imagePath; }
    public String getPaperFormat() { return paperFormat; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public long getCreatedAt() { return createdAt; }

    public PaperFormat resolvePaperFormat() {
        return PaperFormat.fromName(paperFormat);
    }
}
