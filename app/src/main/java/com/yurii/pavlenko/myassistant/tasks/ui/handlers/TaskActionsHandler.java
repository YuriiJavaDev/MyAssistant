package com.yurii.pavlenko.myassistant.tasks.ui.handlers;

import android.content.Context;
import android.widget.Toast;

import com.yurii.pavlenko.myassistant.databinding.FragmentTasksBinding;
import com.yurii.pavlenko.myassistant.tasks.ui.dialogs.AddTaskDialog;
import com.yurii.pavlenko.myassistant.tasks.ui.dialogs.DeleteConfirmationDialog;
import com.yurii.pavlenko.myassistant.tasks.viewmodel.TaskViewModel;

public class TaskActionsHandler {

    public static void setupClickListeners(Context context, FragmentTasksBinding binding, TaskViewModel taskViewModel) {
        binding.addButton.setOnClickListener(v -> {
            String initialText = binding.taskInput.getText() != null ? binding.taskInput.getText().toString().trim() : "";
            AddTaskDialog.show(context, initialText, (title, importance, deadline, remindSound) -> {
                taskViewModel.createNewTask(title, importance, deadline, remindSound);
                binding.taskInput.setText("");
            });
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