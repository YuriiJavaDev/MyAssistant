package com.yurii.pavlenko.myassistant.tasks.ui;

import android.graphics.Color;
import android.graphics.Paint;
import android.widget.TextView;

public class TaskStyleHelper {

    public static void applyCompletionStyle(TextView textView, boolean isCompleted) {
        if (isCompleted) {
            textView.setPaintFlags(textView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        } else {
            textView.setPaintFlags(textView.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
        }
    }

    public static void applyImportanceColor(TextView textView, String importance) {
        if (importance == null) {
            importance = "Normal";
        }
        switch (importance.toLowerCase()) {
            case "urgent":
                textView.setTextColor(Color.parseColor("#D32F2F")); // Red
                break;
            case "important":
                textView.setTextColor(Color.parseColor("#FA8C16")); // Brownish-orange
                break;
            case "normal":
            default:
                textView.setTextColor(Color.parseColor("#3E2773")); // Standard app text color
                break;
        }
    }
}