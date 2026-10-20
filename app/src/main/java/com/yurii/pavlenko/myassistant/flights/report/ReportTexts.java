package com.yurii.pavlenko.myassistant.flights.report;

import android.content.Context;

import com.yurii.pavlenko.myassistant.R;
import com.yurii.pavlenko.myassistant.flights.logic.Hemispheres;
import com.yurii.pavlenko.myassistant.ui.locale.AppLocale;

import java.util.Locale;

/** Every word of the flight report, in the language chosen for the app. */
final class ReportTexts {

    private final Context context;

    ReportTexts(Context context) {
        this.context = AppLocale.localized(context);
    }

    Locale locale() {
        return AppLocale.current();
    }

    String title() {
        return context.getString(R.string.flights_report_title);
    }

    String north() {
        return context.getString(R.string.flights_report_north);
    }

    Hemispheres hemispheres() {
        return new Hemispheres(context.getString(R.string.flights_report_side_north),
                context.getString(R.string.flights_report_side_south),
                context.getString(R.string.flights_report_side_east),
                context.getString(R.string.flights_report_side_west));
    }

    String duration() {
        return context.getString(R.string.flights_report_duration);
    }

    String distance() {
        return context.getString(R.string.flights_report_distance);
    }

    String topSpeed() {
        return context.getString(R.string.flights_report_top_speed);
    }

    String averageSpeed() {
        return context.getString(R.string.flights_report_average_speed);
    }

    String highestAltitude() {
        return context.getString(R.string.flights_report_highest_altitude);
    }

    String bestClimb() {
        return context.getString(R.string.flights_report_best_climb);
    }

    String worstSink() {
        return context.getString(R.string.flights_report_worst_sink);
    }

    String peakLoad() {
        return context.getString(R.string.flights_report_peak_load);
    }

    String startPoint() {
        return context.getString(R.string.flights_report_start_point);
    }

    String endPoint() {
        return context.getString(R.string.flights_report_end_point);
    }

    String altitudeWithReference(String altitude, boolean fromStart) {
        return context.getString(fromStart ? R.string.flights_report_altitude_above_start
                : R.string.flights_report_altitude_above_sea_level, altitude);
    }

    String altitudeChartTitle(String unitSymbol, boolean fromStart) {
        return context.getString(fromStart ? R.string.flights_report_chart_altitude_above_start
                : R.string.flights_report_chart_altitude_above_sea_level, unitSymbol);
    }

    String speedChartTitle(String unitSymbol) {
        return context.getString(R.string.flights_report_chart_speed, unitSymbol);
    }

    /** The credit line under the map, with the source of the picture. */
    String footer(String imageryCredit) {
        String mapCredit = imageryCredit != null ? imageryCredit
                : context.getString(R.string.flights_report_map_credit_coastline);
        return context.getString(R.string.flights_report_footer, mapCredit);
    }
}
