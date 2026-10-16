package com.yurii.pavlenko.myassistant.tasks.database;

import android.content.Context;
import android.text.TextUtils;

import com.yurii.pavlenko.myassistant.flights.database.FlightDao;
import com.yurii.pavlenko.myassistant.flights.database.FlightDatabase;
import com.yurii.pavlenko.myassistant.flights.logic.FlightReportName;
import com.yurii.pavlenko.myassistant.flights.model.FlightReportSummary;
import com.yurii.pavlenko.myassistant.scan.backup.ScanBackupArchive;
import com.yurii.pavlenko.myassistant.scan.backup.ScanBackupRestorer;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** Uploads the local backup (task, step and flight databases, flight report PDFs and scan archive) to a WebDAV server and restores it from there. */
public class CloudSyncManager {

    private static final int TIMEOUT_MS = 10_000;
    private static final int HTTP_NOT_FOUND = 404;
    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor();

    public interface SyncCallback {
        void onSuccess(String message);

        void onError(String error);
    }

    /** Returns the message to show when the action succeeds. */
    private interface IoAction {
        String run() throws IOException;
    }

    private final Context context;
    private final CloudConfigManager configManager;

    public CloudSyncManager(Context context) {
        this.context = context.getApplicationContext();
        this.configManager = new CloudConfigManager(this.context);
    }

    public void uploadBackup(SyncCallback callback) {
        runAsync(() -> {
            uploadDatabaseBlocking(ManagedDatabase.TASKS);
            if (hasLocalDatabase(ManagedDatabase.STEPS)) {
                uploadDatabaseBlocking(ManagedDatabase.STEPS);
            }
            if (hasLocalDatabase(ManagedDatabase.FLIGHTS)) {
                uploadFlightsBlocking();
            }
            if (ScanBackupArchive.hasData(context)) {
                uploadScansBlocking();
            }
            return "Backup successfully uploaded to cloud";
        }, "Upload error: ", callback);
    }

    /**
     * Restores tasks, steps, flights and scans from the cloud. Each file is downloaded and verified completely
     * before the local data is replaced, so a failed download leaves local data untouched.
     */
    public void restoreBackup(SyncCallback callback) {
        runAsync(() -> {
            downloadDatabaseBlocking(ManagedDatabase.TASKS, false);
            List<String> missing = new ArrayList<>();
            if (!downloadDatabaseBlocking(ManagedDatabase.STEPS, true)) {
                missing.add("step");
            }
            if (!downloadDatabaseBlocking(ManagedDatabase.FLIGHTS, true)) {
                missing.add("flight");
            }
            if (!downloadScansBlocking()) {
                missing.add("scan");
            }
            if (missing.isEmpty()) {
                return "Backup successfully restored from cloud";
            }
            return "Tasks restored from cloud (no " + TextUtils.join(" or ", missing) + " backup found there)";
        }, "Download error: ", callback);
    }

    /** Blocking upload for background workers that already run off the main thread. */
    public void uploadDatabaseBlocking(ManagedDatabase database) throws IOException {
        File dbFile = database.getFile(context);
        if (!dbFile.exists()) {
            throw new IOException("Local database file not found");
        }
        upload(database.getBackupFileName(), dbFile);
    }

    public boolean hasLocalDatabase(ManagedDatabase database) {
        return database.getFile(context).exists();
    }

    /**
     * Uploads the flight database and then each flight report as a PDF file of its own, so the reports can be
     * opened straight from the cloud; remembers which state went up.
     */
    public void uploadFlightsBlocking() throws IOException {
        String signature = flightsSignature();
        uploadDatabaseBlocking(ManagedDatabase.FLIGHTS);
        uploadFlightReportsBlocking();
        configManager.saveUploadedFlightSignature(signature);
    }

    /** Sends only the reports that are new or were made again, which is told by the size of the PDF. */
    private void uploadFlightReportsBlocking() throws IOException {
        FlightDao dao = FlightDatabase.getInstance(context).flightDao();
        Set<String> uploaded = configManager.getUploadedFlightReports();
        Set<String> current = new HashSet<>();
        for (FlightReportSummary report : dao.reportSummaries()) {
            String key = report.getFlightId() + ":" + report.getSize();
            current.add(key);
            byte[] pdf = uploaded.contains(key) ? null : dao.reportPdf(report.getFlightId());
            if (pdf != null) {
                upload(FlightReportName.of(report.getStartMillis()), new ByteArrayInputStream(pdf), pdf.length);
                uploaded.add(key);
                configManager.saveUploadedFlightReports(uploaded);
            }
        }
        configManager.saveUploadedFlightReports(current);
    }

