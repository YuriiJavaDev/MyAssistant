package com.yurii.pavlenko.myassistant.tasks.ui.dialogs;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.yurii.pavlenko.myassistant.R;
import com.yurii.pavlenko.myassistant.databinding.DialogAddTaskBinding;
import com.yurii.pavlenko.myassistant.tasks.model.Task;
import com.yurii.pavlenko.myassistant.tasks.ui.handlers.TaskDeadlinePickerHelper;

import java.time.LocalDate;

public class TaskDialog {

    public interface OnTaskSavedListener {
        void onTaskSaved(String title, String importance, LocalDate deadline, boolean remindSound, boolean showTimestamps);
    }

    public interface OnTaskUpdatedListener {
        void onTaskUpdated(Task task, String title, String importance, LocalDate deadline, boolean remindSound, boolean showTimestamps);
    }

    public interface OnTaskDeletedListener {
        void onTaskDeleted(Task task);
    }

    public static void showCreate(Context context, @Nullable String initialText, OnTaskSavedListener listener) {
        show(context, null, initialText, listener, null, null);
    }

    public static void showEdit(Context context, Task task, OnTaskUpdatedListener updateListener, OnTaskDeletedListener deleteListener) {
        show(context, task, null, null, updateListener, deleteListener);
    }

    private static void show(Context context,
                             @Nullable Task task,
                             @Nullable String initialText,
                             @Nullable OnTaskSavedListener createListener,
                             @Nullable OnTaskUpdatedListener updateListener,
                             @Nullable OnTaskDeletedListener deleteListener) {

        DialogAddTaskBinding binding = DialogAddTaskBinding.inflate(LayoutInflater.from(context));

        String[] importanceOptions = {"Normal", "Important", "Urgent"};
        ArrayAdapter<String> importanceAdapter = new ArrayAdapter<>(context, R.layout.item_spinner, importanceOptions);
        importanceAdapter.setDropDownViewResource(R.layout.item_spinner);
        binding.dialogImportanceSpinner.setAdapter(importanceAdapter);

        TaskDeadlinePickerHelper deadlineHelper = new TaskDeadlinePickerHelper(context);
        boolean isEditMode = task != null;

        if (isEditMode) {
            // Edit mode setup
            binding.dialogTaskEditText.setText(task.getTitle());
            binding.dialogShowTimestampsCheckBox.setChecked(task.isShowTimestamps());
            binding.dialogRemindCheckBox.setChecked(task.isRemindSoundOneDayBefore());
            deadlineHelper.setupDeadlinePicker(context, binding.dialogDeadlineTextView, binding.dialogRemindCheckBox, task.getDeadline());

            if (task.getImportance() != null) {
                for (int i = 0; i < importanceOptions.length; i++) {
                    if (importanceOptions[i].equalsIgnoreCase(task.getImportance())) {
                        binding.dialogImportanceSpinner.setSelection(i);
                        break;
                    }
                }
            }
        } else {
            // Create mode setup
            if (initialText != null && !initialText.isEmpty()) {
                binding.dialogTaskEditText.setText(initialText);
            }
            binding.dialogShowTimestampsCheckBox.setChecked(true);
            deadlineHelper.setupDeadlinePicker(context, binding.dialogDeadlineTextView, binding.dialogRemindCheckBox, null);
            binding.dialogImportanceSpinner.setSelection(0);
        }

        binding.dialogTaskEditText.setSelection(binding.dialogTaskEditText.getText().length());

        AlertDialog.Builder builder = new AlertDialog.Builder(context)
                .setTitle(isEditMode ? "Edit Task" : "Add New Task")
                .setView(binding.getRoot())
                .setPositiveButton(isEditMode ? "Save" : "Add", (dialog, which) -> {
                    String title = binding.dialogTaskEditText.getText().toString().trim();
                    String importance = binding.dialogImportanceSpinner.getSelectedItem().toString();
                    LocalDate deadline = deadlineHelper.getSelectedDeadline();
                    boolean remindSound = binding.dialogRemindCheckBox.isChecked();
                    boolean showTimestamps = binding.dialogShowTimestampsCheckBox.isChecked();

                    if (title.isEmpty()) {
                        Toast.makeText(context, "Task description cannot be empty", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (isEditMode && updateListener != null) {
                        updateListener.onTaskUpdated(task, title, importance, deadline, remindSound, showTimestamps);
                        Toast.makeText(context, "Task updated", Toast.LENGTH_SHORT).show();
                    } else if (!isEditMode && createListener != null) {
                        createListener.onTaskSaved(title, importance, deadline, remindSound, showTimestamps);
                        Toast.makeText(context, "Task created", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        // Add Delete button only in edit mode
        if (isEditMode && deleteListener != null) {
            builder.setNeutralButton("Delete", (dialog, which) -> {
                DeleteConfirmationDialog.show(context, true, () -> {
                    deleteListener.onTaskDeleted(task);
                    Toast.makeText(context, "Task deleted", Toast.LENGTH_SHORT).show();
                });
            });
        }

        AlertDialog dialog = builder.create();
        dialog.setOnDismissListener(dialogInterface -> deadlineHelper.release());
        dialog.show();
    }
}