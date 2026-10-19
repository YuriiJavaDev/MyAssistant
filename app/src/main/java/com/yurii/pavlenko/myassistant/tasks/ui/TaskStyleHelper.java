package com.yurii.pavlenko.myassistant.tasks.ui;

import android.content.Context;
import android.graphics.Paint;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.yurii.pavlenko.myassistant.R;
import com.yurii.pavlenko.myassistant.tasks.logic.Importance;

public class TaskStyleHelper {

    public static void applyCompletionStyle(TextView textView, boolean isCompleted) {
        if (isCompleted) {
            textView.setPaintFlags(textView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        } else {
            textView.setPaintFlags(textView.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
        }
    }

    public static void applyImportanceColor(TextView textView, String importance, boolean isCompleted) {

        Context context = textView.getContext();
        if (isCompleted) {
            textView.setTextColor(ContextCompat.getColor(context, R.color.text_muted));
            return;
        }

        switch (Importance.fromLabel(importance)) {
            case URGENT:
                textView.setTextColor(ContextCompat.getColor(context, R.color.status_overdue));
                break;
            case IMPORTANT:
                textView.setTextColor(ContextCompat.getColor(context, R.color.status_important));
                break;
            case NORMAL:
            default:
                textView.setTextColor(ContextCompat.getColor(context, R.color.text_color));
                break;
        }
    }
}