package com.yurii.pavlenko.myassistant.tasks.ui.dialogs;

import android.content.Context;
import android.content.res.Resources;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;


import com.yurii.pavlenko.myassistant.R;
import com.yurii.pavlenko.myassistant.tasks.model.Task;
import com.yurii.pavlenko.myassistant.ui.theme.DialogStyle;

import java.util.ArrayList;
import java.util.List;

public class DeleteConfirmationDialog {

    /** Confirms deleting a single item; with nothing to delete it only says so and returns null. */
    public static AlertDialog show(Context context, boolean canDelete, Runnable onConfirmed) {
        if (!canDelete) {
            Toast.makeText(context, R.string.tasks_delete_nothing, Toast.LENGTH_SHORT).show();
            return null;
        }
        return showCustom(context,
                context.getString(R.string.tasks_delete_title),
                context.getString(R.string.tasks_delete_item_message),
                context.getString(R.string.tasks_delete_button),
                onConfirmed);
    }

    public static AlertDialog showDeleteCompleted(Context context, List<Task> allTasks, Runnable onConfirmed) {
        int completed = (int) allTasks.stream().filter(Task::isCompleted).count();
        if (completed == 0) {
            return show(context, false, onConfirmed);
        }
        int withReminder = (int) allTasks.stream()
                .filter(t -> t.isCompleted() && t.getCustomReminderDateTime() != null)
                .count();

        Resources resources = context.getResources();
        List<String> sentences = new ArrayList<>();
        sentences.add(resources.getQuantityString(R.plurals.tasks_delete_completed_question, completed, completed));
        if (withReminder > 0) {
            sentences.add(resources.getQuantityString(R.plurals.tasks_delete_completed_reminders, withReminder, withReminder));
        }
        return showConfirmed(context, sentences, onConfirmed);
    }

    public static AlertDialog showClearAll(Context context, List<Task> allTasks, Runnable onConfirmed) {
        if (allTasks.isEmpty()) {
            return show(context, false, onConfirmed);
        }
        int activeReminders = (int) allTasks.stream()
                .filter(t -> !t.isCompleted() && t.getCustomReminderDateTime() != null)
                .count();

        Resources resources = context.getResources();
        List<String> sentences = new ArrayList<>();
        sentences.add(resources.getQuantityString(R.plurals.tasks_delete_all_question, allTasks.size(), allTasks.size()));
        if (activeReminders > 0) {
            sentences.add(resources.getQuantityString(R.plurals.tasks_delete_all_reminders, activeReminders, activeReminders));
        }
        return showConfirmed(context, sentences, onConfirmed);
    }

    private static AlertDialog showConfirmed(Context context, List<String> sentences, Runnable onConfirmed) {
        sentences.add(context.getString(R.string.tasks_delete_cannot_be_undone));
        return showCustom(context,
                context.getString(R.string.tasks_delete_title),
                TextUtils.join(" ", sentences),
                context.getString(R.string.tasks_delete_button),
                onConfirmed);
    }

    public static AlertDialog showCustom(Context context, String title, String message, String positiveButtonText, Runnable onConfirmed) {
        return DialogStyle.show(new AlertDialog.Builder(context)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(positiveButtonText, (dialogInterface, which) -> onConfirmed.run())
                .setNegativeButton(R.string.common_cancel, null));
    }
}
