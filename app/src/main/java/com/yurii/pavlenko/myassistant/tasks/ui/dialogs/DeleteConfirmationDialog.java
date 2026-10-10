package com.yurii.pavlenko.myassistant.tasks.ui.dialogs;

import android.app.AlertDialog;
import android.content.Context;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import com.yurii.pavlenko.myassistant.R;
import com.yurii.pavlenko.myassistant.tasks.model.Task;

import java.util.List;

public class DeleteConfirmationDialog {

    private static final String CANNOT_BE_UNDONE = " This action cannot be undone!";

    // Старый метод для удаления (оставляем для обратной совместимости)
    public static AlertDialog show(Context context, boolean canDelete, Runnable onConfirmed) {
        if (!canDelete) {
            Toast.makeText(context, "No objects found to delete!", Toast.LENGTH_SHORT).show();
            return null;
        }
        return showCustom(context,
                "Delete Confirmation",
                "Are you sure you want to delete this item? This action cannot be undone!",
                "Delete",
                onConfirmed);
    }

    public static AlertDialog showDeleteCompleted(Context context, List<Task> allTasks, Runnable onConfirmed) {
        long completed = allTasks.stream().filter(Task::isCompleted).count();
        if (completed == 0) {
            return show(context, false, onConfirmed);
        }
        long withReminder = allTasks.stream()
                .filter(t -> t.isCompleted() && t.getCustomReminderDateTime() != null)
                .count();

        StringBuilder message = new StringBuilder("Delete " + completed + " completed " + taskWord(completed) + "?");
        if (withReminder > 0) {
            message.append(" ").append(withReminder).append(withReminder == 1 ? " of them still has" : " of them still have")
                    .append(" a saved reminder.");
        }
        return showCustom(context, "Delete Confirmation", message + CANNOT_BE_UNDONE, "Delete", onConfirmed);
    }

    public static AlertDialog showClearAll(Context context, List<Task> allTasks, Runnable onConfirmed) {
        if (allTasks.isEmpty()) {
            return show(context, false, onConfirmed);
        }
        long activeReminders = allTasks.stream()
                .filter(t -> !t.isCompleted() && t.getCustomReminderDateTime() != null)
                .count();

        StringBuilder message = new StringBuilder("Delete all " + allTasks.size() + " " + taskWord(allTasks.size()) + "?");
        if (activeReminders > 0) {
            message.append(" ").append(activeReminders)
                    .append(activeReminders == 1 ? " has an active reminder." : " have active reminders.");
        }
        return showCustom(context, "Delete Confirmation", message + CANNOT_BE_UNDONE, "Delete", onConfirmed);
    }

    private static String taskWord(long count) {
        return count == 1 ? "task" : "tasks";
    }

    public static AlertDialog showCustom(Context context, String title, String message, String positiveButtonText, Runnable onConfirmed) {
        AlertDialog dialog = new AlertDialog.Builder(context)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(positiveButtonText, (dialogInterface, which) -> onConfirmed.run())
                .setNegativeButton("Cancel", null)
                .create();

        dialog.show();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(R.drawable.bg_spinner_dropdown);
        }

        Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        Button negativeButton = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);

        int paddingHorizontal = (int) (16 * context.getResources().getDisplayMetrics().density);
        int marginHorizontal = (int) (8 * context.getResources().getDisplayMetrics().density);

        if (positiveButton != null) {
            positiveButton.setBackgroundTintList(null);
            positiveButton.setBackgroundResource(R.drawable.button_selector);
            positiveButton.setTextColor(ContextCompat.getColor(context, R.color.text_color_btn));
            positiveButton.setAllCaps(false);

            positiveButton.setPadding(paddingHorizontal, positiveButton.getPaddingTop(), paddingHorizontal, positiveButton.getPaddingBottom());

            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) positiveButton.getLayoutParams();
            params.leftMargin = marginHorizontal;
            positiveButton.setLayoutParams(params);
        }

        if (negativeButton != null) {
            negativeButton.setBackgroundTintList(null);
            negativeButton.setBackgroundResource(R.drawable.button_selector);
            negativeButton.setTextColor(ContextCompat.getColor(context, R.color.text_color_btn));
            negativeButton.setAllCaps(false);
            negativeButton.setPadding(paddingHorizontal, negativeButton.getPaddingTop(), paddingHorizontal, negativeButton.getPaddingBottom());
        }

        return dialog;
    }
}