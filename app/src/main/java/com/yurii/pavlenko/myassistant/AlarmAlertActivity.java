package com.yurii.pavlenko.myassistant;

import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;

import com.yurii.pavlenko.myassistant.tasks.model.Task;
import com.yurii.pavlenko.myassistant.tasks.ui.handlers.DeadlineAlertManager;

/**
 * Lightweight activity launched via full-screen notification intent to wake up the device,
 * bypass the lock screen, and display the task deadline alert dialog with TTS voice.
 */
public class AlarmAlertActivity extends AppCompatActivity {

    private static final String EXTRA_TASK_ID = "extra_task_id";
    private static final String EXTRA_TASK_TITLE = "extra_task_title";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Wake up the screen and show over lock screen on modern Android & Samsung One UI
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true);
            setTurnScreenOn(true);
            KeyguardManager keyguardManager = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
            if (keyguardManager != null) {
                keyguardManager.requestDismissKeyguard(this, null);
            }
        } else {
            getWindow().addFlags(
                    WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                            WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON |
                            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
            );
        }

        // Extract task details passed from the broadcast receiver intent
        long taskId = getIntent().getLongExtra(EXTRA_TASK_ID, -1);
        String taskTitle = getIntent().getStringExtra(EXTRA_TASK_TITLE);

        if (taskId != -1 && taskTitle != null) {
            Task task = new Task(taskId, taskTitle);
            task.setId(taskId);
            task.setTitle(taskTitle);

            // Safely trigger the popup alert and TTS voice using a valid Activity context
            DeadlineAlertManager.showDeadlineAlert(this, task);
        } else {
            finish();
        }
    }
}