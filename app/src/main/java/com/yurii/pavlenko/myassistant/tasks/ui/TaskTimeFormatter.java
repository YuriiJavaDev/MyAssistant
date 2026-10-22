package com.yurii.pavlenko.myassistant.tasks.ui;

import android.content.Context;
import android.widget.TextView;

import com.yurii.pavlenko.myassistant.R;
import com.yurii.pavlenko.myassistant.tasks.logic.DeadlineText;
import com.yurii.pavlenko.myassistant.tasks.model.Task;
import com.yurii.pavlenko.myassistant.ui.theme.ThemeColors;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class TaskTimeFormatter {

    private static final DateTimeFormatter DEADLINE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    public static void formatTimestamps(TextView textView, Task task) {
        Context context = textView.getContext();
        StringBuilder sb = new StringBuilder();

        if (task.isShowTimestamps()) {
            if (task.getCreatedAt() != null) {
                sb.append(context.getString(R.string.tasks_time_created, formatDateTime(task.getCreatedAt())));
            }
            if (task.getUpdatedAt() != null) {
                if (sb.length() > 0) {
                    sb.append("\n");
                }
                sb.append(context.getString(R.string.tasks_time_edited, formatDateTime(task.getUpdatedAt())));
            }
        }

        if (task.isCompleted() && task.getCompletedAt() != null) {
            if (sb.length() > 0) {
                sb.append("\n");
            }
            sb.append(context.getString(R.string.tasks_time_completed, formatDateTime(task.getCompletedAt())));
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

    /** For example "05.11.2026 — 3 days left!" or "01.11.2026 — Overdue by 1 day!". */
    public static String getRawDeadlineText(Context context, LocalDate deadline) {
        long daysLeft = DeadlineText.daysLeft(LocalDate.now(), deadline);
        String date = deadline.format(DEADLINE_FORMATTER);
        if (daysLeft < 0) {
            return context.getResources().getQuantityString(R.plurals.tasks_deadline_overdue, quantity(-daysLeft), date, -daysLeft);
        }
        return context.getResources().getQuantityString(R.plurals.tasks_deadline_days_left, quantity(daysLeft), date, daysLeft);
    }

    private static int quantity(long days) {
        return (int) Math.min(days, Integer.MAX_VALUE);
    }

    public static void formatDeadline(TextView deadlineTextView, Task task) {
        LocalDate deadline = task.getDeadline();
        if (deadline != null) {
            Context context = deadlineTextView.getContext();
            deadlineTextView.setText(context.getString(R.string.tasks_deadline_prefixed, getRawDeadlineText(context, deadline)));

            if (task.isCompleted()) {
                deadlineTextView.setTextColor(ThemeColors.get(context, R.attr.maTextMuted));
            } else {
                if (DeadlineText.isOverdue(LocalDate.now(), deadline)) {
                    deadlineTextView.setTextColor(ThemeColors.get(context, R.attr.maOverdue));
                } else {
                    deadlineTextView.setTextColor(ThemeColors.get(context, R.attr.maOk));
                }
            }

            deadlineTextView.setVisibility(TextView.VISIBLE);
        } else {
            deadlineTextView.setVisibility(TextView.GONE);
        }
    }
}