package com.yurii.pavlenko.myassistant.tasks.ui.handlers;

import android.content.Context;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.SpinnerAdapter;

import com.yurii.pavlenko.myassistant.R;
import com.yurii.pavlenko.myassistant.databinding.FragmentTasksBinding;
import com.yurii.pavlenko.myassistant.tasks.viewmodel.TaskFilterSorter;
import com.yurii.pavlenko.myassistant.tasks.viewmodel.TaskViewModel;

/**
 * Helper class to initialize and manage task filters and sorting spinners.
 */
public class TaskSpinnerHelper {

    /**
     * Sets up adapters and selection listeners for filter and sort spinners.
     */
    public static void setupSpinners(Context context, FragmentTasksBinding binding, TaskViewModel taskViewModel) {
        // Initialize filter spinner adapter using string array resource
        ArrayAdapter<CharSequence> filterAdapter = ArrayAdapter.createFromResource(
                context,
                R.array.filter_options,
                R.layout.item_spinner
        );
        filterAdapter.setDropDownViewResource(R.layout.item_spinner);
        binding.filterSpinner.setAdapter(filterAdapter);

        // Initialize sort spinner adapter using string array resource
        ArrayAdapter<CharSequence> sortAdapter = ArrayAdapter.createFromResource(
                context,
                R.array.sort_options,
                R.layout.item_spinner
        );
        sortAdapter.setDropDownViewResource(R.layout.item_spinner);
        binding.sortSpinner.setAdapter(sortAdapter);

        // Define shared selection listener for both spinners
        AdapterView.OnItemSelectedListener selectionListener = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // Get selected filter option safely with fallback
                String selectedFilter = binding.filterSpinner.getSelectedItem() != null
                        ? binding.filterSpinner.getSelectedItem().toString()
                        : TaskFilterSorter.FILTER_ALL;

                // Get selected sort option safely with fallback
                String selectedSort = binding.sortSpinner.getSelectedItem() != null
                        ? binding.sortSpinner.getSelectedItem().toString()
                        : "Alpha A-Z";

                // Update ViewModel states
                taskViewModel.setFilter(selectedFilter);
                taskViewModel.setSort(selectedSort);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing when selection is cleared
            }
        };

        // Attach listeners to spinners
        binding.filterSpinner.setOnItemSelectedListener(selectionListener);
        binding.sortSpinner.setOnItemSelectedListener(selectionListener);
    }
    /**
     * Selects a filter in the filter spinner; the selection listener then applies it to the ViewModel.
     */
    public static void selectFilter(FragmentTasksBinding binding, String filter) {
        SpinnerAdapter adapter = binding.filterSpinner.getAdapter();
        for (int i = 0; i < adapter.getCount(); i++) {
            if (filter.equals(String.valueOf(adapter.getItem(i)))) {
                binding.filterSpinner.setSelection(i);
                return;
            }
        }
    }
}