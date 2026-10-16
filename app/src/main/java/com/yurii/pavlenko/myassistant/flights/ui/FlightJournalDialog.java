package com.yurii.pavlenko.myassistant.flights.ui;

import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.yurii.pavlenko.myassistant.R;
import com.yurii.pavlenko.myassistant.databinding.DialogFlightJournalBinding;
import com.yurii.pavlenko.myassistant.flights.model.FlightRecord;
import com.yurii.pavlenko.myassistant.flights.viewmodel.FlightsViewModel;

/** The list of recorded flights; a tap offers to open or share the report, or to delete the flight. */
public class FlightJournalDialog extends DialogFragment {

    private FlightsViewModel viewModel;
    private DialogFlightJournalBinding binding;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        binding = DialogFlightJournalBinding.inflate(getLayoutInflater());
        viewModel = new ViewModelProvider(requireParentFragment()).get(FlightsViewModel.class);

        FlightAdapter adapter = new FlightAdapter(viewModel.getUnits().getValue(), this::showActions);
        binding.journalList.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.journalList.setAdapter(adapter);
        viewModel.getFlights().observe(this, flights -> {
            adapter.submit(flights);
            binding.journalEmpty.setVisibility(flights.isEmpty() ? View.VISIBLE : View.GONE);
        });

        AlertDialog dialog = new AlertDialog.Builder(requireContext()).setView(binding.getRoot()).create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
        return dialog;
    }

    private void showActions(FlightRecord flight) {
        if (!flight.isFinished()) {
            Toast.makeText(requireContext(), R.string.flights_journal_in_progress, Toast.LENGTH_SHORT).show();
            return;
        }
        String[] actions = {getString(R.string.flights_report_open), getString(R.string.flights_report_share),
                getString(R.string.flights_report_delete)};
        new AlertDialog.Builder(requireContext())
                .setItems(actions, (dialog, which) -> {
                    if (which == 0) {
                        withReport(flight, true);
                    } else if (which == 1) {
                        withReport(flight, false);
                    } else {
                        confirmDelete(flight);
                    }
                })
                .show();
    }

    private void withReport(FlightRecord flight, boolean open) {
        viewModel.loadReport(flight.getId(), pdf -> {
            if (!isAdded()) {
                return;
            }
            if (pdf == null) {
                Toast.makeText(requireContext(), R.string.flights_report_failed, Toast.LENGTH_LONG).show();
            } else if (open) {
                FlightReportFiles.view(requireContext(), flight, pdf);
            } else {
                FlightReportFiles.share(requireContext(), flight, pdf);
            }
        });
    }

    private void confirmDelete(FlightRecord flight) {
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.flights_delete_title)
                .setMessage(R.string.flights_delete_message)
                .setPositiveButton(R.string.flights_delete_confirm, (dialog, which) -> viewModel.deleteFlight(flight.getId()))
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
