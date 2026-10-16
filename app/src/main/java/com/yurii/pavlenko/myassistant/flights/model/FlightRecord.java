package com.yurii.pavlenko.myassistant.flights.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

/** The totals of one recorded flight. The route points and the report are kept in their own tables. */
@Entity(tableName = "flights")
public class FlightRecord {

    @PrimaryKey(autoGenerate = true)
    private long id;
    private long startMillis;
    private long endMillis;
    private double distanceMetres;
    private double maxSpeed;
    private double maxAltitude;
    private double maxClimb;
    private double maxSink;
    private double maxGForce;
    private boolean altitudeFromStart;
    private boolean finished;

    public FlightRecord(long id, long startMillis, long endMillis, double distanceMetres, double maxSpeed,
                        double maxAltitude, double maxClimb, double maxSink, double maxGForce,
                        boolean altitudeFromStart, boolean finished) {
        this.id = id;
        this.startMillis = startMillis;
        this.endMillis = endMillis;
        this.distanceMetres = distanceMetres;
        this.maxSpeed = maxSpeed;
        this.maxAltitude = maxAltitude;
        this.maxClimb = maxClimb;
        this.maxSink = maxSink;
        this.maxGForce = maxGForce;
        this.altitudeFromStart = altitudeFromStart;
        this.finished = finished;
    }

    public long getId() { return id; }
    public long getStartMillis() { return startMillis; }
    public long getEndMillis() { return endMillis; }
    public double getDistanceMetres() { return distanceMetres; }
    public double getMaxSpeed() { return maxSpeed; }
    public double getMaxAltitude() { return maxAltitude; }
    public double getMaxClimb() { return maxClimb; }
    public double getMaxSink() { return maxSink; }
    public double getMaxGForce() { return maxGForce; }
    public boolean isAltitudeFromStart() { return altitudeFromStart; }
    public boolean isFinished() { return finished; }

    public long getDurationMillis() {
        return Math.max(0, endMillis - startMillis);
    }

    /** Average speed over the whole time of the flight, in metres per second. */
    public double getAverageSpeed() {
        long millis = getDurationMillis();
        return millis <= 0 ? 0 : distanceMetres / (millis / 1000.0);
    }
}
