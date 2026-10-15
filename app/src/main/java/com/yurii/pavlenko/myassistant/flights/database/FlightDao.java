package com.yurii.pavlenko.myassistant.flights.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.yurii.pavlenko.myassistant.flights.model.FlightPointRecord;
import com.yurii.pavlenko.myassistant.flights.model.FlightRecord;
import com.yurii.pavlenko.myassistant.flights.model.FlightReportRecord;

import java.util.List;

@Dao
public interface FlightDao {

    @Insert
    long insertFlight(FlightRecord flight);

    @Update
    void updateFlight(FlightRecord flight);

    @Insert
    void insertPoint(FlightPointRecord point);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void saveReport(FlightReportRecord report);

    @Query("SELECT * FROM flights ORDER BY startMillis DESC")
    LiveData<List<FlightRecord>> observeFlights();

    @Query("SELECT * FROM flights WHERE id = :id")
    FlightRecord flight(long id);

    @Query("SELECT * FROM flights WHERE finished = 0")
    List<FlightRecord> unfinishedFlights();

    @Query("SELECT * FROM flight_points WHERE flightId = :flightId ORDER BY timeMillis")
    List<FlightPointRecord> points(long flightId);

    @Query("SELECT pdf FROM flight_reports WHERE flightId = :flightId")
    byte[] reportPdf(long flightId);

    @Query("DELETE FROM flights WHERE id = :id")
    void deleteFlight(long id);
}
