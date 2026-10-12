package com.yurii.pavlenko.myassistant.steps.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.yurii.pavlenko.myassistant.steps.data.StepRepository;
import com.yurii.pavlenko.myassistant.steps.logic.DayTotals;
import com.yurii.pavlenko.myassistant.steps.logic.PeriodSummarizer;
import com.yurii.pavlenko.myassistant.steps.logic.PeriodSummary;
import com.yurii.pavlenko.myassistant.steps.logic.StepLengths;

import java.time.LocalDate;
import java.util.List;

/** State of the Steps screen: today's totals, the chosen period and the step lengths. */
public class StepsViewModel extends AndroidViewModel {

    private static final int DEFAULT_PERIOD_DAYS = 7;

    private final StepRepository repository;
    private final MutableLiveData<Long> today = new MutableLiveData<>(LocalDate.now().toEpochDay());
    private final MutableLiveData<long[]> range = new MutableLiveData<>(lastDays(DEFAULT_PERIOD_DAYS));
    private final LiveData<DayTotals> todayTotals;
    private final LiveData<StepLengths> lengths;
    private final LiveData<List<DayTotals>> rangeDays;
    private final MediatorLiveData<PeriodStats> period = new MediatorLiveData<>();

    public StepsViewModel(@NonNull Application application) {
        super(application);
        repository = new StepRepository(application);
        todayTotals = Transformations.switchMap(today, repository::observeDay);
        lengths = repository.observeLengths();
        rangeDays = Transformations.switchMap(range, r -> repository.observeDays(r[0], r[1]));
        period.addSource(rangeDays, days -> updatePeriod());
        period.addSource(lengths, l -> updatePeriod());
    }

    private void updatePeriod() {
        List<DayTotals> days = rangeDays.getValue();
        StepLengths current = lengths.getValue();
        long[] r = range.getValue();
        if (days == null || current == null || r == null) {
            return;
        }
        PeriodSummary summary = PeriodSummarizer.summarize(days, r[0], r[1], current);
        period.setValue(new PeriodStats(r[0], r[1], summary, days));
    }

    public LiveData<DayTotals> getTodayTotals() {
        return todayTotals;
    }

    public LiveData<StepLengths> getLengths() {
        return lengths;
    }

    public LiveData<PeriodStats> getPeriod() {
        return period;
    }

    /** Follows the calendar when the screen stays open past midnight. */
    public void refreshToday() {
        long now = LocalDate.now().toEpochDay();
        if (!Long.valueOf(now).equals(today.getValue())) {
            today.setValue(now);
        }
    }

    public void selectRange(long fromEpochDay, long toEpochDay) {
        range.setValue(new long[]{fromEpochDay, toEpochDay});
    }

    public void selectLastDays(int days) {
        range.setValue(lastDays(days));
    }

    public void selectThisMonth() {
        LocalDate now = LocalDate.now();
        selectRange(now.withDayOfMonth(1).toEpochDay(), now.toEpochDay());
    }

    public void saveLengths(StepLengths newLengths) {
        repository.saveLengths(newLengths);
    }

    public void recordNow() {
        repository.recordNow();
    }

    private static long[] lastDays(int days) {
        long end = LocalDate.now().toEpochDay();
        return new long[]{end - days + 1, end};
    }
}
