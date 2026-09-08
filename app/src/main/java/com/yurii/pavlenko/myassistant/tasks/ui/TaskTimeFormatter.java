package com.yurii.pavlenko.myassistant.tasks.ui;

import android.graphics.Color;
import android.widget.TextView;

import com.yurii.pavlenko.myassistant.tasks.model.Task;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public class TaskTimeFormatter {

    private static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
    private static final DateTimeFormatter DEADLINE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    public static void formatTimestamps(TextView textView, Task task) {
        StringBuilder sb = new StringBuilder();

        if (task.getCreatedAt() != null) {
            sb.append("Created: ").append(task.getCreatedAt().format(TIMESTAMP_FORMATTER));
        }
        if (task.getUpdatedAt() != null) {
            if (sb.length() > 0) {
                sb.append("\n");
            }
            sb.append("Edited: ").append(task.getUpdatedAt().format(TIMESTAMP_FORMATTER));
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

    public static String getFormattedDeadlineText(LocalDate deadline) {
        if (deadline == null) return null;
        long daysRemaining = ChronoUnit.DAYS.between(LocalDate.now(), deadline);
        return String.format("Deadline: %s — %d days left!",
                deadline.format(DEADLINE_FORMATTER), daysRemaining);
    }

    public static void formatDeadline(TextView deadlineTextView, Task task) {
        if (task.getDeadline() != null) {
            String text = getFormattedDeadlineText(task.getDeadline());
            deadlineTextView.setText(text);
            deadlineTextView.setTextColor(Color.parseColor("#00C853"));
            deadlineTextView.setVisibility(TextView.VISIBLE);
        } else {
            deadlineTextView.setVisibility(TextView.GONE);
        }
    }
}