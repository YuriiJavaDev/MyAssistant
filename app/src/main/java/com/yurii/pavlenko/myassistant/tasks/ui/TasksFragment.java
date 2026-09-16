package com.yurii.pavlenko.myassistant.tasks.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import android.widget.Toast;

import com.yurii.pavlenko.myassistant.tasks.database.CloudSettingsDialog;
import com.yurii.pavlenko.myassistant.tasks.database.CloudSyncManager;
import com.yurii.pavlenko.myassistant.databinding.FragmentTasksBinding;
import com.yurii.pavlenko.myassistant.tasks.database.BackupMenuHandler;
import com.yurii.pavlenko.myassistant.tasks.ui.dialogs.TaskDialog;
import com.yurii.pavlenko.myassistant.tasks.ui.handlers.TaskActionsHandler;
import com.yurii.pavlenko.myassistant.tasks.ui.handlers.TaskSpinnerHelper;
import com.yurii.pavlenko.myassistant.tasks.viewmodel.TaskViewModel;

public class TasksFragment extends Fragment implements BackupMenuHandler.OnMenuActionListener {

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
        TaskActionsHandler.setupClickListeners(requireContext(), binding, taskViewModel);
        observeViewModel();
    }

    private void setupRecyclerView() {
        binding.tasksRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        taskAdapter = new TaskAdapter(
                (task, isChecked) -> taskViewModel.updateTaskCompletion(task, isChecked),
                task -> TaskDialog.showEdit(
                        requireContext(),
                        task,
                        (t, title, importance, deadline, remindSound, showTimestamps) -> taskViewModel.updateTaskDetails(t, title, importance, deadline, remindSound, showTimestamps),
                        t -> taskViewModel.deleteTask(t)
                )
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
    public void onExportLocal() {
        // Локальний експорт (якщо вже реалізовано через DatabaseBackupManager)
    }

    @Override
    public void onImportLocal() {
        // Локальний імпорт
    }

    @Override
    public void onCloudSettings() {
        // Відкриття діалогу налаштувань хмари
        new CloudSettingsDialog().show(getParentFragmentManager(), "CloudSettingsDialog");
    }

    @Override
    public void onCloudExport() {
        CloudSyncManager syncManager = new CloudSyncManager(requireContext());
        Toast.makeText(requireContext(), "Exporting database to cloud...", Toast.LENGTH_SHORT).show();

        syncManager.uploadDatabase(new CloudSyncManager.SyncCallback() {
            @Override
            public void onSuccess(String message) {
                requireActivity().runOnUiThread(() ->
                        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                );
            }

            @Override
            public void onError(String error) {
                requireActivity().runOnUiThread(() ->
                        Toast.makeText(requireContext(), error, Toast.LENGTH_LONG).show()
                );
            }
        });
    }

    @Override
    public void onCloudImport() {
        CloudSyncManager syncManager = new CloudSyncManager(requireContext());
        Toast.makeText(requireContext(), "Importing database from cloud...", Toast.LENGTH_SHORT).show();

        syncManager.downloadDatabase(new CloudSyncManager.SyncCallback() {
            @Override
            public void onSuccess(String message) {
                requireActivity().runOnUiThread(() -> {
                    Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
                    // За потреби тут можна оновити дані у ViewModel, щоб список завдань на екрані оновився після імпорту
                });
            }

            @Override
            public void onError(String error) {
                requireActivity().runOnUiThread(() ->
                        Toast.makeText(requireContext(), error, Toast.LENGTH_LONG).show()
                );
            }
        });
    }
}