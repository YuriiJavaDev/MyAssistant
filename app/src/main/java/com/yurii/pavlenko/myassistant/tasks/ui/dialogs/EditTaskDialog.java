package com.yurii.pavlenko.myassistant.tasks.ui.dialogs;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.yurii.pavlenko.myassistant.R;
import com.yurii.pavlenko.myassistant.tasks.model.Task;

public class EditTaskDialog {

    public interface OnTaskUpdatedListener {
        void onTaskUpdated(Task task, String title, String importance);
    }

    public interface OnTaskDeletedListener {
        void onTaskDeleted(Task task);
    }

    public static void show(Context context, Task task, OnTaskUpdatedListener updateListener, OnTaskDeletedListener deleteListener) {
        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_add_task, null);

        EditText dialogTaskEditText = dialogView.findViewById(R.id.dialogTaskEditText);
        Spinner dialogImportanceSpinner = dialogView.findViewById(R.id.dialogImportanceSpinner);

        dialogTaskEditText.setText(task.getTitle());
        dialogTaskEditText.setSelection(dialogTaskEditText.getText().length());

        String[] importanceOptions = {"Normal", "Important", "Urgent"};
        ArrayAdapter<String> importanceAdapter = new ArrayAdapter<>(context, R.layout.item_spinner, importanceOptions);
        importanceAdapter.setDropDownViewResource(R.layout.item_spinner);
        dialogImportanceSpinner.setAdapter(importanceAdapter);

        if (task.getImportance() != null) {
            for (int i = 0; i < importanceOptions.length; i++) {
                if (importanceOptions[i].equalsIgnoreCase(task.getImportance())) {
                    dialogImportanceSpinner.setSelection(i);
                    break;
                }
            }
        }

        new AlertDialog.Builder(context)
                .setTitle("Edit task")
                .setView(dialogView)
                .setPositiveButton("Save", (dialog, which) -> {
                    String description = dialogTaskEditText.getText().toString().trim();
                    String importance = dialogImportanceSpinner.getSelectedItem().toString();

                    if (!description.isEmpty()) {
                        updateListener.onTaskUpdated(task, description, importance);
                        Toast.makeText(context, "Task updated", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(context, "Task description cannot be empty", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNeutralButton("Delete", (dialog, which) -> {
                    DeleteConfirmationDialog.show(context, true, () -> {
                        deleteListener.onTaskDeleted(task);
                        Toast.makeText(context, "Task deleted", Toast.LENGTH_SHORT).show();
                    });
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .create()
                .show();
    }
}