package com.yurii.pavlenko.myassistant.flights.data;

import com.yurii.pavlenko.myassistant.flights.logic.AltitudeUnit;
import com.yurii.pavlenko.myassistant.flights.logic.SpeedUnit;

/** The units in which the instruments show speed and altitude. */
public final class FlightUnits {

    private final SpeedUnit speed;
    private final AltitudeUnit altitude;

    public FlightUnits(SpeedUnit speed, AltitudeUnit altitude) {
        this.speed = speed;
        this.altitude = altitude;
    }

    public SpeedUnit getSpeed() {
        return speed;
    }

    public AltitudeUnit getAltitude() {
        return altitude;
    }
}
