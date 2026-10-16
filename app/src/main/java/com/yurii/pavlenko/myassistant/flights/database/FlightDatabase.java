package com.yurii.pavlenko.myassistant.flights.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.yurii.pavlenko.myassistant.flights.model.FlightPointRecord;
import com.yurii.pavlenko.myassistant.flights.model.FlightRecord;
import com.yurii.pavlenko.myassistant.flights.model.FlightReportRecord;

/** Separate from the other databases, so the flights are backed up and restored as their own file. */
@Database(entities = {FlightRecord.class, FlightPointRecord.class, FlightReportRecord.class}, version = 2, exportSchema = false)
public abstract class FlightDatabase extends RoomDatabase {

    public static final String DATABASE_NAME = "flight_database";

    private static final Migration ADD_ONLINE_MAP = new Migration(1, 2) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE flight_reports ADD COLUMN onlineMap INTEGER NOT NULL DEFAULT 0");
        }
    };

    private static volatile FlightDatabase instance;

    public abstract FlightDao flightDao();

    public static FlightDatabase getInstance(Context context) {
        if (instance == null) {
            synchronized (FlightDatabase.class) {
                if (instance == null) {
                    instance = Room.databaseBuilder(context.getApplicationContext(), FlightDatabase.class, DATABASE_NAME)
                            // TRUNCATE keeps the whole database in one file, so copying that file is a valid backup
                            .setJournalMode(RoomDatabase.JournalMode.TRUNCATE)
                            .addMigrations(ADD_ONLINE_MAP)
                            .build();
                }
            }
        }
        return instance;
    }

    /** Closes the singleton so the database file can be replaced; the next getInstance() reopens it. */
    public static void closeInstance() {
        synchronized (FlightDatabase.class) {
            if (instance != null) {
                instance.close();
                instance = null;
            }
        }
    }
}
