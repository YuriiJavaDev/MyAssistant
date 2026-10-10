package com.yurii.pavlenko.myassistant.scan.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.yurii.pavlenko.myassistant.scan.model.ScanItem;

/** Separate from the task database so task backup, import and cloud sync never touch scans. */
@Database(entities = {ScanItem.class}, version = 1, exportSchema = false)
public abstract class ScanDatabase extends RoomDatabase {

    private static final String DATABASE_NAME = "scan_database";

    private static volatile ScanDatabase instance;

    public abstract ScanDao scanDao();

    public static ScanDatabase getInstance(Context context) {
        if (instance == null) {
            synchronized (ScanDatabase.class) {
                if (instance == null) {
                    instance = Room.databaseBuilder(context.getApplicationContext(), ScanDatabase.class, DATABASE_NAME).build();
                }
            }
        }
        return instance;
    }
}
