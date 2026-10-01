package com.yurii.pavlenko.myassistant.tasks.receivers;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.yurii.pavlenko.myassistant.AlarmAlertActivity;
import com.yurii.pavlenko.myassistant.R;
import com.yurii.pavlenko.myassistant.tasks.database.AppDatabase;
import com.yurii.pavlenko.myassistant.tasks.model.Task;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

/**
 * BroadcastReceiver for handling task deadline alarms, counting all overdue tasks asynchronously,
 * triggering full-screen activity, and maintaining the 5-minute repeating loop until dismissed.
 *
 * @date 2026-10-01
 */
public class TaskAlarmReceiver extends BroadcastReceiver {

    private static final String TAG = "TaskAlarmReceiver";
    public static final String CHANNEL_ID = "task_deadline_channel";
    public static final String ACTION_CLOSE_ALERT = "com.yurii.pavlenko.myassistant.ACTION_CLOSE_ALERT";
    private static final int NOTIFICATION_ID = 9999;
    private static final int REPEAT_REQUEST_CODE = 8888;
    private static final long REPEAT_INTERVAL_MS = 5 * 60 * 1000; // 5 minutes

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "Alarm received. Processing in background...");
        final PendingResult pendingResult = goAsync();

        // Execute database query and background processing on a single-thread executor
        // to avoid blocking the main thread and Room database errors.
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                // 1. Create notification channel
                createNotificationChannel(context);

                // 2. Fetch all overdue tasks from the database
                List<Task> dueTasks = fetchDueTasks(context);
                int dueTasksCount = (dueTasks != null) ? dueTasks.size() : 0;

                Log.d(TAG, "Fetched due tasks count from DB: " + dueTasksCount);

                // 3. Automatically schedule the next 5-minute repeating alarm
                scheduleRepeatingAlarm(context);

                // 3.1. Force close any lingering AlarmAlertActivity window before triggering a new one
                Intent closeIntent = new Intent(ACTION_CLOSE_ALERT);
                context.sendBroadcast(closeIntent);

                // 4. Create intent for AlarmAlertActivity with updated task count extra
                Intent alertIntent = new Intent(context, AlarmAlertActivity.class);
                alertIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                alertIntent.putExtra("extra_due_tasks_count", dueTasksCount);

                // Use NOTIFICATION_ID instead of 0 to ensure PendingIntent correctly updates extras
                PendingIntent fullScreenPendingIntent = PendingIntent.getActivity(
                        context,
                        NOTIFICATION_ID,
                        alertIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
                );

                // 5. Build high-priority notification with full-screen intent
                NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_launcher_foreground)
                        .setContentTitle("Deadline Alert!")
                        .setContentText("You have " + dueTasksCount + " overdue task deadlines.")
                        .setPriority(NotificationCompat.PRIORITY_MAX)
                        .setCategory(NotificationCompat.CATEGORY_ALARM)
                        .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                        .setFullScreenIntent(fullScreenPendingIntent, true)
                        .setContentIntent(fullScreenPendingIntent)
                        .setAutoCancel(true)
                        .setDefaults(NotificationCompat.DEFAULT_ALL);

                NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);

                // Примусово скидаємо старе сповіщення, щоб Android не ігнорував fullScreenIntent та звук на повторі
                notificationManager.cancel(NOTIFICATION_ID);
                notificationManager.notify(NOTIFICATION_ID, builder.build());

            } catch (Exception e) {
                Log.e(TAG, "Error processing task alarm in background", e);
            } finally {
                // Always finish the broadcast receiver's async work
                pendingResult.finish();
            }
        });
    }

    /**
     * Schedules the next repeating alarm exactly 5 minutes later.
     */
    private void scheduleRepeatingAlarm(Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) return;

        Intent intent = new Intent(context, TaskAlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                REPEAT_REQUEST_CODE,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        long triggerAtMillis = System.currentTimeMillis() + REPEAT_INTERVAL_MS;

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
                alarmManager.set(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent);
            } else {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent);
            }
            Log.d(TAG, "Next 5-minute repeating alarm scheduled successfully.");
        } catch (SecurityException e) {
            Log.e(TAG, "Failed to schedule repeating alarm", e);
        }
    }

    /**
     * Cancels the repeating alarm when the user dismisses it from the UI.
     */
    public static void cancelRepeatingAlarm(Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) return;

        Intent intent = new Intent(context, TaskAlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                REPEAT_REQUEST_CODE,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        alarmManager.cancel(pendingIntent);
        Log.d(TAG, "Repeating deadline alarm canceled by user.");
    }

    private void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Task Deadline Alerts";
            String description = "Channel for urgent task deadline full-screen alerts";
            int importance = NotificationManager.IMPORTANCE_HIGH;

            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            channel.enableVibration(true);

            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    private List<Task> fetchDueTasks(Context context) {
        try {
            return AppDatabase.getInstance(context).taskDao().getDueTasks(LocalDateTime.now());
        } catch (Exception e) {
            Log.e(TAG, "Failed to fetch due tasks", e);
        }
        return new ArrayList<>();
    }

    public static void scheduleAlarm(Context context, Task task) {
        if (task.getCustomReminderDateTime() == null) return;

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) return;

        Intent intent = new Intent(context, TaskAlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                (int) task.getId(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        long triggerAtMillis = task.getCustomReminderDateTime()
                .atZone(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli();

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
                alarmManager.set(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent);
            } else {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent);
            }
        } catch (SecurityException e) {
            Log.e(TAG, "Failed to schedule exact alarm", e);
        }
    }

    public static void cancelAlarm(Context context, Task task) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) return;

        Intent intent = new Intent(context, TaskAlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                (int) task.getId(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        alarmManager.cancel(pendingIntent);
    }
}