package com.yurii.pavlenko.myassistant.flights.data;

import com.yurii.pavlenko.myassistant.flights.logic.AltitudeUnit;
import com.yurii.pavlenko.myassistant.flights.logic.ClimbUnit;
import com.yurii.pavlenko.myassistant.flights.logic.SpeedUnit;

/** The units in which the instruments show their readings. */
public final class FlightUnits {

    private final SpeedUnit speed;
    private final AltitudeUnit altitude;
    private final ClimbUnit climb;

    public FlightUnits(SpeedUnit speed, AltitudeUnit altitude, ClimbUnit climb) {
        this.speed = speed;
        this.altitude = altitude;
        this.climb = climb;
    }

    public SpeedUnit getSpeed() {
        return speed;
    }

    public AltitudeUnit getAltitude() {
        return altitude;
    }

    public ClimbUnit getClimb() {
        return climb;
    }
}
