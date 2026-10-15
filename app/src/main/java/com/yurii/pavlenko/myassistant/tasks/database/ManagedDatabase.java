package com.yurii.pavlenko.myassistant.tasks.database;

import android.content.Context;

import com.yurii.pavlenko.myassistant.flights.database.FlightDatabase;
import com.yurii.pavlenko.myassistant.steps.database.StepDatabase;

import java.io.File;

/** A database that is backed up and restored as one file; its required table tells the backups apart. */
public enum ManagedDatabase {

    TASKS(AppDatabase.DATABASE_NAME, "myassistant_backup.db", "tasks", AppDatabase::closeInstance),
    STEPS(StepDatabase.DATABASE_NAME, "myassistant_steps_backup.db", "step_days", StepDatabase::closeInstance),
    FLIGHTS(FlightDatabase.DATABASE_NAME, "myassistant_flights_backup.db", "flights", FlightDatabase::closeInstance);

    private final String databaseName;
    private final String backupFileName;
    private final String requiredTable;
    private final Runnable closeAction;

    ManagedDatabase(String databaseName, String backupFileName, String requiredTable, Runnable closeAction) {
        this.databaseName = databaseName;
        this.backupFileName = backupFileName;
        this.requiredTable = requiredTable;
        this.closeAction = closeAction;
    }

    public String getBackupFileName() {
        return backupFileName;
    }

    String getRequiredTable() {
        return requiredTable;
    }

    public File getFile(Context context) {
        return context.getDatabasePath(databaseName);
    }

    /** Closes the Room instance so the file can be replaced; the next use reopens it. */
    void close() {
        closeAction.run();
    }
}
