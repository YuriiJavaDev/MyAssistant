package com.yurii.pavlenko.myassistant.tasks.ui.handlers;

import android.content.Context;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import com.yurii.pavlenko.myassistant.R;
import com.yurii.pavlenko.myassistant.databinding.FragmentTasksBinding;
import com.yurii.pavlenko.myassistant.tasks.viewmodel.TaskViewModel;

public class TaskSpinnerHelper {

    public static void setupSpinners(Context context, FragmentTasksBinding binding, TaskViewModel taskViewModel) {
        String[] filterOptions = {"All Tasks", "Active", "Completed"};
        ArrayAdapter<String> filterAdapter = new ArrayAdapter<>(context, R.layout.item_spinner, filterOptions);
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
        ArrayAdapter<String> sortAdapter = new ArrayAdapter<>(context, R.layout.item_spinner, sortOptions);
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
}