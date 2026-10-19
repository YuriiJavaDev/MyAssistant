package com.yurii.pavlenko.myassistant.tasks.ui;

import android.content.Context;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.yurii.pavlenko.myassistant.R;
import com.yurii.pavlenko.myassistant.tasks.logic.DeadlineText;
import com.yurii.pavlenko.myassistant.tasks.model.Task;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class TaskTimeFormatter {

    private static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    public static void formatTimestamps(TextView textView, Task task) {
        StringBuilder sb = new StringBuilder();

        if (task.isShowTimestamps()) {
            if (task.getCreatedAt() != null) {
                sb.append("Created: ").append(task.getCreatedAt().format(TIMESTAMP_FORMATTER));
            }
            if (task.getUpdatedAt() != null) {
                if (sb.length() > 0) {
                    sb.append("\n");
                }
                sb.append("Edited: ").append(task.getUpdatedAt().format(TIMESTAMP_FORMATTER));
            }
        }

        if (task.isCompleted() && task.getCompletedAt() != null) {
            if (sb.length() > 0) {
                sb.append("\n");
            }
            sb.append("Completed: ").append(task.getCompletedAt().format(TIMESTAMP_FORMATTER));
        }

        if (sb.length() > 0) {
            textView.setText(sb.toString());
            textView.setVisibility(TextView.VISIBLE);
        } else {
            textView.setVisibility(TextView.GONE);
        }
    }

    public static String formatDateTime(LocalDateTime dateTime) {
        return dateTime.format(TIMESTAMP_FORMATTER);
    }

    public static String getRawDeadlineText(LocalDate deadline) {
        return DeadlineText.describe(LocalDate.now(), deadline);
    }

    public static String getFormattedDeadlineText(LocalDate deadline) {
        String rawText = getRawDeadlineText(deadline);
        return rawText != null ? "Deadline: " + rawText : null;
    }

    public static void formatDeadline(TextView deadlineTextView, Task task) {
        LocalDate deadline = task.getDeadline();
        if (deadline != null) {
            String text = getFormattedDeadlineText(deadline);
            deadlineTextView.setText(text);

            Context context = deadlineTextView.getContext();
            if (task.isCompleted()) {
                deadlineTextView.setTextColor(ContextCompat.getColor(context, R.color.text_muted));
            } else {
                if (DeadlineText.isOverdue(LocalDate.now(), deadline)) {
                    deadlineTextView.setTextColor(ContextCompat.getColor(context, R.color.status_overdue));
                } else {
                    deadlineTextView.setTextColor(ContextCompat.getColor(context, R.color.status_ok));
                }
            }

            deadlineTextView.setVisibility(TextView.VISIBLE);
        } else {
            deadlineTextView.setVisibility(TextView.GONE);
        }
    }
}