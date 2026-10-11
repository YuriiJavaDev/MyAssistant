package com.yurii.pavlenko.myassistant.scan.database;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.yurii.pavlenko.myassistant.scan.model.ScanDocument;
import com.yurii.pavlenko.myassistant.scan.model.ScanPage;

import java.io.File;

/** Separate from the task database, so scans are backed up and restored as their own archive (database and images). */
@Database(entities = {ScanDocument.class, ScanPage.class}, version = 2, exportSchema = false)
public abstract class ScanDatabase extends RoomDatabase {

    public static final String DATABASE_NAME = "scan_database";

    /** Version 1 stored single scans; each of them becomes a one-page document. */
    private static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase db) {
            db.execSQL("CREATE TABLE IF NOT EXISTS `scan_documents` ("
                    + "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `title` TEXT, `createdAt` INTEGER NOT NULL)");
            db.execSQL("CREATE TABLE IF NOT EXISTS `scan_pages` ("
                    + "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `documentId` INTEGER NOT NULL, `position` INTEGER NOT NULL, "
                    + "`imagePath` TEXT, `paperFormat` TEXT, `width` INTEGER NOT NULL, `height` INTEGER NOT NULL, "
                    + "FOREIGN KEY(`documentId`) REFERENCES `scan_documents`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE)");
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_scan_pages_documentId` ON `scan_pages` (`documentId`)");
            db.execSQL("INSERT INTO scan_documents (id, title, createdAt) SELECT id, title, createdAt FROM scans");
            db.execSQL("INSERT INTO scan_pages (documentId, position, imagePath, paperFormat, width, height) "
                    + "SELECT id, 0, imagePath, paperFormat, width, height FROM scans");
            db.execSQL("DROP TABLE scans");
        }
    };

    private static volatile ScanDatabase instance;

    public abstract ScanDao scanDao();

    public static ScanDatabase getInstance(Context context) {
        if (instance == null) {
            synchronized (ScanDatabase.class) {
                if (instance == null) {
                    instance = Room.databaseBuilder(context.getApplicationContext(), ScanDatabase.class, DATABASE_NAME)
                            .addMigrations(MIGRATION_1_2)
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
        synchronized (ScanDatabase.class) {
            if (instance != null) {
                instance.close();
                instance = null;
            }
        }
    }
}
