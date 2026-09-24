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

    public static void showDeadlineAlert(Context context, Task task) {
        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_deadline_alert, null);

        TextView taskTitleTextView = dialogView.findViewById(R.id.tvTaskTitleContent);
        MaterialButton btnDismiss = dialogView.findViewById(R.id.btnDismissAlert);

        taskTitleTextView.setText(task.getTitle() != null ? task.getTitle() : "Untitled Task");

        AlertDialog dialog = new AlertDialog.Builder(context)
                .setView(dialogView)
                .setCancelable(false)
                .create();

        // Ініціалізація та первинний запуск озвучки
        initAndPlayTts(context);

        // Налаштування повторення кожні 5 хвилин (5 * 60 * 1000 мс)
        repeatHandler = new Handler(Looper.getMainLooper());
        repeatRunnable = new Runnable() {
            @Override
            public void run() {
                playTtsMessage();
                repeatHandler.postDelayed(this, 300000); // 5 хвилин
            }
        };
        repeatHandler.postDelayed(repeatRunnable, 300000);

        btnDismiss.setOnClickListener(v -> {
            stopAlertAndTts();
            dialog.dismiss();
        });

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        dialog.show();
    }

    private static void initAndPlayTts(Context context) {
        tts = new TextToSpeech(context, status -> {
            if (status == TextToSpeech.SUCCESS) {
                tts.setLanguage(Locale.US);
                playTtsMessage();
            }
        });
    }

    private static void playTtsMessage() {
        if (tts != null) {
            tts.speak("Deadline is near! Check your task!", TextToSpeech.QUEUE_FLUSH, null, "DeadlineAlert");
        }
    }

    private static void stopAlertAndTts() {
        // Зупиняємо повторення таймера
        if (repeatHandler != null && repeatRunnable != null) {
            repeatHandler.removeCallbacks(repeatRunnable);
            repeatHandler = null;
            repeatRunnable = null;
        }

        // Зупиняємо та вивільняємо TextToSpeech
        if (tts != null) {
            tts.stop();
            tts.shutdown();
            tts = null;
        }
    }
}