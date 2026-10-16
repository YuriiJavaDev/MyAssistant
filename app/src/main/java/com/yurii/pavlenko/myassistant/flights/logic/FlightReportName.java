package com.yurii.pavlenko.myassistant.flights.logic;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/** The file name of a flight report, the same wherever the report is saved or sent. */
public final class FlightReportName {

    private FlightReportName() {
    }

    public static String of(long startMillis) {
        return "flight_" + new SimpleDateFormat("yyyyMMdd_HHmm", Locale.US).format(new Date(startMillis)) + ".pdf";
    }
}
