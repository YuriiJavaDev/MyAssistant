package com.yurii.pavlenko.myassistant.tasks.ui.handlers;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.yurii.pavlenko.myassistant.tasks.model.Task;
import com.yurii.pavlenko.myassistant.tasks.receivers.TaskAlarmReceiver;

import java.time.ZoneId;

public class TaskAlarmManager {

    private static final String EXTRA_TASK_ID = "extra_task_id";
    private static final String EXTRA_TASK_TITLE = "extra_task_title";

    // Schedule an exact alarm for the task reminder
    public static void scheduleAlarm(Context context, Task task) {
        if (task.getCustomReminderDateTime() == null) return;

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) return;

        // Check if exact alarms are permitted on Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                // If permission is missing, system will handle or we can skip exact scheduling
                return;
            }
        }

        Intent intent = new Intent(context, TaskAlarmReceiver.class);
        intent.putExtra(EXTRA_TASK_ID, task.getId());
        intent.putExtra(EXTRA_TASK_TITLE, task.getTitle());

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                (int) task.getId(), // Unique request code per task ID
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // Convert LocalDateTime to epoch milliseconds
        long triggerTimeMillis = task.getCustomReminderDateTime()
                .atZone(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli();

        // Set exact and wake up alarm
        alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerTimeMillis,
                pendingIntent
        );
    }

    // Cancel the alarm when task is completed, deleted, or reminder is cleared
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