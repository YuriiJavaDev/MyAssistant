package com.yurii.pavlenko.myassistant.tasks.ui.handlers;

import android.app.DatePickerDialog;
import android.content.Context;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.Nullable;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public class TaskDeadlinePickerHelper {
    private final Context context;
    private LocalDate selectedDeadline;
    private TextView deadlineTextView;
    private CheckBox remindCheckBox;

    public TaskDeadlinePickerHelper(Context context) {
        this.context = context;
        this.selectedDeadline = null;
    }

    public void setupDeadlinePicker(Context context, TextView deadlineTextView, CheckBox remindCheckBox, @Nullable LocalDate initialDeadline) {
        this.deadlineTextView = deadlineTextView;
        this.remindCheckBox = remindCheckBox;
        this.selectedDeadline = initialDeadline;

        updateDeadlineDisplay();

        deadlineTextView.setOnClickListener(v -> showDatePicker());
    }

    private void showDatePicker() {
        LocalDate initialDate = selectedDeadline != null ? selectedDeadline : LocalDate.now();

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                context,
                (view, year, month, dayOfMonth) -> {
                    selectedDeadline = LocalDate.of(year, month + 1, dayOfMonth);
                    updateDeadlineDisplay();

                    if (remindCheckBox != null) {
                        remindCheckBox.setEnabled(true);
                    }
                },
                initialDate.getYear(),
                initialDate.getMonthValue() - 1,
                initialDate.getDayOfMonth()
        );

        datePickerDialog.setButton(DatePickerDialog.BUTTON_NEGATIVE, "Clear", (dialog, which) -> {
            selectedDeadline = null;
            updateDeadlineDisplay();

            if (remindCheckBox != null) {
                remindCheckBox.setEnabled(false);
                remindCheckBox.setChecked(false);
            }
        });

        datePickerDialog.show();
    }

    private void updateDeadlineDisplay() {
        if (selectedDeadline != null) {
            long daysLeft = ChronoUnit.DAYS.between(LocalDate.now(), selectedDeadline);
            String text = "Deadline: " + selectedDeadline.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")) + " — " + daysLeft + " days left!";
            deadlineTextView.setText(text);
        } else {
            deadlineTextView.setText("Click to select a deadline");
        }
    }

    public LocalDate getSelectedDeadline() {
        return selectedDeadline;
    }

    public void release() {
        deadlineTextView = null;
        remindCheckBox = null;
    }
}