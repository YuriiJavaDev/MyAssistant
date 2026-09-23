package com.yurii.pavlenko.myassistant.tasks.ui;

import android.graphics.Color;
import android.widget.TextView;

import com.yurii.pavlenko.myassistant.tasks.model.Task;

import java.util.Locale;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public class TaskTimeFormatter {

    private static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
    private static final DateTimeFormatter DEADLINE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");

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

    public static String getRawDeadlineText(LocalDate deadline) {
        if (deadline == null) return null;
        long daysLeft = ChronoUnit.DAYS.between(LocalDate.now(), deadline);
        if (daysLeft < 0) {
            long overdueDays = Math.abs(daysLeft);
            return String.format(Locale.US, "%s — Overdue by %d %s!",
                    deadline.format(DEADLINE_FORMATTER),
                    overdueDays,
                    overdueDays == 1 ? "day" : "days");
        } else {
            return String.format(Locale.US, "%s — %d %s left!",
                    deadline.format(DEADLINE_FORMATTER),
                    daysLeft,
                    daysLeft == 1 ? "day" : "days");
        }
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

            if (task.isCompleted()) {
                deadlineTextView.setTextColor(Color.parseColor("#9E9E9E")); // Сірий
            } else {
                long daysLeft = ChronoUnit.DAYS.between(LocalDate.now(), deadline);
                if (daysLeft < 0) {
                    deadlineTextView.setTextColor(Color.parseColor("#D32F2F")); // Червоний
                } else {
                    deadlineTextView.setTextColor(Color.parseColor("#00C853")); // Зелений
                }
            }

            deadlineTextView.setVisibility(TextView.VISIBLE);
        } else {
            deadlineTextView.setVisibility(TextView.GONE);
        }
    }
}