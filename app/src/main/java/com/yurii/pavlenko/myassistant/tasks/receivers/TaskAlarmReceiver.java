package com.yurii.pavlenko.myassistant.tasks.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.yurii.pavlenko.myassistant.tasks.database.AppDatabase;
import com.yurii.pavlenko.myassistant.tasks.notifications.DeadlineNotifier;
import com.yurii.pavlenko.myassistant.tasks.ui.handlers.TaskAlarmManager;

import java.time.LocalDateTime;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Handles task alarms: shows an alert for all overdue reminders and repeats it
 * every few minutes until the user dismisses it or nothing is overdue anymore.
 */
public class TaskAlarmReceiver extends BroadcastReceiver {

    public static final String ACTION_CLOSE_ALERT = "com.yurii.pavlenko.myassistant.ACTION_CLOSE_ALERT";

    private static final String TAG = "TaskAlarmReceiver";
    // Task IDs are positive, so this request code never collides with a task alarm
    private static final int REPEAT_REQUEST_CODE = -1;
    private static final long REPEAT_INTERVAL_MS = TimeUnit.MINUTES.toMillis(5);
    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor();

    @Override
    public void onReceive(Context context, Intent intent) {
        PendingResult pendingResult = goAsync();
        Context appContext = context.getApplicationContext();
        EXECUTOR.execute(() -> {
            try {
                handleAlarm(appContext);
            } catch (Exception e) {
                Log.e(TAG, "Error processing task alarm", e);
            } finally {
                pendingResult.finish();
            }
        });
    }

    public static void cancelRepeatingAlarm(Context context) {
        TaskAlarmManager.cancel(context, REPEAT_REQUEST_CODE);
    }

    private static void handleAlarm(Context context) {
        int dueTasksCount = AppDatabase.getInstance(context).taskDao().getDueTasksCount(LocalDateTime.now());

        if (dueTasksCount == 0) {
            // The task was completed or deleted after this alarm was set: nothing to remind about
            cancelRepeatingAlarm(context);
            DeadlineNotifier.dismiss(context);
            closeAlertWindow(context);
            return;
        }

        TaskAlarmManager.schedule(context, REPEAT_REQUEST_CODE, System.currentTimeMillis() + REPEAT_INTERVAL_MS);
        closeAlertWindow(context);
        DeadlineNotifier.show(context, dueTasksCount);
    }

    private static void closeAlertWindow(Context context) {
        context.sendBroadcast(new Intent(ACTION_CLOSE_ALERT).setPackage(context.getPackageName()));
    }
}