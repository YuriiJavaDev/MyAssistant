package com.yurii.pavlenko.myassistant.scan.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import com.yurii.pavlenko.myassistant.scan.model.ScanItem;

import java.util.List;

@Dao
public interface ScanDao {

    @Insert
    long insert(ScanItem item);

    @Delete
    void delete(ScanItem item);

    @Query("UPDATE scans SET title = :title WHERE id = :id")
    void rename(long id, String title);

    @Query("SELECT * FROM scans ORDER BY createdAt DESC")
    LiveData<List<ScanItem>> getAll();
}
