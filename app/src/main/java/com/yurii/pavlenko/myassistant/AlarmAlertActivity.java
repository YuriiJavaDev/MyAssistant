package com.yurii.pavlenko.myassistant;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;
import com.yurii.pavlenko.myassistant.tasks.notifications.DeadlineSpeaker;
import com.yurii.pavlenko.myassistant.tasks.notifications.OverdueAlerts;
import com.yurii.pavlenko.myassistant.tasks.ui.handlers.TaskPreferences;

/**
 * Full-screen alert over the lock screen: shows how many reminders are overdue, speaks it,
 * and leads to the list of those tasks.
 */
public class AlarmAlertActivity extends AppCompatActivity {

    public static final String EXTRA_DUE_TASKS_COUNT = "extra_due_tasks_count";

    private DeadlineSpeaker speaker;
    private TextView tvTaskTitleContent;

    // Closes this window before a repeated alarm opens a fresh one
    private final BroadcastReceiver closeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            finish();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ContextCompat.registerReceiver(
                this,
                closeReceiver,
                new IntentFilter(OverdueAlerts.ACTION_CLOSE_ALERT),
                ContextCompat.RECEIVER_NOT_EXPORTED
        );

        showOverLockScreen();
        setContentView(R.layout.dialog_deadline_alert);

        tvTaskTitleContent = findViewById(R.id.tvTaskTitleContent);
        MaterialButton btnViewTasks = findViewById(R.id.btnDismissAlert);
        btnViewTasks.setOnClickListener(v -> openTaskList());

        speaker = new DeadlineSpeaker(this, null);
        showAlert(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        showAlert(intent);
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(closeReceiver);
        speaker.shutdown();
        super.onDestroy();
    }

    private void showAlert(Intent intent) {
        int dueTasksCount = intent.getIntExtra(EXTRA_DUE_TASKS_COUNT, 0);
        tvTaskTitleContent.setText("Overdue tasks: " + dueTasksCount);
        if (TaskPreferences.isVoiceEnabled(this)) {
            speaker.speak(dueTasksCount);
        }
    }

    private void openTaskList() {
        OverdueAlerts.stop(this);
        speaker.shutdown();
        startActivity(MainActivity.createShowDueRemindersIntent(this));
        finish();
    }

    private void showOverLockScreen() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true);
            setTurnScreenOn(true);
        } else {
            getWindow().addFlags(
                    WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                            | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                            | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
            );
        }
    }
}