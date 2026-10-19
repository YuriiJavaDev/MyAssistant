package com.yurii.pavlenko.myassistant.flights.data;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.lifecycle.LiveData;

import com.yurii.pavlenko.myassistant.flights.database.FlightDao;
import com.yurii.pavlenko.myassistant.flights.database.FlightDatabase;
import com.yurii.pavlenko.myassistant.flights.logic.FlightPoint;
import com.yurii.pavlenko.myassistant.flights.logic.FlightStats;
import com.yurii.pavlenko.myassistant.flights.logic.ReportQuota;
import com.yurii.pavlenko.myassistant.flights.model.FlightPointRecord;
import com.yurii.pavlenko.myassistant.flights.model.FlightRecord;
import com.yurii.pavlenko.myassistant.flights.model.FlightReportRecord;
import com.yurii.pavlenko.myassistant.flights.report.BuiltReport;
import com.yurii.pavlenko.myassistant.flights.report.FlightReportBuilder;

import java.io.IOException;
import java.io.OutputStream;
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

    /**
     * Hands over the PDF of a flight, building it first if it is missing or has only the offline map and a
     * connection now gives a better one; null when it cannot be had.
     */
    public void loadReport(long flightId, Consumer<byte[]> onDone) {
        io.execute(() -> {
            byte[] result = reportOf(flightId);
            main.post(() -> onDone.accept(result));
        });
    }

    public void deleteFlight(long flightId) {
        io.execute(() -> dao().deleteFlight(flightId));
    }

    /** Tells how full the journal is, measured in reports against the limit the user has set. */
    public void checkQuota(Consumer<ReportQuota> onDone) {
        io.execute(() -> {
            ReportQuota quota = new ReportQuota(new ReportLimitStore(context).load(), dao().reportCount());
            main.post(() -> onDone.accept(quota));
        });
    }

    /** Writes the report to the place the user chose and deletes the flight only when that succeeded. */
    public void saveReportThenDelete(long flightId, Uri target, Consumer<Boolean> onDone) {
        io.execute(() -> {
            boolean saved = writeReport(flightId, target);
            if (saved) {
                dao().deleteFlight(flightId);
            }
            main.post(() -> onDone.accept(saved));
        });
    }

    private boolean writeReport(long flightId, Uri target) {
        byte[] pdf = reportOf(flightId);
        if (pdf == null) {
            return false;
        }
        try (OutputStream out = context.getContentResolver().openOutputStream(target)) {
            if (out == null) {
                return false;
            }
            out.write(pdf);
            return true;
        } catch (IOException e) {
            Log.e(TAG, "Cannot save the flight report", e);
            return false;
        }
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
        reportOf(id);
    }

    private byte[] reportOf(long flightId) {
        Boolean storedOnline = dao().reportOnlineMap(flightId);
        boolean stored = storedOnline != null;
        if (stored && storedOnline) {
            return storedPdf(flightId);
        }
        FlightRecord flight = dao().flight(flightId);
        BuiltReport built = flight != null && flight.isFinished() ? buildReport(flight) : null;
        if (built == null || stored && !built.hasOnlineMap()) {
            return stored ? storedPdf(flightId) : null;
        }
        dao().saveReport(new FlightReportRecord(flightId, built.getPdf(), built.hasOnlineMap()));
        return built.getPdf();
    }

    private byte[] storedPdf(long flightId) {
        try {
            return new FlightReportStore(dao()).pdf(flightId);
        } catch (IOException e) {
            Log.e(TAG, "Cannot read the stored flight report", e);
            return null;
        }
    }

    /** Builds the report of a flight and keeps it; null when the flight has no points or the PDF fails. */
    private BuiltReport buildReport(FlightRecord flight) {
        List<FlightPoint> points = pointsOf(flight.getId());
        if (points.isEmpty()) {
            return null;
        }
        try {
            return new FlightReportBuilder(context).build(flight, points, new FlightUnitsStore(context).load());
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
