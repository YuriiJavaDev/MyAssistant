package com.yurii.pavlenko.myassistant.tasks.ui;

import android.widget.TextView;

import com.yurii.pavlenko.myassistant.tasks.model.Task;

import java.time.format.DateTimeFormatter;

public class TaskTimeFormatter {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    public static void formatTimestamps(TextView textView, Task task) {
        StringBuilder sb = new StringBuilder();

        if (task.getCreatedAt() != null) {
            sb.append("Created: ").append(task.getCreatedAt().format(DATE_FORMATTER));
        }
        if (task.getUpdatedAt() != null) {
            if (sb.length() > 0) {
                sb.append("\n");
            }
            sb.append("Edited: ").append(task.getUpdatedAt().format(DATE_FORMATTER));
        }
        if (task.isCompleted() && task.getCompletedAt() != null) {
            if (sb.length() > 0) {
                sb.append("\n");
            }
            sb.append("Completed: ").append(task.getCompletedAt().format(DATE_FORMATTER));
        }

        // TODO: Здесь в будущем легко добавим форматирование дедлайна и статус напоминания
        // formatDeadline(sb, task);

        textView.setText(sb.toString());
    }

    /*
    private static void formatDeadline(StringBuilder sb, Task task) {
        if (task.getDeadlineAt() != null) {
            if (sb.length() > 0) {
                sb.append("\n");
            }
            sb.append("Deadline: ").append(task.getDeadlineAt().format(DATE_FORMATTER));
            // Здесь же можно будет проверять, приближается ли событие,
            // и инициировать световую/звуковую индикацию
        }
    }
    */
}