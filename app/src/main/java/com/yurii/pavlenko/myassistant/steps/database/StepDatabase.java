package com.yurii.pavlenko.myassistant.steps.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.yurii.pavlenko.myassistant.steps.model.StepDay;
import com.yurii.pavlenko.myassistant.steps.model.StepSettings;

import java.io.File;

/** Separate from the other databases, so step statistics are backed up and restored as their own file. */
@Database(entities = {StepDay.class, StepSettings.class}, version = 1, exportSchema = false)
public abstract class StepDatabase extends RoomDatabase {

    public static final String DATABASE_NAME = "step_database";

    private static volatile StepDatabase instance;

    public abstract StepDao stepDao();

    public static StepDatabase getInstance(Context context) {
        if (instance == null) {
            synchronized (StepDatabase.class) {
                if (instance == null) {
                    instance = Room.databaseBuilder(context.getApplicationContext(), StepDatabase.class, DATABASE_NAME)
                            // TRUNCATE keeps the whole database in one file, so copying that file is a valid backup
                            .setJournalMode(RoomDatabase.JournalMode.TRUNCATE)
                            .build();
                }
            }
        }
        return instance;
    }

    public static File getDatabaseFile(Context context) {
        return context.getDatabasePath(DATABASE_NAME);
    }

    /** Closes the singleton so the database file can be replaced; the next getInstance() reopens it. */
    public static void closeInstance() {
        synchronized (StepDatabase.class) {
            if (instance != null) {
                instance.close();
                instance = null;
            }
        }
    }
}
