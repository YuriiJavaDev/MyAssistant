package com.yurii.pavlenko.myassistant.tasks.ui.dialogs;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.yurii.pavlenko.myassistant.R;
import com.yurii.pavlenko.myassistant.tasks.ui.handlers.TaskDeadlinePickerHelper;

import java.time.LocalDate;

public class AddTaskDialog {

    public interface OnTaskCreatedListener {
        void onTaskCreated(String title, String importance, LocalDate deadline, boolean remindSound);
    }
    public static void show(Context context, String initialText, OnTaskCreatedListener listener) {
        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_add_task, null);

        EditText dialogTaskEditText = dialogView.findViewById(R.id.dialogTaskEditText);
        Spinner dialogImportanceSpinner = dialogView.findViewById(R.id.dialogImportanceSpinner);
        TextView dialogDeadlineTextView = dialogView.findViewById(R.id.dialogDeadlineTextView);
        CheckBox dialogRemindCheckBox = dialogView.findViewById(R.id.dialogRemindCheckBox);

        dialogTaskEditText.setText(initialText);
        dialogTaskEditText.setSelection(dialogTaskEditText.getText().length());

        String[] importanceOptions = {"Normal", "Important", "Urgent"};
        ArrayAdapter<String> importanceAdapter = new ArrayAdapter<>(context, R.layout.item_spinner, importanceOptions);
        importanceAdapter.setDropDownViewResource(R.layout.item_spinner);
        dialogImportanceSpinner.setAdapter(importanceAdapter);
        dialogImportanceSpinner.setSelection(0);

        TaskDeadlinePickerHelper deadlineHelper = new TaskDeadlinePickerHelper(context);
        deadlineHelper.setupDeadlinePicker(context, dialogDeadlineTextView, dialogRemindCheckBox, null);

        AlertDialog dialog = new AlertDialog.Builder(context)
                .setTitle("Add New Task")
                .setView(dialogView)
                .setPositiveButton("OK", (dialogInterface, which) -> {
                    String description = dialogTaskEditText.getText().toString().trim();
                    String importance = dialogImportanceSpinner.getSelectedItem().toString();
                    LocalDate deadline = deadlineHelper.getSelectedDeadline();
                    boolean remindSound = dialogRemindCheckBox.isChecked();

                    if (!description.isEmpty()) {
                        listener.onTaskCreated(description, importance, deadline, remindSound);
                    } else {
                        Toast.makeText(context, "Please enter a task title", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", (dialogInterface, which) -> dialogInterface.dismiss())
                .create();

        dialog.setOnDismissListener(dialogInterface -> deadlineHelper.release());
        dialog.show();
    }
}