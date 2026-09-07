package com.yurii.pavlenko.myassistant.tasks.ui;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.yurii.pavlenko.myassistant.R;
import com.yurii.pavlenko.myassistant.databinding.FragmentTasksBinding;
import com.yurii.pavlenko.myassistant.tasks.model.Task;
import com.yurii.pavlenko.myassistant.tasks.viewmodel.TaskViewModel;

public class TasksFragment extends Fragment {

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
        setupSpinners();
        setupActionButtonsListeners();
        observeViewModel();
    }

    private void setupRecyclerView() {
        binding.tasksRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        taskAdapter = new TaskAdapter(
                (task, isChecked) -> taskViewModel.updateTaskCompletion(task, isChecked),
                this::showEditTaskDialog
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

    private void setupSpinners() {
        String[] filterOptions = {"All Tasks", "Active", "Completed"};
        ArrayAdapter<String> filterAdapter = new ArrayAdapter<>(requireContext(), R.layout.item_spinner, filterOptions);
        filterAdapter.setDropDownViewResource(R.layout.item_spinner);
        binding.filterSpinner.setAdapter(filterAdapter);

        String[] sortOptions = {
                "Alpha A-Z",
                "Alpha Z-A",
                "Status",
                "Created",
                "Edited",
                "Completed",
                "Importance"
        };
        ArrayAdapter<String> sortAdapter = new ArrayAdapter<>(requireContext(), R.layout.item_spinner, sortOptions);
        sortAdapter.setDropDownViewResource(R.layout.item_spinner);
        binding.sortSpinner.setAdapter(sortAdapter);

        AdapterView.OnItemSelectedListener selectionListener = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedFilter = binding.filterSpinner.getSelectedItem() != null ? binding.filterSpinner.getSelectedItem().toString() : "All Tasks";
                String selectedSort = binding.sortSpinner.getSelectedItem() != null ? binding.sortSpinner.getSelectedItem().toString() : "Alpha A-Z";

                taskViewModel.setFilter(selectedFilter);
                taskViewModel.setSort(selectedSort);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        };

        binding.filterSpinner.setOnItemSelectedListener(selectionListener);
        binding.sortSpinner.setOnItemSelectedListener(selectionListener);
    }

    private void setupActionButtonsListeners() {
        binding.addButton.setOnClickListener(v -> {
            String initialText = binding.taskInput.getText() != null ? binding.taskInput.getText().toString().trim() : "";
            showAddTaskDialog(initialText);
        });

        binding.deleteCompletedButton.setOnClickListener(v -> {
            boolean hasCompleted = taskViewModel.hasCompletedTasks();
            showDeleteConfirmationDialog(hasCompleted, () -> {
                taskViewModel.deleteCompletedTasks();
                Toast.makeText(requireContext(), "Completed tasks deleted", Toast.LENGTH_SHORT).show();
            });
        });

        binding.clearAllButton.setOnClickListener(v -> {
            boolean hasTasks = taskViewModel.hasTasks();
            showDeleteConfirmationDialog(hasTasks, () -> {
                taskViewModel.clearAllTasks();
                Toast.makeText(requireContext(), "All tasks cleared", Toast.LENGTH_SHORT).show();
            });
        });
    }

    private void showAddTaskDialog(String initialText) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_task, null);

        EditText dialogTaskEditText = dialogView.findViewById(R.id.dialogTaskEditText);
        Spinner dialogImportanceSpinner = dialogView.findViewById(R.id.dialogImportanceSpinner);

        dialogTaskEditText.setText(initialText);
        dialogTaskEditText.setSelection(dialogTaskEditText.getText().length());

        String[] importanceOptions = {"Normal", "Important", "Urgent"};
        ArrayAdapter<String> importanceAdapter = new ArrayAdapter<>(requireContext(), R.layout.item_spinner, importanceOptions);
        importanceAdapter.setDropDownViewResource(R.layout.item_spinner);
        dialogImportanceSpinner.setAdapter(importanceAdapter);
        dialogImportanceSpinner.setSelection(0);

        new AlertDialog.Builder(requireContext())
                .setTitle("Add New Task")
                .setView(dialogView)
                .setPositiveButton("OK", (dialog, which) -> {
                    String description = dialogTaskEditText.getText().toString().trim();
                    String importance = dialogImportanceSpinner.getSelectedItem().toString();

                    if (!description.isEmpty()) {
                        taskViewModel.createNewTask(description, importance);
                        binding.taskInput.setText("");
                    } else {
                        Toast.makeText(requireContext(), "Please enter a task title", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .create()
                .show();
    }

    private void showEditTaskDialog(Task task) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_task, null);

        EditText dialogTaskEditText = dialogView.findViewById(R.id.dialogTaskEditText);
        Spinner dialogImportanceSpinner = dialogView.findViewById(R.id.dialogImportanceSpinner);

        dialogTaskEditText.setText(task.getTitle());
        dialogTaskEditText.setSelection(dialogTaskEditText.getText().length());

        String[] importanceOptions = {"Normal", "Important", "Urgent"};
        ArrayAdapter<String> importanceAdapter = new ArrayAdapter<>(requireContext(), R.layout.item_spinner, importanceOptions);
        importanceAdapter.setDropDownViewResource(R.layout.item_spinner);
        dialogImportanceSpinner.setAdapter(importanceAdapter);

        if (task.getImportance() != null) {
            for (int i = 0; i < importanceOptions.length; i++) {
                if (importanceOptions[i].equalsIgnoreCase(task.getImportance())) {
                    dialogImportanceSpinner.setSelection(i);
                    break;
                }
            }
        }

        new AlertDialog.Builder(requireContext())
                .setTitle("Edit task")
                .setView(dialogView)
                .setPositiveButton("Save", (dialog, which) -> {
                    String description = dialogTaskEditText.getText().toString().trim();
                    String importance = dialogImportanceSpinner.getSelectedItem().toString();

                    if (!description.isEmpty()) {
                        taskViewModel.updateTaskDetails(task, description, importance);
                        Toast.makeText(requireContext(), "Task updated", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(requireContext(), "Task description cannot be empty", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNeutralButton("Delete", (dialog, which) -> {
                    showDeleteConfirmationDialog(true, () -> {
                        taskViewModel.deleteTask(task);
                        Toast.makeText(requireContext(), "Task deleted", Toast.LENGTH_SHORT).show();
                    });
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .create()
                .show();
    }

    private void showDeleteConfirmationDialog(boolean canDelete, Runnable onConfirmed) {
        if (!canDelete) {
            Toast.makeText(requireContext(), "No objects found to delete!", Toast.LENGTH_SHORT).show();
            return;
        }

        new AlertDialog.Builder(requireContext())
                .setTitle("Delete Confirmation")
                .setMessage("Are you sure you want to delete this item? This action cannot be undone!")
                .setPositiveButton("Delete", (dialog, which) -> onConfirmed.run())
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}