package com.yurii.pavlenko.myassistant.tasks.ui.handlers;

import android.content.Context;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import com.yurii.pavlenko.myassistant.R;
import com.yurii.pavlenko.myassistant.databinding.FragmentTasksBinding;
import com.yurii.pavlenko.myassistant.tasks.logic.TaskFilter;
import com.yurii.pavlenko.myassistant.tasks.logic.TaskSort;
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
                taskViewModel.setFilter(TaskFilter.fromPosition(binding.filterSpinner.getSelectedItemPosition()));
                taskViewModel.setSort(TaskSort.fromPosition(binding.sortSpinner.getSelectedItemPosition()));
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
    public static void selectFilter(FragmentTasksBinding binding, TaskFilter filter) {
        binding.filterSpinner.setSelection(filter.ordinal());
    }
}
