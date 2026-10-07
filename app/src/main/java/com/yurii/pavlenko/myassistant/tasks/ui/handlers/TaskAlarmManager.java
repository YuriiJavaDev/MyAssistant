package com.yurii.pavlenko.myassistant.tasks.ui.handlers;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.yurii.pavlenko.myassistant.tasks.model.Task;
import com.yurii.pavlenko.myassistant.tasks.receivers.TaskAlarmReceiver;

import java.time.LocalDateTime;
import java.time.ZoneId;

/** Registers and cancels the wake-up alarms that trigger {@link TaskAlarmReceiver}. */
public final class TaskAlarmManager {

    private static final String TAG = "TaskAlarmManager";

    private TaskAlarmManager() {
    }

    public static void scheduleAlarm(Context context, Task task) {
        LocalDateTime reminder = task.getCustomReminderDateTime();
        if (reminder != null) {
            long triggerAtMillis = reminder.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
            schedule(context, (int) task.getId(), triggerAtMillis);
        }
    }

    public static void cancelAlarm(Context context, Task task) {
        cancel(context, (int) task.getId());
    }

    /**
     * Schedules a wake-up alarm identified by {@code requestCode}; an earlier alarm with the same
     * code is replaced. Falls back to an inexact alarm when exact alarms are not permitted.
     */
    public static void schedule(Context context, int requestCode, long triggerAtMillis) {
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                createIntent(context),
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        AlarmManager alarmManager = context.getSystemService(AlarmManager.class);

        try {
            if (ExactAlarmPermissionHelper.hasExactAlarmPermission(context)) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent);
            } else {
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent);
            }
        } catch (SecurityException e) {
            Log.w(TAG, "Alarm was not scheduled", e);
        }
    }

    public static void cancel(Context context, int requestCode) {
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                createIntent(context),
                PendingIntent.FLAG_NO_CREATE | PendingIntent.FLAG_IMMUTABLE
        );
        if (pendingIntent != null) {
            context.getSystemService(AlarmManager.class).cancel(pendingIntent);
            pendingIntent.cancel();
        }
    }

    private static Intent createIntent(Context context) {
        return new Intent(context, TaskAlarmReceiver.class);
    }
}