    /** True when the flights have changed since they were last uploaded. */
    public boolean flightsNeedUpload() {
        return hasLocalDatabase(ManagedDatabase.FLIGHTS)
                && !flightsSignature().equals(configManager.getUploadedFlightSignature());
    }

    private String flightsSignature() {
        File database = ManagedDatabase.FLIGHTS.getFile(context);
        return database.lastModified() + ":" + database.length();
    }

    /** The archive is built in the cache first, because the upload needs to know its size. */
    public void uploadScansBlocking() throws IOException {
        String signature = ScanBackupArchive.signature(context);
        File archive = new File(DatabaseBackupManager.cacheFolder(context), ScanBackupArchive.FILE_NAME);
        try {
            try (OutputStream out = new FileOutputStream(archive)) {
                ScanBackupArchive.write(context, out);
            }
            upload(ScanBackupArchive.FILE_NAME, archive);
        } finally {
            archive.delete();
        }
        configManager.saveUploadedScanSignature(signature);
    }

    /** True when the scans have changed since they were last uploaded. */
    public boolean scansNeedUpload() {
        return ScanBackupArchive.hasData(context)
                && !ScanBackupArchive.signature(context).equals(configManager.getUploadedScanSignature());
    }

    /**
     * Looks for the backup under its current name and then under the names it had before. Returns false when an
     * optional database is missing on the server, as in a backup made before it existed.
     */
    private boolean downloadDatabaseBlocking(ManagedDatabase database, boolean optional) throws IOException {
        for (String fileName : database.getRestoreFileNames()) {
            HttpURLConnection connection = openConnection(fileName, "GET");
            try {
                if (connection.getResponseCode() == HTTP_NOT_FOUND) {
                    continue;
                }
                requireSuccess(connection);
                DatabaseFileManager.Staged staged;
                try (InputStream in = connection.getInputStream()) {
                    staged = DatabaseFileManager.stage(context, in, database);
                }
                DatabaseFileManager.replaceWith(context, staged);
                return true;
            } finally {
                connection.disconnect();
            }
        }
        if (optional) {
            return false;
        }
        throw new IOException("Server responded with HTTP " + HTTP_NOT_FOUND);
    }

    /** Returns false when the server has no scan archive, which is normal for a backup made before scans existed. */
    private boolean downloadScansBlocking() throws IOException {
        HttpURLConnection connection = openConnection(ScanBackupArchive.FILE_NAME, "GET");
        try {
            if (connection.getResponseCode() == HTTP_NOT_FOUND) {
                return false;
            }
            requireSuccess(connection);
            try (InputStream in = connection.getInputStream()) {
                ScanBackupRestorer.restore(context, in);
            }
            return true;
        } finally {
            connection.disconnect();
        }
    }

    private void upload(String fileName, File file) throws IOException {
        try (InputStream in = new FileInputStream(file)) {
            upload(fileName, in, file.length());
        }
    }

    private void upload(String fileName, InputStream in, long length) throws IOException {
        HttpURLConnection connection = openConnection(fileName, "PUT");
        try {
            connection.setDoOutput(true);
            connection.setFixedLengthStreamingMode(length);
            try (OutputStream out = connection.getOutputStream()) {
                DatabaseFileManager.copy(in, out);
            }
            requireSuccess(connection);
        } finally {
            connection.disconnect();
        }
    }

    private HttpURLConnection openConnection(String fileName, String method) throws IOException {
        String baseUrl = configManager.getUrl();
        if (baseUrl.isEmpty()) {
            throw new IOException("WebDAV URL is not configured");
        }
        HttpURLConnection connection = (HttpURLConnection) new URL(WebDavUtils.buildTargetUrl(baseUrl, fileName)).openConnection();
        connection.setRequestMethod(method);
        connection.setConnectTimeout(TIMEOUT_MS);
        connection.setReadTimeout(TIMEOUT_MS);
        WebDavUtils.applyBasicAuth(connection, configManager.getUsername(), configManager.getPassword());
        return connection;
    }

    private static void requireSuccess(HttpURLConnection connection) throws IOException {
        int code = connection.getResponseCode();
        if (code < 200 || code >= 300) {
            throw new IOException("Server responded with HTTP " + code);
        }
    }

    private static void runAsync(IoAction action, String errorPrefix, SyncCallback callback) {
        EXECUTOR.execute(() -> {
            String message;
            try {
                message = action.run();
            } catch (Exception e) {
                callback.onError(errorPrefix + (e.getMessage() != null ? e.getMessage() : "Unknown error"));
                return;
            }
            callback.onSuccess(message);
        });
    }
}
