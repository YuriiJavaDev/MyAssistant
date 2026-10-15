package com.yurii.pavlenko.myassistant.flights.report;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.pdf.PdfDocument;

import com.yurii.pavlenko.myassistant.flights.data.FlightUnits;
import com.yurii.pavlenko.myassistant.flights.logic.Coastline;
import com.yurii.pavlenko.myassistant.flights.logic.FlightFormat;
import com.yurii.pavlenko.myassistant.flights.logic.FlightPoint;
import com.yurii.pavlenko.myassistant.flights.logic.FlightTime;
import com.yurii.pavlenko.myassistant.flights.model.FlightRecord;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/** Builds the PDF report of a flight: the route on a map, the statistics beside it and the profiles below. */
public final class FlightReportBuilder {

    private static final int PAGE_WIDTH = 842;
    private static final int PAGE_HEIGHT = 595;
    private static final float MARGIN = 28f;
    private static final float GAP = 16f;
    private static final float MAP_TOP = 76f;
    private static final float MAP_HEIGHT = 318f;
    private static final float MAP_WIDTH = 486f;
    private static final float CHART_TOP = 410f;
    private static final float CHART_HEIGHT = 148f;
    private static final float ROW_HEIGHT = 25f;

    private final Coastline coastline;
    private final Paint headline = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint subtitle = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint label = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint value = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint rule = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint footer = new Paint(Paint.ANTI_ALIAS_FLAG);

    public FlightReportBuilder(Context context) {
        this(CoastlineAsset.load(context));
    }

    FlightReportBuilder(Coastline coastline) {
        this.coastline = coastline;
        headline.setColor(ReportColors.HEADER);
        headline.setTextSize(22);
        headline.setTypeface(Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD));
        subtitle.setColor(ReportColors.MUTED);
        subtitle.setTextSize(11);
        label.setColor(ReportColors.MUTED);
        label.setTextSize(10.5f);
        value.setColor(ReportColors.TEXT);
        value.setTextSize(11.5f);
        value.setTextAlign(Paint.Align.RIGHT);
        value.setTypeface(Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD));
        rule.setColor(ReportColors.LINE);
        rule.setStrokeWidth(0.6f);
        footer.setColor(ReportColors.MUTED);
        footer.setTextSize(7.5f);
    }

    /**
     * @param points the position fixes of the flight, in order; not empty
     * @return the bytes of a one-page PDF
     */
    public byte[] build(FlightRecord flight, List<FlightPoint> points, FlightUnits units) throws IOException {
        PdfDocument document = new PdfDocument();
        try {
            PdfDocument.Page page = document.startPage(new PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create());
            drawPage(page.getCanvas(), flight, points, units);
            document.finishPage(page);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            document.writeTo(out);
            return out.toByteArray();
        } finally {
            document.close();
        }
    }

    /** Draws the whole page on a canvas of PAGE_WIDTH by PAGE_HEIGHT units. */
    void drawPage(Canvas canvas, FlightRecord flight, List<FlightPoint> points, FlightUnits units) {
        drawHeader(canvas, flight);
        new RouteMapPainter(coastline).draw(canvas,
                new RectF(MARGIN, MAP_TOP, MARGIN + MAP_WIDTH, MAP_TOP + MAP_HEIGHT), points, units.getSpeed());
        drawStatistics(canvas, flight, points, units);
        drawProfiles(canvas, flight, points, units);
        canvas.drawText("Map: Natural Earth (public domain). Recorded with MyAssistant. "
                + "For fun only: not for navigation or safety.", MARGIN, PAGE_HEIGHT - 14, footer);
    }

    private void drawHeader(Canvas canvas, FlightRecord flight) {
        canvas.drawText("Flight report", MARGIN, 44, headline);
        SimpleDateFormat day = new SimpleDateFormat("EEEE, d MMMM yyyy", Locale.ENGLISH);
        SimpleDateFormat time = new SimpleDateFormat("HH:mm", Locale.ENGLISH);
        canvas.drawText(day.format(new Date(flight.getStartMillis())) + "   "
                + time.format(new Date(flight.getStartMillis())) + " - " + time.format(new Date(flight.getEndMillis())),
                MARGIN, 64, subtitle);
    }

    private void drawStatistics(Canvas canvas, FlightRecord flight, List<FlightPoint> points, FlightUnits units) {
        float left = MARGIN + MAP_WIDTH + GAP;
        float right = PAGE_WIDTH - MARGIN;
        String altitude = FlightFormat.altitude(flight.getMaxAltitude(), units.getAltitude())
                + (flight.isAltitudeFromStart() ? " above start" : " above sea level");
        String[][] rows = {
                {"Duration", FlightTime.format(flight.getDurationMillis())},
                {"Distance", FlightFormat.distance(flight.getDistanceMetres(), units.getSpeed())},
                {"Top speed", FlightFormat.speed(flight.getMaxSpeed(), units.getSpeed())},
                {"Average speed", FlightFormat.speed(flight.getAverageSpeed(), units.getSpeed())},
                {"Highest altitude", altitude},
                {"Best climb", FlightFormat.climbRate(flight.getMaxClimb(), units.getClimb())},
                {"Worst sink", FlightFormat.climbRate(flight.getMaxSink(), units.getClimb())},
                {"Peak load", FlightFormat.gForce(flight.getMaxGForce())},
                {"Start point", FlightFormat.position(points.get(0))},
                {"End point", FlightFormat.position(points.get(points.size() - 1))},
        };
        float y = MAP_TOP + 14;
        for (String[] row : rows) {
            canvas.drawText(row[0], left, y, label);
            canvas.drawText(row[1], right, y, value);
            canvas.drawLine(left, y + 7, right, y + 7, rule);
            y += ROW_HEIGHT;
        }
    }

    private void drawProfiles(Canvas canvas, FlightRecord flight, List<FlightPoint> points, FlightUnits units) {
        List<Double> seconds = new ArrayList<>();
        List<Double> altitudes = new ArrayList<>();
        List<Double> speeds = new ArrayList<>();
        long start = points.get(0).getTimeMillis();
        for (FlightPoint point : points) {
            seconds.add((point.getTimeMillis() - start) / 1000.0);
            altitudes.add(units.getAltitude().fromMetres(point.getAltitudeMetres()));
            speeds.add(units.getSpeed().fromMetresPerSecond(point.getSpeedMetresPerSecond()));
        }
        float width = (PAGE_WIDTH - 2 * MARGIN - GAP) / 2;
        ProfileChartPainter charts = new ProfileChartPainter();
        charts.draw(canvas, new RectF(MARGIN, CHART_TOP, MARGIN + width, CHART_TOP + CHART_HEIGHT),
                "Altitude (" + units.getAltitude().symbol() + (flight.isAltitudeFromStart() ? ", above start)" : ", above sea level)"),
                seconds, altitudes, ReportColors.ALTITUDE);
        charts.draw(canvas, new RectF(MARGIN + width + GAP, CHART_TOP, PAGE_WIDTH - MARGIN, CHART_TOP + CHART_HEIGHT),
                "Ground speed (" + units.getSpeed().symbol() + ")", seconds, speeds, ReportColors.SPEED);
    }
}
