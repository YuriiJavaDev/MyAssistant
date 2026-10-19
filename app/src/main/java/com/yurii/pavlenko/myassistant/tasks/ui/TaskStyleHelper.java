package com.yurii.pavlenko.myassistant.tasks.ui;

import android.content.Context;
import android.graphics.Paint;
import android.widget.TextView;


import com.yurii.pavlenko.myassistant.R;
import com.yurii.pavlenko.myassistant.ui.theme.ThemeColors;

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
            textView.setTextColor(ThemeColors.get(context, R.attr.maTextMuted));
            return;
        }

        if (importance == null) {
            importance = "Normal";
        }
        switch (importance.toLowerCase()) {
            case "urgent":
                textView.setTextColor(ThemeColors.get(context, R.attr.maOverdue));
                break;
            case "important":
                textView.setTextColor(ThemeColors.get(context, R.attr.maImportant));
                break;
            case "normal":
            default:
                textView.setTextColor(ThemeColors.get(context, R.attr.maText));
                break;
        }
    }
}