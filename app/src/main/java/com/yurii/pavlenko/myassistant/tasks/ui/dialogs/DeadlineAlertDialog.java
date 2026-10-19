package com.yurii.pavlenko.myassistant.tasks.ui.dialogs;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.yurii.pavlenko.myassistant.R;
import com.yurii.pavlenko.myassistant.tasks.notifications.DeadlineSpeaker;

/** The alert for one task whose reminder has already passed: shown in a dialog and read aloud. */
final class DeadlineAlertDialog {

    private final AlertDialog dialog;
    private final DeadlineSpeaker speaker;

    private DeadlineAlertDialog(Context context, String taskTitle) {
        View content = LayoutInflater.from(context).inflate(R.layout.dialog_deadline_alert, null);
        ((TextView) content.findViewById(R.id.tvTaskTitleContent)).setText(taskTitle);
        content.findViewById(R.id.btnDismissAlert).setOnClickListener(v -> dismiss());

        dialog = new AlertDialog.Builder(context).setView(content).setCancelable(false).create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
        speaker = new DeadlineSpeaker(context, null);
    }

    static DeadlineAlertDialog show(Context context, String taskTitle) {
        DeadlineAlertDialog alert = new DeadlineAlertDialog(context, taskTitle);
        alert.dialog.show();
        alert.speaker.speak("Attention! Deadline is near for task: " + taskTitle);
        return alert;
    }

    /** Closes the dialog and stops the voice; safe to call more than once. */
    void dismiss() {
        speaker.shutdown();
        if (dialog.isShowing()) {
            dialog.dismiss();
        }
    }
}
