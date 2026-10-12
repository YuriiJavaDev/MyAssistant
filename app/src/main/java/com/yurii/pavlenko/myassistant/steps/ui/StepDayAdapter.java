package com.yurii.pavlenko.myassistant.steps.ui;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.yurii.pavlenko.myassistant.databinding.ItemStepDayBinding;
import com.yurii.pavlenko.myassistant.steps.logic.DayTotals;
import com.yurii.pavlenko.myassistant.steps.logic.StepLengths;

import java.util.Collections;
import java.util.List;

/** Days of the chosen period; each bar is scaled to the best day. */
class StepDayAdapter extends RecyclerView.Adapter<StepDayAdapter.Holder> {

    private static final int BAR_MAX = 1000;

    private List<DayTotals> days = Collections.emptyList();
    private StepLengths lengths = StepLengths.defaults();
    private int bestSteps;

    void submit(List<DayTotals> newDays, StepLengths newLengths) {
        days = newDays;
        lengths = newLengths;
        bestSteps = 0;
        for (DayTotals day : days) {
            bestSteps = Math.max(bestSteps, day.totalSteps());
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Holder(ItemStepDayBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        DayTotals day = days.get(position);
        ItemStepDayBinding views = holder.views;
        views.dayDate.setText(StepFormat.date(day.getEpochDay()));
        views.daySteps.setText(StepFormat.steps(day.totalSteps()));
        views.dayBar.setProgress(bestSteps == 0 ? 0 : day.totalSteps() * BAR_MAX / bestSteps);
        views.dayDistance.setText(StepFormat.distance(views.getRoot().getContext(),
                lengths.distanceMeters(day.getWalkSteps(), day.getRunSteps())));
    }

    @Override
    public int getItemCount() {
        return days.size();
    }

    static class Holder extends RecyclerView.ViewHolder {
        final ItemStepDayBinding views;

        Holder(ItemStepDayBinding views) {
            super(views.getRoot());
            this.views = views;
        }
    }
}
