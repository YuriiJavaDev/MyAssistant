package com.yurii.pavlenko.myassistant.flights.data;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.lifecycle.LiveData;

import com.yurii.pavlenko.myassistant.flights.database.FlightDao;
import com.yurii.pavlenko.myassistant.flights.database.FlightDatabase;
import com.yurii.pavlenko.myassistant.flights.logic.FlightPoint;
import com.yurii.pavlenko.myassistant.flights.logic.FlightStats;
import com.yurii.pavlenko.myassistant.flights.model.FlightPointRecord;
import com.yurii.pavlenko.myassistant.flights.model.FlightRecord;
import com.yurii.pavlenko.myassistant.flights.model.FlightReportRecord;
import com.yurii.pavlenko.myassistant.flights.report.FlightReportBuilder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

/**
 * Keeps the flights in their database: the points of the flight being recorded are saved as they arrive,
 * and when it ends the totals and the PDF report are saved too. All work happens on one background thread,
 * in the order it was asked for.
 */
public final class FlightRepository {

    private static final String TAG = "FlightRepository";
    private static final long NO_FLIGHT = 0;

    private static FlightRepository instance;

    private final Context context;
    private final ExecutorService io = Executors.newSingleThreadExecutor();
    private final Handler main = new Handler(Looper.getMainLooper());
    private long activeFlightId = NO_FLIGHT;

    private FlightRepository(Context context) {
        this.context = context.getApplicationContext();
    }

    public static synchronized FlightRepository get(Context context) {
        if (instance == null) {
            instance = new FlightRepository(context);
        }
        return instance;
    }

    private FlightDao dao() {
        return FlightDatabase.getInstance(context).flightDao();
    }

    public LiveData<List<FlightRecord>> observeFlights() {
        return dao().observeFlights();
    }

    public void beginFlight(long startMillis, boolean altitudeFromStart) {
        io.execute(() -> activeFlightId = dao().insertFlight(
                new FlightRecord(0, startMillis, startMillis, 0, 0, 0, 0, 0, 0, altitudeFromStart, false)));
    }

    /** @param gForce the highest load felt since the previous fix */
    public void appendPoint(FlightPoint point, double gForce) {
        io.execute(() -> {
            if (activeFlightId != NO_FLIGHT) {
                dao().insertPoint(new FlightPointRecord(0, activeFlightId, point.getTimeMillis(), point.getLatitude(),
                        point.getLongitude(), point.getAltitudeMetres(), point.getSpeedMetresPerSecond(),
                        point.getClimbMetresPerSecond(), gForce));
            }
        });
    }

    /** Saves the totals and builds the report; a flight without a single fix is not kept. */
    public void finishFlight(FlightStats stats, boolean altitudeFromStart) {
        io.execute(() -> {
            long id = activeFlightId;
            activeFlightId = NO_FLIGHT;
            if (id != NO_FLIGHT) {
                complete(id, stats, altitudeFromStart);
            }
        });
    }

    /** Completes flights that were cut short, for example when the app was killed during a recording. */
    public void recoverUnfinished() {
        io.execute(() -> {
            for (FlightRecord flight : dao().unfinishedFlights()) {
                FlightStats stats = FlightStats.empty();
                for (FlightPointRecord record : dao().points(flight.getId())) {
                    stats = stats.plus(toPoint(record), record.getGForce());
                }
                complete(flight.getId(), stats, flight.isAltitudeFromStart());
            }
        });
    }

    /** Hands over the PDF of a flight, building it first if it is missing; null when it cannot be had. */
    public void loadReport(long flightId, Consumer<byte[]> onDone) {
        io.execute(() -> {
            byte[] pdf = dao().reportPdf(flightId);
            if (pdf == null) {
                FlightRecord flight = dao().flight(flightId);
                pdf = flight != null && flight.isFinished() ? buildReport(flight) : null;
            }
            byte[] result = pdf;
            main.post(() -> onDone.accept(result));
        });
    }

    public void deleteFlight(long flightId) {
        io.execute(() -> dao().deleteFlight(flightId));
    }

    private void complete(long id, FlightStats stats, boolean altitudeFromStart) {
        if (stats.isEmpty()) {
            dao().deleteFlight(id);
            return;
        }
        FlightRecord flight = new FlightRecord(id, stats.getStartMillis(), stats.getEndMillis(),
                stats.getDistanceMetres(), stats.getMaxSpeed(), stats.getMaxAltitude(), stats.getMaxClimb(),
                stats.getMaxSink(), stats.getMaxGForce(), altitudeFromStart, true);
        dao().updateFlight(flight);
        buildReport(flight);
    }

    private byte[] buildReport(FlightRecord flight) {
        List<FlightPoint> points = pointsOf(flight.getId());
        if (points.isEmpty()) {
            return null;
        }
        try {
            byte[] pdf = new FlightReportBuilder(context).build(flight, points, new FlightUnitsStore(context).load());
            dao().saveReport(new FlightReportRecord(flight.getId(), pdf));
            return pdf;
        } catch (IOException | RuntimeException e) {
            Log.e(TAG, "Cannot build the flight report", e);
            return null;
        }
    }

    private List<FlightPoint> pointsOf(long flightId) {
        List<FlightPoint> points = new ArrayList<>();
        for (FlightPointRecord record : dao().points(flightId)) {
            points.add(toPoint(record));
        }
        return points;
    }

    private static FlightPoint toPoint(FlightPointRecord record) {
        return new FlightPoint(record.getTimeMillis(), record.getLatitude(), record.getLongitude(),
                record.getAltitude(), record.getSpeed(), record.getClimb());
    }
}
