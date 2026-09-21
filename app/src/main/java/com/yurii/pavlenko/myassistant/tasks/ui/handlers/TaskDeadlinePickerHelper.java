package com.yurii.pavlenko.myassistant.tasks.ui.handlers;

import android.app.DatePickerDialog;
import android.content.Context;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.yurii.pavlenko.myassistant.R;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public class TaskDeadlinePickerHelper {
    private final Context context;
    private LocalDate selectedDeadline;
    private TextView deadlineTextView;
    private CheckBox remindCheckBox;
    private TextView customReminderTextView;
    private LocalDate customReminderDate;

    public TaskDeadlinePickerHelper(Context context) {
        this.context = context;
        this.selectedDeadline = null;
        this.customReminderDate = null;
    }

    public void setupDeadlinePicker(Context context,
                                    TextView deadlineTextView,
                                    CheckBox remindCheckBox,
                                    TextView customReminderTextView,
                                    @Nullable LocalDate initialDeadline,
                                    @Nullable LocalDate initialCustomReminder) {
        this.deadlineTextView = deadlineTextView;
        this.remindCheckBox = remindCheckBox;
        this.customReminderTextView = customReminderTextView;
        this.selectedDeadline = initialDeadline;
        this.customReminderDate = initialCustomReminder;

        updateDeadlineDisplay();
        updateCustomReminderDisplay();

        deadlineTextView.setOnClickListener(v -> showDatePicker());
        customReminderTextView.setOnClickListener(v -> showCustomReminderDatePicker());
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

        if (datePickerDialog.getWindow() != null) {
            datePickerDialog.getWindow().setBackgroundDrawableResource(R.drawable.bg_spinner_dropdown);
        }
    }

    private void showCustomReminderDatePicker() {
        LocalDate initialDate = customReminderDate != null ? customReminderDate : LocalDate.now();

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                context,
                (view, year, month, dayOfMonth) -> {
                    customReminderDate = LocalDate.of(year, month + 1, dayOfMonth);
                    updateCustomReminderDisplay();
                },
                initialDate.getYear(),
                initialDate.getMonthValue() - 1,
                initialDate.getDayOfMonth()
        );

        datePickerDialog.setButton(DatePickerDialog.BUTTON_NEGATIVE, "Clear", (dialog, which) -> {
            customReminderDate = null;
            updateCustomReminderDisplay();
        });

        datePickerDialog.show();

        if (datePickerDialog.getWindow() != null) {
            datePickerDialog.getWindow().setBackgroundDrawableResource(R.drawable.bg_spinner_dropdown);
        }
    }

    private void updateDeadlineDisplay() {
        if (selectedDeadline != null) {
            long daysLeft = ChronoUnit.DAYS.between(LocalDate.now(), selectedDeadline);
            String text = selectedDeadline.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")) + " — " + daysLeft + " days left!";
            deadlineTextView.setText(text);
        } else {
            deadlineTextView.setText("Select a deadline");
        }
    }

    private void updateCustomReminderDisplay() {
        if (customReminderDate != null) {
            String text = customReminderDate.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));
            customReminderTextView.setText(text);
        } else {
            customReminderTextView.setText("Select custom reminder time");
        }
    }

    public LocalDate getSelectedDeadline() {
        return selectedDeadline;
    }

    public LocalDate getCustomReminderDate() {
        return customReminderDate;
    }

    public void release() {
        deadlineTextView = null;
        remindCheckBox = null;
        customReminderTextView = null;
    }
}