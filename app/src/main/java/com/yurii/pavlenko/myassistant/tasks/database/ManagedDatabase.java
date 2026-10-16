package com.yurii.pavlenko.myassistant.tasks.database;

import android.content.Context;

import com.yurii.pavlenko.myassistant.flights.database.FlightDatabase;
import com.yurii.pavlenko.myassistant.steps.database.StepDatabase;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** A database that is backed up and restored as one file; its required table tells the backups apart. */
public enum ManagedDatabase {

    TASKS(AppDatabase.DATABASE_NAME, "myassistant_tasks_backup.db", "tasks", AppDatabase::closeInstance, "myassistant_backup.db"),
    STEPS(StepDatabase.DATABASE_NAME, "myassistant_steps_backup.db", "step_days", StepDatabase::closeInstance),
    FLIGHTS(FlightDatabase.DATABASE_NAME, "myassistant_flights_backup.db", "flights", FlightDatabase::closeInstance);

    private final String databaseName;
    private final String backupFileName;
    private final String requiredTable;
    private final Runnable closeAction;
    private final List<String> olderBackupFileNames;

    /** @param olderBackupFileNames names this backup had in earlier versions, still read when restoring */
    ManagedDatabase(String databaseName, String backupFileName, String requiredTable, Runnable closeAction,
                    String... olderBackupFileNames) {
        this.databaseName = databaseName;
        this.backupFileName = backupFileName;
        this.requiredTable = requiredTable;
        this.closeAction = closeAction;
        this.olderBackupFileNames = Arrays.asList(olderBackupFileNames);
    }

    public String getBackupFileName() {
        return backupFileName;
    }

    /** The current backup name first, then the older ones, in the order they are looked for when restoring. */
    List<String> getRestoreFileNames() {
        List<String> names = new ArrayList<>();
        names.add(backupFileName);
        names.addAll(olderBackupFileNames);
        return names;
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
