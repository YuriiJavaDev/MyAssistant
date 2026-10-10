package com.yurii.pavlenko.myassistant.tasks.notifications;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationChannelCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.yurii.pavlenko.myassistant.AlarmAlertActivity;
import com.yurii.pavlenko.myassistant.MainActivity;
import com.yurii.pavlenko.myassistant.R;
import com.yurii.pavlenko.myassistant.tasks.ui.handlers.TaskPreferences;

/**
 * Shows the overdue-deadline notification. When the full-screen alert is disabled by the user or not
 * allowed by the system, it degrades to a regular high-priority notification with sound.
 */
public final class DeadlineNotifier {

    private static final String CHANNEL_ID = "task_deadline_channel";
    private static final int NOTIFICATION_ID = 9999;
    private static final int ALERT_REQUEST_CODE = 1;
    private static final int TASK_LIST_REQUEST_CODE = 2;
    private static final int PENDING_INTENT_FLAGS = PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE;

    private DeadlineNotifier() {
    }

    /**
     * Shows or refreshes the notification.
     *
     * @return true if the full-screen alert window was requested, false for a plain notification
     */
    public static boolean show(Context context, int dueTasksCount) {
        createChannel(context);

        Intent alertIntent = new Intent(context, AlarmAlertActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP)
                .putExtra(AlarmAlertActivity.EXTRA_DUE_TASKS_COUNT, dueTasksCount);
        PendingIntent alertPendingIntent = PendingIntent.getActivity(context, ALERT_REQUEST_CODE, alertIntent, PENDING_INTENT_FLAGS);
        PendingIntent taskListPendingIntent = PendingIntent.getActivity(
                context, TASK_LIST_REQUEST_CODE, MainActivity.createShowDueRemindersIntent(context), PENDING_INTENT_FLAGS);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("Deadline Alert!")
                .setContentText("You have " + dueTasksCount + " overdue task deadlines.")
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setContentIntent(taskListPendingIntent)
                .setAutoCancel(true)
                .setDefaults(NotificationCompat.DEFAULT_ALL);

        boolean fullScreen = canUseFullScreenIntent(context);
        if (fullScreen) {
            builder.setFullScreenIntent(alertPendingIntent, true);
        }

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        // Cancel first, otherwise the system ignores the sound and full-screen intent of a repeated notification
        notificationManager.cancel(NOTIFICATION_ID);
        notificationManager.notify(NOTIFICATION_ID, builder.build());
        return fullScreen;
    }

    public static void dismiss(Context context) {
        NotificationManagerCompat.from(context).cancel(NOTIFICATION_ID);
    }

    private static void createChannel(Context context) {
        NotificationChannelCompat channel = new NotificationChannelCompat.Builder(
                CHANNEL_ID, NotificationManagerCompat.IMPORTANCE_HIGH)
                .setName("Task Deadline Alerts")
                .setDescription("Urgent task deadline alerts")
                .setVibrationEnabled(true)
                .build();
        NotificationManagerCompat.from(context).createNotificationChannel(channel);
    }

    private static boolean canUseFullScreenIntent(Context context) {
        if (!TaskPreferences.isFullScreenEnabled(context)) {
            return false;
        }
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.UPSIDE_DOWN_CAKE
                || context.getSystemService(NotificationManager.class).canUseFullScreenIntent();
    }
}