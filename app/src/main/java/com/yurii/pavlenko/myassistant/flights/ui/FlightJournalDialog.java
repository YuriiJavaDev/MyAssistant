package com.yurii.pavlenko.myassistant.flights.ui;

import android.app.Dialog;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.yurii.pavlenko.myassistant.R;
import com.yurii.pavlenko.myassistant.databinding.DialogFlightJournalBinding;
import com.yurii.pavlenko.myassistant.flights.logic.FlightReportName;
import com.yurii.pavlenko.myassistant.flights.model.FlightRecord;
import com.yurii.pavlenko.myassistant.flights.viewmodel.FlightsViewModel;

/** The list of recorded flights; a tap offers to open or share the report, or to delete the flight. */
public class FlightJournalDialog extends DialogFragment {

    private static final long NO_FLIGHT = -1;
    private static final String STATE_PENDING_FLIGHT = "pending_flight";
    private static final String PDF_MIME = "application/pdf";

    private FlightsViewModel viewModel;
    private DialogFlightJournalBinding binding;
    private long pendingFlightId = NO_FLIGHT;

    private final ActivityResultLauncher<String> saveLauncher = registerForActivityResult(
            new ActivityResultContracts.CreateDocument(PDF_MIME), this::onSaveTargetChosen);

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            pendingFlightId = savedInstanceState.getLong(STATE_PENDING_FLIGHT, NO_FLIGHT);
        }
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
                .setPositiveButton(R.string.flights_delete_save_first, (dialog, which) -> chooseWhereToSave(flight))
                .setNegativeButton(R.string.flights_delete_without_saving, (dialog, which) -> viewModel.deleteFlight(flight.getId()))
                .setNeutralButton(android.R.string.cancel, null)
                .show();
    }

    /** The system file picker offers the phone, a flash drive and cloud drives such as Google Drive. */
    private void chooseWhereToSave(FlightRecord flight) {
        pendingFlightId = flight.getId();
        saveLauncher.launch(FlightReportName.of(flight.getStartMillis()));
    }

    private void onSaveTargetChosen(Uri target) {
        long flightId = pendingFlightId;
        pendingFlightId = NO_FLIGHT;
        if (target == null || flightId == NO_FLIGHT) {
            return;
        }
        Context context = requireContext().getApplicationContext();
        viewModel.saveReportThenDelete(flightId, target, saved ->
                Toast.makeText(context, saved ? R.string.flights_delete_saved : R.string.flights_delete_save_failed,
                        Toast.LENGTH_LONG).show());
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putLong(STATE_PENDING_FLIGHT, pendingFlightId);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
