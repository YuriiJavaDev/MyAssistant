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

public class AddTaskDialog {

    public interface OnTaskCreatedListener {
        void onTaskCreated(String title, String importance);
    }

    public static void show(Context context, String initialText, OnTaskCreatedListener listener) {
        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_add_task, null);

        EditText dialogTaskEditText = dialogView.findViewById(R.id.dialogTaskEditText);
        Spinner dialogImportanceSpinner = dialogView.findViewById(R.id.dialogImportanceSpinner);

        dialogTaskEditText.setText(initialText);
        dialogTaskEditText.setSelection(dialogTaskEditText.getText().length());

        String[] importanceOptions = {"Normal", "Important", "Urgent"};
        ArrayAdapter<String> importanceAdapter = new ArrayAdapter<>(context, R.layout.item_spinner, importanceOptions);
        importanceAdapter.setDropDownViewResource(R.layout.item_spinner);
        dialogImportanceSpinner.setAdapter(importanceAdapter);
        dialogImportanceSpinner.setSelection(0);

        new AlertDialog.Builder(context)
                .setTitle("Add New Task")
                .setView(dialogView)
                .setPositiveButton("OK", (dialog, which) -> {
                    String description = dialogTaskEditText.getText().toString().trim();
                    String importance = dialogImportanceSpinner.getSelectedItem().toString();

                    if (!description.isEmpty()) {
                        listener.onTaskCreated(description, importance);
                    } else {
                        Toast.makeText(context, "Please enter a task title", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .create()
                .show();
    }
}