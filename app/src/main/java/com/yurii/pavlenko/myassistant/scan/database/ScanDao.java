package com.yurii.pavlenko.myassistant.scan.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import com.yurii.pavlenko.myassistant.scan.model.ScanDocument;
import com.yurii.pavlenko.myassistant.scan.model.ScanDocumentSummary;
import com.yurii.pavlenko.myassistant.scan.model.ScanPage;

import java.util.List;

@Dao
public interface ScanDao {

    @Insert
    long insertDocument(ScanDocument document);

    @Insert
    long insertPage(ScanPage page);

    @Delete
    void deleteDocument(ScanDocument document);

    @Delete
    void deletePage(ScanPage page);

    @Query("UPDATE scan_documents SET title = :title WHERE id = :documentId")
    void renameDocument(long documentId, String title);

    @Query("UPDATE scan_pages SET position = :position WHERE id = :pageId")
    void setPagePosition(long pageId, int position);

    @Query("SELECT COALESCE(MAX(position), -1) + 1 FROM scan_pages WHERE documentId = :documentId")
    int nextPosition(long documentId);

    @Query("SELECT COUNT(*) FROM scan_pages WHERE documentId = :documentId")
    int countPages(long documentId);

    @Query("SELECT d.*, "
            + "(SELECT COUNT(*) FROM scan_pages WHERE documentId = d.id) AS pageCount, "
            + "(SELECT imagePath FROM scan_pages WHERE documentId = d.id ORDER BY position LIMIT 1) AS coverPath "
            + "FROM scan_documents d ORDER BY d.createdAt DESC")
    LiveData<List<ScanDocumentSummary>> getDocuments();

    @Query("SELECT * FROM scan_documents WHERE id = :documentId")
    LiveData<ScanDocument> getDocument(long documentId);

    @Query("SELECT * FROM scan_pages WHERE documentId = :documentId ORDER BY position")
    LiveData<List<ScanPage>> observePages(long documentId);

    @Query("SELECT * FROM scan_pages WHERE documentId = :documentId ORDER BY position")
    List<ScanPage> getPages(long documentId);

    /** Pages go with their documents through the foreign key cascade. */
    @Query("DELETE FROM scan_documents")
    void deleteAllDocuments();
}
