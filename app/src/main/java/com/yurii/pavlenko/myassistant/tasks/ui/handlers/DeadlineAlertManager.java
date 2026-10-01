package com.yurii.pavlenko.myassistant.tasks.ui.handlers;

import android.app.AlertDialog;
import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.google.android.material.button.MaterialButton;
import com.yurii.pavlenko.myassistant.R;

import java.util.ArrayList;
import java.util.Locale;

/**
 * Manages the consolidated task deadline alert dialog with custom layout
 * and Text-to-Speech (TTS) voice announcements for multiple due tasks.
 */
public class DeadlineAlertManager {

    private static final String TAG = "DeadlineAlertManager";

    private static TextToSpeech tts = null;
    private static AlertDialog currentDialog = null;

    /**
     * Shows the consolidated deadline alert dialog with custom layout and voice announcement.
     */
    public static synchronized void showDeadlineAlert(Context context, ArrayList<String> taskTitles, Runnable onDismissCallback) {
        if (context == null) return;

        stopAlertAndTts();

        int taskCount = (taskTitles != null) ? taskTitles.size() : 0;
        String messageText = taskCount > 1
                ? "You have " + taskCount + " tasks requiring your attention."
                : (taskCount == 1 ? taskTitles.get(0) : "You have a task deadline due.");

        // Initialize TTS for voice announcement
        tts = new TextToSpeech(context.getApplicationContext(), status -> {
            if (status == TextToSpeech.SUCCESS && tts != null) {
                try {
                    tts.setLanguage(Locale.US);
                    String speechText = taskCount > 1
                            ? "Attention! You have " + taskCount + " pending task deadlines."
                            : "Attention! Deadline is near for task: " + messageText;
                    tts.speak(speechText, TextToSpeech.QUEUE_FLUSH, null, "BatchDeadlineAlertTTS");
                } catch (Exception e) {
                    Log.e(TAG, "TTS speak failed", e);
                }
            }
        });

        try {
            // Inflate custom dialog layout
            View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_deadline_alert, null);

            TextView taskTitleTextView = dialogView.findViewById(R.id.tvTaskTitleContent);
            MaterialButton btnDismiss = dialogView.findViewById(R.id.btnDismissAlert);

            if (taskTitleTextView != null) {
                taskTitleTextView.setText(messageText);
            }

            // Update button text to reflect redirection action ("View Tasks" or "Got it")
            if (btnDismiss != null) {
                btnDismiss.setText("View Tasks");
                btnDismiss.setOnClickListener(v -> {
                    stopAlertAndTts();
                    if (onDismissCallback != null) {
                        onDismissCallback.run();
                    }
                });
            }

            currentDialog = new AlertDialog.Builder(context)
                    .setView(dialogView)
                    .setCancelable(false)
                    .create();

            if (currentDialog.getWindow() != null) {
                currentDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            }

            currentDialog.show();
        } catch (Exception e) {
            Log.e(TAG, "Failed to show consolidated alert dialog", e);
            stopAlertAndTts();
        }
    }

    /**
     * Immediately stops active TTS playback, dismisses dialog, and resets resources.
     */
    public static synchronized void stopAlertAndTts() {
        if (tts != null) {
            try {
                tts.stop();
                tts.shutdown();
            } catch (Exception ignored) {}
            tts = null;
        }

        if (currentDialog != null) {
            try {
                if (currentDialog.isShowing()) {
                    currentDialog.dismiss();
                }
            } catch (Exception ignored) {}
            currentDialog = null;
        }
    }
}