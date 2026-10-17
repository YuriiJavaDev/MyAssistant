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
import com.yurii.pavlenko.myassistant.flights.model.FlightReportSummary;

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

    /** Null when the flight has no report; true when the report has the online map. */
    @Query("SELECT onlineMap FROM flight_reports WHERE flightId = :flightId")
    Boolean reportOnlineMap(long flightId);

    @Query("SELECT length(pdf) FROM flight_reports WHERE flightId = :flightId")
    Long reportSize(long flightId);

    /** A piece of the PDF; the first byte is number 1. */
    @Query("SELECT substr(pdf, :firstByte, :length) FROM flight_reports WHERE flightId = :flightId")
    byte[] reportChunk(long flightId, long firstByte, int length);

    @Query("SELECT f.id AS flightId, f.startMillis AS startMillis, length(r.pdf) AS size FROM flights f "
            + "INNER JOIN flight_reports r ON r.flightId = f.id ORDER BY f.startMillis")
    List<FlightReportSummary> reportSummaries();

    @Query("DELETE FROM flights WHERE id = :id")
    void deleteFlight(long id);
}
