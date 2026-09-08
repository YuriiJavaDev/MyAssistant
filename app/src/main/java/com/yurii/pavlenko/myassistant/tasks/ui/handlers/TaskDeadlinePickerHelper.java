package com.yurii.pavlenko.myassistant.tasks.ui.handlers;

import android.app.DatePickerDialog;
import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.widget.CheckBox;
import android.widget.TextView;

import com.yurii.pavlenko.myassistant.tasks.ui.TaskTimeFormatter;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Locale;

public class TaskDeadlinePickerHelper {

    private LocalDate selectedDeadline;
    private TextToSpeech textToSpeech;
    private boolean isTtsInitialized = false;
    private static final DateTimeFormatter DEADLINE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    public TaskDeadlinePickerHelper(Context context) {
        textToSpeech = new TextToSpeech(context, status -> {
            if (status == TextToSpeech.SUCCESS) {
                textToSpeech.setLanguage(Locale.ENGLISH);
                isTtsInitialized = true;
            }
        });
    }

    public void setupDeadlinePicker(Context context, TextView deadlineDisplayTextView,
                                    CheckBox remindCheckBox, LocalDate initialDate) {
        this.selectedDeadline = initialDate;
        updateDeadlineDisplay(deadlineDisplayTextView);

        deadlineDisplayTextView.setOnClickListener(v -> {
            LocalDate baseDate = selectedDeadline != null ? selectedDeadline : LocalDate.now();
            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    context,
                    (view, year, month, dayOfMonth) -> {
                        selectedDeadline = LocalDate.of(year, month + 1, dayOfMonth);
                        updateDeadlineDisplay(deadlineDisplayTextView);
                        speakSelection("Deadline set to " + selectedDeadline.format(DEADLINE_FORMATTER));
                    },
                    baseDate.getYear(),
                    baseDate.getMonthValue() - 1,
                    baseDate.getDayOfMonth()
            );
            datePickerDialog.show();
        });

        remindCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                speakSelection("Sound reminder one day before enabled");
            }
        });
    }
    private void updateDeadlineDisplay(TextView textView) {
        if (selectedDeadline != null) {
            String formattedText = TaskTimeFormatter.getFormattedDeadlineText(selectedDeadline);
            textView.setText(formattedText);
        } else {
            textView.setText("Click to select a deadline");
        }
    }
    public void speakSelection(String message) {
        if (isTtsInitialized && textToSpeech != null) {
            textToSpeech.speak(message, TextToSpeech.QUEUE_FLUSH, null, null);
        }
    }

    public LocalDate getSelectedDeadline() {
        return selectedDeadline;
    }
    public void release() {
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
    }
}