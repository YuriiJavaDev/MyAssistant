package com.yurii.pavlenko.myassistant.flights.model;

/** What is needed to tell whether a report is already in the cloud, without loading the PDF. */
public class FlightReportSummary {

    private final long flightId;
    private final long startMillis;
    private final long size;

    public FlightReportSummary(long flightId, long startMillis, long size) {
        this.flightId = flightId;
        this.startMillis = startMillis;
        this.size = size;
    }

    public long getFlightId() { return flightId; }
    public long getStartMillis() { return startMillis; }
    public long getSize() { return size; }
}
