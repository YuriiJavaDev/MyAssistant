package com.yurii.pavlenko.myassistant.tasks.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.yurii.pavlenko.myassistant.tasks.ui.handlers.TaskAlarmRescheduler;

/** Restores task reminders after the device is rebooted or the app is updated. */
public class BootCompletedReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (Intent.ACTION_BOOT_COMPLETED.equals(action) || Intent.ACTION_MY_PACKAGE_REPLACED.equals(action)) {
            PendingResult pendingResult = goAsync();
            TaskAlarmRescheduler.rescheduleAsync(context, true, pendingResult::finish);
        }
    }
}