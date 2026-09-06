package com.yurii.pavlenko.myassistant.fragments;

/**
 * TasksFragment implementation managing task listing, filtering, sorting, and batch operations.
 * @date 2026-09-06
 */
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
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
import java.util.Collections;
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
        // Step 1: Inflate layout using View Binding
        binding = FragmentTasksBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Step 2: Initialize lists and RecyclerView adapter
        initDataStructures();
        setupRecyclerView();

        // Step 3: Load initial mock tasks
        loadInitialData();

        // Step 4: Setup Spinners for filtering and sorting
        setupSpinners();

        // Step 5: Setup action button listeners (Add, Delete, Clear)
        setupActionButtonsListeners();
    }

    private void initDataStructures() {
        // Step 2.1: Initialize master and display collections
        allTasks = new ArrayList<>();
        displayList = new ArrayList<>();
    }

    private void setupRecyclerView() {
        // Step 2.2: Configure RecyclerView layout manager and adapter
        binding.tasksRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        taskAdapter = new TaskAdapter((task, isChecked) -> {
            task.setCompleted(isChecked);
            task.setCompletedAt(isChecked ? LocalDateTime.now() : null);
            task.setUpdatedAt(LocalDateTime.now());
            applyFilterAndSort();
        });
        binding.tasksRecyclerView.setAdapter(taskAdapter);
    }

    private void loadInitialData() {
        // Step 3.1: Populate master list with sample data matching Swing project
        allTasks.add(new Task(UUID.randomUUID(), "выпить чашечку кофе", false, LocalDateTime.now().minusHours(3), null, null, "Normal"));
        allTasks.add(new Task(UUID.randomUUID(), "Почитать учебник по Java", false, LocalDateTime.now().minusHours(2), null, null, "Important"));
        allTasks.add(new Task(UUID.randomUUID(), "поспать перед обедом и после него", false, LocalDateTime.now().minusHours(1), null, null, "Urgent"));

        applyFilterAndSort();
    }

    private void setupSpinners() {
        // Step 4.1: Setup Filter Spinner adapter with custom layout
        String[] filterOptions = {"All Tasks", "Active", "Completed"};
        ArrayAdapter<String> filterAdapter = new ArrayAdapter<>(requireContext(), R.layout.item_spinner, filterOptions);
        filterAdapter.setDropDownViewResource(R.layout.item_spinner);
        binding.filterSpinner.setAdapter(filterAdapter);

        // Step 4.2: Setup Sort Spinner adapter with custom layout
        String[] sortOptions = {
                "Alpha A-Z",
                "Alpha Z-A",
                "Status",
                "Created",
                "Modified",
                "Completed",
                "Importance"
        };
        ArrayAdapter<String> sortAdapter = new ArrayAdapter<>(requireContext(), R.layout.item_spinner, sortOptions);
        sortAdapter.setDropDownViewResource(R.layout.item_spinner);
        binding.sortSpinner.setAdapter(sortAdapter);

        // Step 4.3: Add selection listeners for dynamic updating
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
        // Step 4.4: Retrieve selected filter and sort criteria
        String selectedFilter = binding.filterSpinner.getSelectedItem() != null ? binding.filterSpinner.getSelectedItem().toString() : "All Tasks";
        String selectedSort = binding.sortSpinner.getSelectedItem() != null ? binding.sortSpinner.getSelectedItem().toString() : "Alphabetical (A-Z)";

        // Step 4.5: Apply filtering logic
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

        // Step 4.6: Apply sorting logic based on Swing specifications
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
            case "Modified":
                filtered.sort(Comparator.comparing(Task::getUpdatedAt, Comparator.nullsLast(Comparator.naturalOrder())).reversed());
                break;
            case "Completed":
                filtered.sort(Comparator.comparing(Task::getCompletedAt, Comparator.nullsLast(Comparator.naturalOrder())).reversed());
                break;
            case "Importance":
                filtered.sort(Comparator.comparingInt(t -> getImportanceWeight(t.getImportance())));
                break;
        }

        // Step 4.7: Update display list and refresh adapter and statistics
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
        // Step 5.1: Setup Add button listener
        binding.addButton.setOnClickListener(v -> {
            String title = binding.taskInput.getText() != null ? binding.taskInput.getText().toString().trim() : "";
            if (title.isEmpty()) {
                Toast.makeText(requireContext(), "Please enter a task title", Toast.LENGTH_SHORT).show();
                return;
            }
            Task newTask = new Task(UUID.randomUUID(), title, false, LocalDateTime.now(), null, null, "Normal");
            allTasks.add(0, newTask);
            binding.taskInput.setText("");
            applyFilterAndSort();
        });

        // Step 5.2: Setup Delete Completed button listener
        binding.deleteCompletedButton.setOnClickListener(v -> {
            allTasks.removeIf(Task::isCompleted);
            applyFilterAndSort();
            Toast.makeText(requireContext(), "Completed tasks deleted", Toast.LENGTH_SHORT).show();
        });

        // Step 5.3: Setup Clear All button listener
        binding.clearAllButton.setOnClickListener(v -> {
            allTasks.clear();
            applyFilterAndSort();
            Toast.makeText(requireContext(), "All tasks cleared", Toast.LENGTH_SHORT).show();
        });
    }

    private void updateStatistics() {
        // Step 6.1: Calculate total, completed, and remaining tasks based on master list
        int total = allTasks.size();
        int completed = (int) allTasks.stream().filter(Task::isCompleted).count();
        int left = total - completed;
        int progress = total > 0 ? (completed * 100) / total : 0;

        // Step 6.2: Display formatted statistics text
        String statsText = String.format("Total: %d  Completed: %d  Left: %d  Progress: %d%%", total, completed, left, progress);
        binding.statisticsTextView.setText(statsText);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Step 7: Clear view binding reference to prevent memory leaks
        binding = null;
    }
}