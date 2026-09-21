package com.yurii.pavlenko.myassistant.tasks.ui.handlers;

import android.content.Context;
import android.widget.Toast;

import androidx.fragment.app.FragmentManager;

import com.yurii.pavlenko.myassistant.databinding.FragmentTasksBinding;
import com.yurii.pavlenko.myassistant.tasks.ui.dialogs.DeleteConfirmationDialog;
import com.yurii.pavlenko.myassistant.tasks.ui.dialogs.TaskDialogFragment;
import com.yurii.pavlenko.myassistant.tasks.viewmodel.TaskViewModel;

public class TaskActionsHandler {

    public static void setupClickListeners(Context context, FragmentManager fragmentManager, FragmentTasksBinding binding, TaskViewModel taskViewModel) {

        binding.addButton.setOnClickListener(v -> {
            String initialText = binding.taskInput.getText() != null ? binding.taskInput.getText().toString().trim() : "";

            TaskDialogFragment.newInstance(initialText, (title, importance, deadline, remindSound, showTimestamps, customReminderDate) -> {
                taskViewModel.createNewTask(title, importance, deadline, remindSound, showTimestamps, customReminderDate);
                binding.taskInput.setText("");
            }).show(fragmentManager, "TaskDialogFragment");
        });

        binding.deleteCompletedButton.setOnClickListener(v -> {
            boolean hasCompleted = taskViewModel.hasCompletedTasks();
            DeleteConfirmationDialog.show(context, hasCompleted, () -> {
                taskViewModel.deleteCompletedTasks();
                Toast.makeText(context, "Completed tasks deleted", Toast.LENGTH_SHORT).show();
            });
        });

        binding.clearAllButton.setOnClickListener(v -> {
            boolean hasTasks = taskViewModel.hasTasks();
            DeleteConfirmationDialog.show(context, hasTasks, () -> {
                taskViewModel.clearAllTasks();
                Toast.makeText(context, "All tasks cleared", Toast.LENGTH_SHORT).show();
            });
        });
    }
}