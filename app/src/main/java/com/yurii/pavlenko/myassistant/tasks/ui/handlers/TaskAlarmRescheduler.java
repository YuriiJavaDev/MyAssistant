package com.yurii.pavlenko.myassistant.tasks.ui.handlers;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.yurii.pavlenko.myassistant.tasks.database.AppDatabase;
import com.yurii.pavlenko.myassistant.tasks.model.Task;
import com.yurii.pavlenko.myassistant.tasks.receivers.TaskAlarmReceiver;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Restores task alarms from the database. AlarmManager forgets every alarm after a device reboot
 * or an app update, while the tasks and their reminder times stay in Room.
 */
public final class TaskAlarmRescheduler {

    private static final String TAG = "TaskAlarmRescheduler";
    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor();

    private TaskAlarmRescheduler() {
    }

    /**
     * Re-registers alarms of all not completed tasks whose reminder is still in the future.
     * When {@code showMissed} is set and some reminder time passed while the alarms were gone,
     * the overdue alert is shown once. {@code onDone} is always called at the end and may be null.
     */
    public static void rescheduleAsync(Context context, boolean showMissed, Runnable onDone) {
        Context appContext = context.getApplicationContext();
        EXECUTOR.execute(() -> {
            try {
                boolean hasMissed = rescheduleFutureReminders(appContext);
                if (showMissed && hasMissed) {
                    appContext.sendBroadcast(new Intent(appContext, TaskAlarmReceiver.class));
                }
            } catch (Exception e) {
                Log.e(TAG, "Failed to restore task alarms", e);
            } finally {
                if (onDone != null) {
                    onDone.run();
                }
            }
        });
    }

    private static boolean rescheduleFutureReminders(Context context) {
        List<Task> tasks = AppDatabase.getInstance(context).taskDao().getTasksWithActiveReminder();
        LocalDateTime now = LocalDateTime.now();
        boolean hasMissed = false;

        for (Task task : tasks) {
            if (task.getCustomReminderDateTime().isAfter(now)) {
                TaskAlarmManager.scheduleAlarm(context, task);
            } else {
                hasMissed = true;
            }
        }
        return hasMissed;
    }
}