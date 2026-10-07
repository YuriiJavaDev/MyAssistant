package com.yurii.pavlenko.myassistant.tasks.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.yurii.pavlenko.myassistant.tasks.model.Task;

@Database(entities = {Task.class}, version = 1, exportSchema = false)
@TypeConverters({Converters.class})
public abstract class AppDatabase extends RoomDatabase {

    public static final String DATABASE_NAME = "task_database";

    private static volatile AppDatabase instance;

    public abstract TaskDao taskDao();

    public static AppDatabase getInstance(Context context) {
        if (instance == null) {
            synchronized (AppDatabase.class) {
                if (instance == null) {
                    // TRUNCATE keeps the whole database in one file, so copying that file is a valid backup
                    instance = Room.databaseBuilder(context.getApplicationContext(), AppDatabase.class, DATABASE_NAME)
                            .setJournalMode(RoomDatabase.JournalMode.TRUNCATE)
                            .build();
                }
            }
        }
        return instance;
    }

    /** Closes the singleton so the database file can be replaced; the next getInstance() reopens it. */
    public static void closeInstance() {
        synchronized (AppDatabase.class) {
            if (instance != null) {
                instance.close();
                instance = null;
            }
        }
    }
}