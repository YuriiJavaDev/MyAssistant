package com.yurii.pavlenko.myassistant.flights.ui;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.yurii.pavlenko.myassistant.R;
import com.yurii.pavlenko.myassistant.databinding.ItemFlightBinding;
import com.yurii.pavlenko.myassistant.flights.data.FlightUnits;
import com.yurii.pavlenko.myassistant.flights.logic.FlightFormat;
import com.yurii.pavlenko.myassistant.flights.logic.FlightTime;
import com.yurii.pavlenko.myassistant.flights.model.FlightRecord;

import java.text.DateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.function.Consumer;

/** The recorded flights, newest first. */
class FlightAdapter extends RecyclerView.Adapter<FlightAdapter.Holder> {

    private final Consumer<FlightRecord> onClick;
    private final DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT);

    private List<FlightRecord> flights = Collections.emptyList();
    private final FlightUnits units;

    FlightAdapter(FlightUnits units, Consumer<FlightRecord> onClick) {
        this.units = units;
        this.onClick = onClick;
    }

    void submit(List<FlightRecord> newFlights) {
        flights = newFlights;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Holder(ItemFlightBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        FlightRecord flight = flights.get(position);
        ItemFlightBinding views = holder.views;
        views.flightDate.setText(dateFormat.format(new Date(flight.getStartMillis())));
        views.flightDuration.setText(FlightTime.format(flight.getDurationMillis()));
        views.flightDetails.setText(views.getRoot().getContext().getString(R.string.flights_journal_line,
                FlightFormat.distance(flight.getDistanceMetres(), units.getSpeed()),
                FlightFormat.speed(flight.getMaxSpeed(), units.getSpeed()),
                FlightFormat.altitude(flight.getMaxAltitude(), units.getAltitude())));
        views.getRoot().setOnClickListener(v -> onClick.accept(flight));
    }

    @Override
    public int getItemCount() {
        return flights.size();
    }

    static class Holder extends RecyclerView.ViewHolder {
        final ItemFlightBinding views;

        Holder(ItemFlightBinding views) {
            super(views.getRoot());
            this.views = views;
        }
    }
}
