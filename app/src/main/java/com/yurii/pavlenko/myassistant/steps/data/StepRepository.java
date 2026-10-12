package com.yurii.pavlenko.myassistant.steps.data;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.Transformations;

import com.yurii.pavlenko.myassistant.steps.database.StepDao;
import com.yurii.pavlenko.myassistant.steps.database.StepDatabase;
import com.yurii.pavlenko.myassistant.steps.logic.DayTotals;
import com.yurii.pavlenko.myassistant.steps.logic.StepLengths;
import com.yurii.pavlenko.myassistant.steps.model.StepSettings;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class StepRepository {

    private static final ExecutorService IO = Executors.newSingleThreadExecutor();

    private final StepDao dao;
    private final StepRecorder recorder;

    public StepRepository(Context context) {
        dao = StepDatabase.getInstance(context).stepDao();
        recorder = new StepRecorder(context);
    }

    public LiveData<DayTotals> observeDay(long epochDay) {
        return dao.observeDay(epochDay);
    }

    public LiveData<List<DayTotals>> observeDays(long fromEpochDay, long toEpochDay) {
        return dao.observeDays(fromEpochDay, toEpochDay);
    }

    public LiveData<StepLengths> observeLengths() {
        return Transformations.map(dao.observeSettings(),
                settings -> settings == null ? StepLengths.defaults() : settings.toLengths());
    }

    public void saveLengths(StepLengths lengths) {
        IO.execute(() -> dao.saveSettings(StepSettings.from(lengths)));
    }

    public void recordNow() {
        IO.execute(recorder::recordNow);
    }
}
