package com.yurii.pavlenko.myassistant.flights.service;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationChannelCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.app.ServiceCompat;

import com.yurii.pavlenko.myassistant.MainActivity;
import com.yurii.pavlenko.myassistant.R;
import com.yurii.pavlenko.myassistant.flights.data.FlightRecorder;

/**
 * Keeps the flight recording alive while the app is in the background or the screen is off.
 * Android requires such a service to show a notification for as long as it uses the location.
 */
public final class FlightRecordingService extends Service {

    private static final String CHANNEL_ID = "flight_recording";
    private static final int NOTIFICATION_ID = 3001;
    private static final int OPEN_REQUEST_CODE = 3002;

    @Override
    public void onCreate() {
        super.onCreate();
        createChannel();
        ServiceCompat.startForeground(this, NOTIFICATION_ID, buildNotification(),
                ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (!FlightRecorder.get(this).startRecording()) {
            stopSelf();
        }
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        FlightRecorder.get(this).stopRecording();
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void createChannel() {
        NotificationChannelCompat channel = new NotificationChannelCompat.Builder(
                CHANNEL_ID, NotificationManagerCompat.IMPORTANCE_LOW)
                .setName("Flight recording")
                .setDescription("Shown while a flight is being recorded")
                .build();
        NotificationManagerCompat.from(this).createNotificationChannel(channel);
    }

    private Notification buildNotification() {
        PendingIntent open = PendingIntent.getActivity(this, OPEN_REQUEST_CODE,
                new Intent(this, MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("Recording the flight")
                .setContentText("Tap to open the instruments")
                .setCategory(NotificationCompat.CATEGORY_SERVICE)
                .setOngoing(true)
                .setContentIntent(open)
                .build();
    }
}
