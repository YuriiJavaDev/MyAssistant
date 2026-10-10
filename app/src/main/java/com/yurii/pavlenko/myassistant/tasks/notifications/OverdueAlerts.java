package com.yurii.pavlenko.myassistant.tasks.notifications;

import android.content.Context;
import android.content.Intent;

import com.yurii.pavlenko.myassistant.tasks.ui.handlers.TaskAlarmManager;
import com.yurii.pavlenko.myassistant.tasks.ui.handlers.TaskPreferences;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Lifecycle of the overdue-reminder alert: the notification, the full-screen window,
 * the voice and the repetition every few minutes.
 */
public final class OverdueAlerts {

    public static final String ACTION_CLOSE_ALERT = "com.yurii.pavlenko.myassistant.ACTION_CLOSE_ALERT";

    // Task IDs are positive, so this request code never collides with a task alarm
    private static final int REPEAT_REQUEST_CODE = -1;
    private static final long REPEAT_INTERVAL_MS = TimeUnit.MINUTES.toMillis(5);
    private static final long VOICE_TIMEOUT_SEC = 8;

    private OverdueAlerts() {
    }

    /**
     * Alerts about overdue reminders and arranges the next repetition. When the alert window is not
     * shown, the voice is spoken here and this call blocks until it ends: use a background thread.
     */
    public static void raise(Context context, int dueTasksCount) {
        TaskAlarmManager.schedule(context, REPEAT_REQUEST_CODE, System.currentTimeMillis() + REPEAT_INTERVAL_MS);
        closeAlertWindow(context);

        boolean windowRequested = DeadlineNotifier.show(context, dueTasksCount);
        // The alert window speaks by itself; without it the voice has to come from here
        if (!windowRequested && TaskPreferences.isVoiceEnabled(context)) {
            speakAndWait(context, dueTasksCount);
        }
    }

    /** Stops the repetition and removes the notification and the alert window. */
    public static void stop(Context context) {
        TaskAlarmManager.cancel(context, REPEAT_REQUEST_CODE);
        DeadlineNotifier.dismiss(context);
        closeAlertWindow(context);
    }

    private static void speakAndWait(Context context, int dueTasksCount) {
        CountDownLatch finished = new CountDownLatch(1);
        DeadlineSpeaker speaker = new DeadlineSpeaker(context, finished::countDown);
        speaker.speak(dueTasksCount);
        try {
            finished.await(VOICE_TIMEOUT_SEC, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            speaker.shutdown();
        }
    }

    private static void closeAlertWindow(Context context) {
        context.sendBroadcast(new Intent(ACTION_CLOSE_ALERT).setPackage(context.getPackageName()));
    }
}