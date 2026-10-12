package com.yurii.pavlenko.myassistant.steps.ui;

import android.content.Context;

import com.yurii.pavlenko.myassistant.R;

import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Locale;

final class StepFormat {

    private static final DateTimeFormatter DATE = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(Locale.ENGLISH);

    private StepFormat() {
    }

    static String steps(long steps) {
        return NumberFormat.getIntegerInstance().format(steps);
    }

    static String distance(Context context, double meters) {
        return context.getString(R.string.steps_distance_km, meters / 1000.0);
    }

    static String date(long epochDay) {
        return LocalDate.ofEpochDay(epochDay).format(DATE);
    }
}
