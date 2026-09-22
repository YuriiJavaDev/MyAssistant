package com.yurii.pavlenko.myassistant.tasks.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.yurii.pavlenko.myassistant.databinding.FragmentTasksBinding;
import com.yurii.pavlenko.myassistant.tasks.database.CloudSettingsDialog;
import com.yurii.pavlenko.myassistant.tasks.database.CloudSyncManager;
import com.yurii.pavlenko.myassistant.tasks.model.Task;
import com.yurii.pavlenko.myassistant.tasks.ui.dialogs.TaskDialogFragment;
import com.yurii.pavlenko.myassistant.tasks.ui.handlers.TaskActionsHandler;
import com.yurii.pavlenko.myassistant.tasks.ui.handlers.TaskSpinnerHelper;
import com.yurii.pavlenko.myassistant.tasks.viewmodel.TaskViewModel;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class TasksFragment extends Fragment implements
        TaskDialogFragment.OnTaskSavedListener,
        TaskDialogFragment.OnTaskUpdatedListener,
        TaskDialogFragment.OnTaskDeletedListener {

    private FragmentTasksBinding binding;
    private TaskAdapter taskAdapter;
    private TaskViewModel taskViewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentTasksBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        taskViewModel = new ViewModelProvider(this).get(TaskViewModel.class);

        setupRecyclerView();
        TaskSpinnerHelper.setupSpinners(requireContext(), binding, taskViewModel);
        TaskActionsHandler.setupClickListeners(requireContext(), getChildFragmentManager(), binding, taskViewModel);
        observeViewModel();
    }

    private void setupRecyclerView() {
        binding.tasksRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        taskAdapter = new TaskAdapter(
                (task, isChecked) -> taskViewModel.updateTaskCompletion(task, isChecked),
                task -> TaskDialogFragment.newInstance(
                        task,
                        this,
                        this
                ).show(getChildFragmentManager(), "TaskDialogFragment")
        );
        binding.tasksRecyclerView.setAdapter(taskAdapter);
    }

    private void observeViewModel() {
        taskViewModel.getDisplayList().observe(getViewLifecycleOwner(), tasks -> {
            taskAdapter.setTasks(tasks);
        });

        taskViewModel.getStatistics().observe(getViewLifecycleOwner(), statsText -> {
            binding.statisticsTextView.setText(statsText);
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onTaskSaved(String title, String importance, LocalDate deadline, boolean remindSound, boolean showTimestamps, LocalDateTime customReminderDateTime) {
        taskViewModel.createNewTask(title, importance, deadline, remindSound, showTimestamps, customReminderDateTime);
    }

    @Override
    public void onTaskUpdated(Task task, String title, String importance, LocalDate deadline, boolean remindSound, boolean showTimestamps, LocalDateTime customReminderDateTime) {
        taskViewModel.updateTaskDetails(task, title, importance, deadline, remindSound, showTimestamps, customReminderDateTime);
    }

    @Override
    public void onTaskDeleted(Task task) {
        taskViewModel.deleteTask(task);
    }
}