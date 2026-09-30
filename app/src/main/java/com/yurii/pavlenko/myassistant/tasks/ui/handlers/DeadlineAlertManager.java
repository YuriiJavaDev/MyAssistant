package com.yurii.pavlenko.myassistant.tasks.ui.handlers;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.speech.tts.TextToSpeech;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.google.android.material.button.MaterialButton;
import com.yurii.pavlenko.myassistant.R;
import com.yurii.pavlenko.myassistant.tasks.model.Task;

import java.util.Locale;

public class DeadlineAlertManager {

    private static TextToSpeech tts;
    private static Handler repeatHandler;
    private static Runnable repeatRunnable;
    private static AlertDialog activeDialog;

    // Overloaded method for calls with 2 arguments (e.g. from TaskDialogFragment)
    public static void showDeadlineAlert(Context context, Task task) {
        showDeadlineAlert(context, task, null);
    }

    // Main method with dismiss callback support
    public static void showDeadlineAlert(Context context, Task task, Runnable onDismissCallback) {
        // Ensure previous alert and timer are cleared before launching a new one
        stopAlertAndTts();

        Context appContext = context.getApplicationContext();
        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_deadline_alert, null);

        TextView taskTitleTextView = dialogView.findViewById(R.id.tvTaskTitleContent);
        MaterialButton btnDismiss = dialogView.findViewById(R.id.btnDismissAlert);

        String taskTitle = (task != null && task.getTitle() != null) ? task.getTitle() : "Untitled Task";
        taskTitleTextView.setText(taskTitle);

        activeDialog = new AlertDialog.Builder(context)
                .setView(dialogView)
                .setCancelable(false)
                .create();

        // Initialize TTS with Application Context to prevent memory leaks
        initAndPlayTts(appContext, taskTitle);

        // Configure repetition every 5 minutes (300,000 ms)
        repeatHandler = new Handler(Looper.getMainLooper());
        repeatRunnable = new Runnable() {
            @Override
            public void run() {
                playTtsMessage(taskTitle);
                if (repeatHandler != null) {
                    repeatHandler.postDelayed(this, 300000);
                }
            }
        };
        repeatHandler.postDelayed(repeatRunnable, 300000);

        btnDismiss.setOnClickListener(v -> {
            stopAlertAndTts();
            if (onDismissCallback != null) {
                onDismissCallback.run();
            }
        });

        if (activeDialog.getWindow() != null) {
            activeDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        activeDialog.show();
    }

    private static void initAndPlayTts(Context appContext, String taskTitle) {
        tts = new TextToSpeech(appContext, status -> {
            if (status == TextToSpeech.SUCCESS && tts != null) {
                tts.setLanguage(Locale.US);
                playTtsMessage(taskTitle);
            }
        });
    }

    private static void playTtsMessage(String taskTitle) {
        if (tts != null) {
            String message = "Deadline is near! Check task: " + taskTitle;
            tts.speak(message, TextToSpeech.QUEUE_FLUSH, null, "DeadlineAlert");
        }
    }

    public static void stopAlertAndTts() {
        // 1. Stop and remove the background repetition timer
        if (repeatHandler != null && repeatRunnable != null) {
            repeatHandler.removeCallbacks(repeatRunnable);
            repeatHandler = null;
            repeatRunnable = null;
        }

        // 2. Dismiss the active dialog if it is currently showing
        if (activeDialog != null && activeDialog.isShowing()) {
            activeDialog.dismiss();
            activeDialog = null;
        }

        // 3. Properly stop and release TextToSpeech resources
        if (tts != null) {
            try {
                tts.stop();
                tts.shutdown();
            } catch (Exception e) {
                e.printStackTrace();
            }
            tts = null;
        }
    }
}