package com.yurii.pavlenko.myassistant;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;
import com.yurii.pavlenko.myassistant.tasks.receivers.TaskAlarmReceiver;
import com.yurii.pavlenko.myassistant.tasks.ui.handlers.TaskPreferences;

import java.util.Locale;

/**
 * Full-screen activity for displaying overdue tasks count over lock screen with TTS and repeat cancellation.
 *
 * @date 2026-10-01
 */
public class AlarmAlertActivity extends AppCompatActivity {

    public static final String EXTRA_DUE_TASKS_COUNT = "extra_due_tasks_count";

    private TextToSpeech textToSpeech;
    private int dueTasksCount;
    private TextView tvTaskTitleContent;

    // BroadcastReceiver to force close the existing activity window before a new repeating alarm opens a fresh one
    private final BroadcastReceiver closeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            finish();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Register the close receiver to handle repeating alert refreshes cleanly
        ContextCompat.registerReceiver(
                this,
                closeReceiver,
                new IntentFilter(TaskAlarmReceiver.ACTION_CLOSE_ALERT),
                ContextCompat.RECEIVER_NOT_EXPORTED
        );

        // Allow the activity to show over lock screen and turn on the screen
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true);
            setTurnScreenOn(true);
        } else {
            getWindow().addFlags(
                    WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                            WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON |
                            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
            );
        }

        setContentView(R.layout.dialog_deadline_alert);

        tvTaskTitleContent = findViewById(R.id.tvTaskTitleContent);
        MaterialButton btnDismissAlert = findViewById(R.id.btnDismissAlert);

        // Initialize Text-to-Speech engine
        textToSpeech = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                int result = textToSpeech.setLanguage(Locale.US);
                if (result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED) {
                    speakAlarm();
                }
            }
        });

        // Cancel repeating alarm and finish activity when user clicks dismiss
        btnDismissAlert.setOnClickListener(v -> {
            TaskAlarmReceiver.cancelRepeatingAlarm(this);
            stopAlarmAndSpeech();
            finish();
        });

        processIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        processIntent(intent);
        speakAlarm();
    }

    private void processIntent(Intent intent) {
        if (intent != null) {
            dueTasksCount = intent.getIntExtra(EXTRA_DUE_TASKS_COUNT, 0);
        }
        updateUiWithCount(dueTasksCount);
    }

    private void updateUiWithCount(int count) {
        if (tvTaskTitleContent != null) {
            tvTaskTitleContent.setText("Overdue tasks: " + count);
        }
    }

    private void speakAlarm() {
        if (textToSpeech != null && TaskPreferences.isVoiceEnabled(this)) {
            String speechText = "Attention! You have " + dueTasksCount + " task deadlines requiring immediate attention.";
            textToSpeech.speak(speechText, TextToSpeech.QUEUE_FLUSH, null, null);
        }
    }

    private void stopAlarmAndSpeech() {
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
    }

    @Override
    protected void onDestroy() {
        try {
            unregisterReceiver(closeReceiver);
        } catch (IllegalArgumentException e) {
            // Receiver might already be unregistered
        }
        stopAlarmAndSpeech();
        super.onDestroy();
    }
}