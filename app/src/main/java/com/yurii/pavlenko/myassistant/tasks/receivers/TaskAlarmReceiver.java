package com.yurii.pavlenko.myassistant.tasks.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.yurii.pavlenko.myassistant.tasks.database.AppDatabase;
import com.yurii.pavlenko.myassistant.tasks.notifications.OverdueAlerts;

import java.time.LocalDateTime;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** Entry point of task alarms: raises the overdue alert, or stops it when nothing is overdue. */
public class TaskAlarmReceiver extends BroadcastReceiver {

    private static final String TAG = "TaskAlarmReceiver";
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

    private static void handleAlarm(Context context) {
        int dueTasksCount = AppDatabase.getInstance(context).taskDao().getDueTasksCount(LocalDateTime.now());
        if (dueTasksCount == 0) {
            // The task was completed or deleted after this alarm was set: nothing to remind about
            OverdueAlerts.stop(context);
        } else {
            OverdueAlerts.raise(context, dueTasksCount);
        }
    }
}