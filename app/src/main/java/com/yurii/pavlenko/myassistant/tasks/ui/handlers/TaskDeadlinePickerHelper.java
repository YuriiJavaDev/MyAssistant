package com.yurii.pavlenko.myassistant.tasks.ui.handlers;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.res.ColorStateList;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.yurii.pavlenko.myassistant.R;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public class TaskDeadlinePickerHelper {
    private final Context context;
    private LocalDate selectedDeadline;
    private LocalDateTime customReminderDateTime;

    private TextView deadlineTextView;
    private TextView customReminderTextView;
    private ColorStateList defaultDeadlineTextColor;

    public TaskDeadlinePickerHelper(Context context) {
        this.context = context;
        this.selectedDeadline = null;
        this.customReminderDateTime = null;
    }

    public void setupDeadlinePicker(TextView deadlineTextView,
                                    TextView customReminderTextView,
                                    @Nullable LocalDate initialDeadline,
                                    @Nullable LocalDateTime initialCustomReminder) {
        this.deadlineTextView = deadlineTextView;
        this.customReminderTextView = customReminderTextView;
        this.defaultDeadlineTextColor = deadlineTextView.getTextColors();

        this.selectedDeadline = initialDeadline;

        // If the reminder is already overdue (in the past), automatically clear it
        if (initialCustomReminder != null && initialCustomReminder.isBefore(LocalDateTime.now())) {
            this.customReminderDateTime = null;
        } else {
            this.customReminderDateTime = initialCustomReminder;
        }

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
                    LocalDate newDeadline = LocalDate.of(year, month + 1, dayOfMonth);

                    // If a custom reminder exists and the new deadline is earlier, clear the reminder
                    if (customReminderDateTime != null) {
                        LocalDateTime deadlineDateTime = newDeadline.atTime(23, 59, 59);
                        if (customReminderDateTime.isAfter(deadlineDateTime)) {
                            customReminderDateTime = null;
                            updateCustomReminderDisplay();
                            Toast.makeText(context, "Reminder was cleared because the deadline is earlier!", Toast.LENGTH_LONG).show();
                        }
                    }

                    selectedDeadline = newDeadline;
                    updateDeadlineDisplay();
                },
                initialDate.getYear(),
                initialDate.getMonthValue() - 1,
                initialDate.getDayOfMonth()
        );

        // UI RESTRICTION: Disable past dates in the calendar view
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis());

        datePickerDialog.setButton(DatePickerDialog.BUTTON_NEGATIVE, "Clear", (dialog, which) -> {
            selectedDeadline = null;
            updateDeadlineDisplay();
        });

        datePickerDialog.show();
        if (datePickerDialog.getWindow() != null) {
            datePickerDialog.getWindow().setBackgroundDrawableResource(R.drawable.bg_spinner_dropdown);
        }
    }

    private void showCustomReminderDatePicker() {
        LocalDate initialDate = customReminderDateTime != null ? customReminderDateTime.toLocalDate() : LocalDate.now();

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                context,
                (view, year, month, dayOfMonth) -> {
                    LocalDate pickedDate = LocalDate.of(year, month + 1, dayOfMonth);

                    // VALIDATION: Reminder cannot be later than the deadline
                    if (selectedDeadline != null && pickedDate.isAfter(selectedDeadline)) {
                        Toast.makeText(context, "Reminder cannot be later than the deadline!", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    showCustomReminderTimePicker(pickedDate);
                },
                initialDate.getYear(),
                initialDate.getMonthValue() - 1,
                initialDate.getDayOfMonth()
        );

        // UI RESTRICTION: Disable past dates in the calendar view
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis());

        datePickerDialog.setButton(DatePickerDialog.BUTTON_NEGATIVE, "Clear", (dialog, which) -> {
            customReminderDateTime = null;
            updateCustomReminderDisplay();
        });

        datePickerDialog.show();
        if (datePickerDialog.getWindow() != null) {
            datePickerDialog.getWindow().setBackgroundDrawableResource(R.drawable.bg_spinner_dropdown);
        }
    }

    private void showCustomReminderTimePicker(LocalDate pickedDate) {
        // SMART UX: If the picked date is today, default to the current time or next hour to prevent past selection
        LocalTime initialTime;
        if (pickedDate.equals(LocalDate.now())) {
            LocalTime now = LocalTime.now();
            initialTime = (customReminderDateTime != null && customReminderDateTime.toLocalDate().equals(pickedDate))
                    ? customReminderDateTime.toLocalTime()
                    : now;
        } else {
            initialTime = customReminderDateTime != null ? customReminderDateTime.toLocalTime() : LocalTime.of(9, 0);
        }

        TimePickerDialog timePickerDialog = new TimePickerDialog(
                context,
                (view, hourOfDay, minute) -> {
                    LocalDateTime pickedDateTime = LocalDateTime.of(pickedDate, LocalTime.of(hourOfDay, minute));

                    // VALIDATION: Reminder time cannot be in the past for today
                    if (pickedDateTime.isBefore(LocalDateTime.now())) {
                        Toast.makeText(context, "Reminder cannot be in the past!", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // VALIDATION: Reminder cannot be later than the deadline
                    if (selectedDeadline != null) {
                        LocalDateTime deadlineDateTime = selectedDeadline.atTime(23, 59, 59);
                        if (pickedDateTime.isAfter(deadlineDateTime)) {
                            Toast.makeText(context, "Reminder cannot be later than the deadline!", Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }

                    customReminderDateTime = pickedDateTime;
                    updateCustomReminderDisplay();
                },
                initialTime.getHour(),
                initialTime.getMinute(),
                true // 24-hour format
        );

        timePickerDialog.show();
    }

    private void updateDeadlineDisplay() {
        if (selectedDeadline != null) {
            long daysLeft = ChronoUnit.DAYS.between(LocalDate.now(), selectedDeadline);
            if (daysLeft < 0) {
                long overdueDays = Math.abs(daysLeft);
                String text = selectedDeadline.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")) + " — Overdue by " + overdueDays + (overdueDays == 1 ? " day!" : " days!");
                deadlineTextView.setText(text);
                // Red color for the overdue deadline
                deadlineTextView.setTextColor(context.getResources().getColor(android.R.color.holo_red_dark, context.getTheme()));
            } else {
                String text = selectedDeadline.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")) + " — " + daysLeft + (daysLeft == 1 ? " day left!" : " days left!");
                deadlineTextView.setText(text);
                if (defaultDeadlineTextColor != null) {
                    deadlineTextView.setTextColor(defaultDeadlineTextColor);
                }
            }
        } else {
            deadlineTextView.setText("Select a deadline");
            if (defaultDeadlineTextColor != null) {
                deadlineTextView.setTextColor(defaultDeadlineTextColor);
            }
        }
    }

    private void updateCustomReminderDisplay() {
        if (customReminderDateTime != null) {
            String text = customReminderDateTime.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));
            customReminderTextView.setText(text);
        } else {
            customReminderTextView.setText("Select reminder date & time");
        }
    }

    public LocalDate getSelectedDeadline() {
        return selectedDeadline;
    }

    public LocalDateTime getCustomReminderDateTime() {
        return customReminderDateTime;
    }

    public void release() {
        deadlineTextView = null;
        customReminderTextView = null;
    }
}