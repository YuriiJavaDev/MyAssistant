package com.yurii.pavlenko.myassistant.fragments;

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
import androidx.recyclerview.widget.LinearLayoutManager;
import com.yurii.pavlenko.myassistant.R;
import com.yurii.pavlenko.myassistant.adapter.TaskAdapter;
import com.yurii.pavlenko.myassistant.databinding.FragmentTasksBinding;
import com.yurii.pavlenko.myassistant.model.tasks.Task;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class TasksFragment extends Fragment {

    private FragmentTasksBinding binding;
    private TaskAdapter taskAdapter;
    private List<Task> allTasks;
    private List<Task> displayList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentTasksBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initDataStructures();
        setupRecyclerView();
        loadInitialData();
        setupSpinners();
        setupActionButtonsListeners();
    }

    private void initDataStructures() {
        allTasks = new ArrayList<>();
        displayList = new ArrayList<>();
    }

    private void setupRecyclerView() {
        binding.tasksRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        taskAdapter = new TaskAdapter(
                (task, isChecked) -> {
                    task.setCompleted(isChecked);
                    task.setCompletedAt(isChecked ? LocalDateTime.now() : null);
                    task.setUpdatedAt(LocalDateTime.now());
                    applyFilterAndSort();
                },
                this::showEditTaskDialog // Open edit dialog when task item is clicked
        );
        binding.tasksRecyclerView.setAdapter(taskAdapter);
    }

    private void loadInitialData() {
        allTasks.add(new Task(UUID.randomUUID(), "выпить чашечку кофе", false, LocalDateTime.now().minusHours(3), null, null, "Normal"));
        allTasks.add(new Task(UUID.randomUUID(), "Почитать учебник по Java", false, LocalDateTime.now().minusHours(2), null, null, "Important"));
        allTasks.add(new Task(UUID.randomUUID(), "поспать перед обедом и после него", false, LocalDateTime.now().minusHours(1), null, null, "Urgent"));
        applyFilterAndSort();
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
                applyFilterAndSort();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        };

        binding.filterSpinner.setOnItemSelectedListener(selectionListener);
        binding.sortSpinner.setOnItemSelectedListener(selectionListener);
    }

    private void applyFilterAndSort() {
        String selectedFilter = binding.filterSpinner.getSelectedItem() != null ? binding.filterSpinner.getSelectedItem().toString() : "All Tasks";
        String selectedSort = binding.sortSpinner.getSelectedItem() != null ? binding.sortSpinner.getSelectedItem().toString() : "Alpha A-Z";

        List<Task> filtered;
        switch (selectedFilter) {
            case "Active":
                filtered = allTasks.stream().filter(t -> !t.isCompleted()).collect(Collectors.toList());
                break;
            case "Completed":
                filtered = allTasks.stream().filter(Task::isCompleted).collect(Collectors.toList());
                break;
            case "All Tasks":
            default:
                filtered = new ArrayList<>(allTasks);
                break;
        }

        switch (selectedSort) {
            case "Alpha A-Z":
                filtered.sort(Comparator.comparing(Task::getTitle, String.CASE_INSENSITIVE_ORDER));
                break;
            case "Alpha Z-A":
                filtered.sort(Comparator.comparing(Task::getTitle, String.CASE_INSENSITIVE_ORDER).reversed());
                break;
            case "Status":
                filtered.sort(Comparator.comparing(Task::isCompleted));
                break;
            case "Created":
                filtered.sort(Comparator.comparing(Task::getCreatedAt, Comparator.nullsLast(Comparator.naturalOrder())).reversed());
                break;
            case "Edited":
                filtered.sort(Comparator.comparing(Task::getUpdatedAt, Comparator.nullsLast(Comparator.naturalOrder())).reversed());
                break;
            case "Completed":
                filtered.sort(Comparator.comparing(Task::getCompletedAt, Comparator.nullsLast(Comparator.naturalOrder())).reversed());
                break;
            case "Importance":
                filtered.sort(Comparator.comparingInt(t -> getImportanceWeight(t.getImportance())));
                break;
        }

        displayList = filtered;
        taskAdapter.setTasks(displayList);
        updateStatistics();
    }

    private int getImportanceWeight(String importance) {
        if (importance == null) return 3;
        switch (importance.toLowerCase()) {
            case "urgent": return 1;
            case "important": return 2;
            case "normal": default: return 3;
        }
    }

    private void setupActionButtonsListeners() {
        binding.addButton.setOnClickListener(v -> {
            String initialText = binding.taskInput.getText() != null ? binding.taskInput.getText().toString().trim() : "";
            showAddTaskDialog(initialText);
        });

        binding.deleteCompletedButton.setOnClickListener(v -> {
            boolean hasCompleted = false;
            for (Task task : allTasks) {
                if (task.isCompleted()) {
                    hasCompleted = true;
                    break;
                }
            }

            showDeleteConfirmationDialog(hasCompleted, () -> {
                allTasks.removeIf(Task::isCompleted);
                applyFilterAndSort();
                Toast.makeText(requireContext(), "Completed tasks deleted", Toast.LENGTH_SHORT).show();
            });
        });

        binding.clearAllButton.setOnClickListener(v -> {
            boolean hasTasks = !allTasks.isEmpty();

            showDeleteConfirmationDialog(hasTasks, () -> {
                allTasks.clear();
                applyFilterAndSort();
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
        dialogImportanceSpinner.setSelection(0); // Default to Normal

        new AlertDialog.Builder(requireContext())
                .setTitle("Add New Task")
                .setView(dialogView)
                .setPositiveButton("OK", (dialog, which) -> {
                    String description = dialogTaskEditText.getText().toString().trim();
                    String importance = dialogImportanceSpinner.getSelectedItem().toString();

                    if (!description.isEmpty()) {
                        createNewTask(description, importance);
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
                        task.setTitle(description);
                        task.setImportance(importance);
                        task.setUpdatedAt(LocalDateTime.now());
                        applyFilterAndSort();
                        Toast.makeText(requireContext(), "Task updated", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(requireContext(), "Task description cannot be empty", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNeutralButton("Delete", (dialog, which) -> {
                    showDeleteConfirmationDialog(true,() -> {
                        allTasks.remove(task);
                        applyFilterAndSort();
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

    private void createNewTask(String title, String importance) {
        Task newTask = new Task(UUID.randomUUID(), title, false, LocalDateTime.now(), null, null, importance);
        allTasks.add(0, newTask);
        applyFilterAndSort();
    }

    private void updateStatistics() {
        int total = allTasks.size();
        int completed = (int) allTasks.stream().filter(Task::isCompleted).count();
        int left = total - completed;
        int progress = total > 0 ? (completed * 100) / total : 0;

        String statsText = String.format("Total: %d  Completed: %d  Left: %d  Progress: %d%%", total, completed, left, progress);
        binding.statisticsTextView.setText(statsText);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}