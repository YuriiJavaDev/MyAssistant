package com.yurii.pavlenko.myassistant.tasks.receivers;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.yurii.pavlenko.myassistant.AlarmAlertActivity;
import com.yurii.pavlenko.myassistant.R;

/**
 * Broadcast receiver responsible for handling scheduled task deadline alarms,
 * triggering high-priority full-screen notifications, and rescheduling the next alert interval.
 */
public class TaskAlarmReceiver extends BroadcastReceiver {

    private static final String CHANNEL_ID = "task_alarm_channel";
    private static final String EXTRA_TASK_ID = "extra_task_id";
    private static final String EXTRA_TASK_TITLE = "extra_task_title";
    private static final long REPEAT_INTERVAL_MS = 5 * 60 * 1000; // 5 minutes interval

    @Override
    public void onReceive(Context context, Intent intent) {
        // Extract task details from the received broadcast intent
        long taskId = intent.getLongExtra(EXTRA_TASK_ID, -1);
        String taskTitle = intent.getStringExtra(EXTRA_TASK_TITLE);

        if (taskId == -1 || taskTitle == null) return;

        // Automatically reschedule the next alarm for 5 minutes later (repeating mechanism)
        rescheduleNextAlarm(context, taskId, taskTitle);

        // Ensure notification channel exists for Android 8.0+
        createNotificationChannel(context);

        // Create intent to open AlarmAlertActivity with full-screen capability and task details
        Intent fullScreenIntent = new Intent(context, AlarmAlertActivity.class);
        fullScreenIntent.putExtra(EXTRA_TASK_ID, taskId);
        fullScreenIntent.putExtra(EXTRA_TASK_TITLE, taskTitle);
        fullScreenIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        PendingIntent fullScreenPendingIntent = PendingIntent.getActivity(
                context,
                (int) taskId,
                fullScreenIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // Build high-priority notification with full-screen intent to wake up and alert the user
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("Deadline is near!!!")
                .setContentText("Task: " + taskTitle)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setFullScreenIntent(fullScreenPendingIntent, true)
                .setAutoCancel(true);

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.notify((int) taskId, builder.build());
        }
    }

    private void rescheduleNextAlarm(Context context, long taskId, String taskTitle) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) return;

        Intent intent = new Intent(context, TaskAlarmReceiver.class);
        intent.putExtra(EXTRA_TASK_ID, taskId);
        intent.putExtra(EXTRA_TASK_TITLE, taskTitle);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                (int) taskId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        long triggerTime = System.currentTimeMillis() + REPEAT_INTERVAL_MS;

        // Schedule exact alarm with wake-up flag to persist repeating behavior in idle/doze mode
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent);
        } else {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent);
        }
    }

    private void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Task Alarms";
            String description = "Channel for task deadline alarms and voice alerts";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }
}