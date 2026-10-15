package com.yurii.pavlenko.myassistant.flights.model;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/** One position fix of a flight, saved as it arrives so that a flight survives a crash. */
@Entity(tableName = "flight_points",
        foreignKeys = @ForeignKey(entity = FlightRecord.class, parentColumns = "id", childColumns = "flightId",
                onDelete = ForeignKey.CASCADE),
        indices = @Index("flightId"))
public class FlightPointRecord {

    @PrimaryKey(autoGenerate = true)
    private long id;
    private long flightId;
    private long timeMillis;
    private double latitude;
    private double longitude;
    private double altitude;
    private double speed;
    private double climb;
    private double gForce;

    public FlightPointRecord(long id, long flightId, long timeMillis, double latitude, double longitude,
                             double altitude, double speed, double climb, double gForce) {
        this.id = id;
        this.flightId = flightId;
        this.timeMillis = timeMillis;
        this.latitude = latitude;
        this.longitude = longitude;
        this.altitude = altitude;
        this.speed = speed;
        this.climb = climb;
        this.gForce = gForce;
    }

    public long getId() { return id; }
    public long getFlightId() { return flightId; }
    public long getTimeMillis() { return timeMillis; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
    public double getAltitude() { return altitude; }
    public double getSpeed() { return speed; }
    public double getClimb() { return climb; }
    public double getGForce() { return gForce; }
}
