package com.yurii.pavlenko.myassistant.scan.model;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import com.yurii.pavlenko.myassistant.scan.imaging.PaperFormat;

/** One scanned page: the JPEG lives in the app's files directory, only its description is stored in the database. */
@Entity(
        tableName = "scan_pages",
        foreignKeys = @ForeignKey(entity = ScanDocument.class, parentColumns = "id", childColumns = "documentId", onDelete = ForeignKey.CASCADE),
        indices = @Index("documentId")
)
public class ScanPage {

    @PrimaryKey(autoGenerate = true)
    private long id;
    private long documentId;
    private int position;
    private String imagePath;
    private String paperFormat;
    private int width;
    private int height;

    public ScanPage(long documentId, int position, String imagePath, String paperFormat, int width, int height) {
        this.documentId = documentId;
        this.position = position;
        this.imagePath = imagePath;
        this.paperFormat = paperFormat;
        this.width = width;
        this.height = height;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public long getDocumentId() { return documentId; }
    public int getPosition() { return position; }
    public String getImagePath() { return imagePath; }
    public String getPaperFormat() { return paperFormat; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }

    public PaperFormat resolvePaperFormat() {
        return PaperFormat.fromName(paperFormat);
    }
}
