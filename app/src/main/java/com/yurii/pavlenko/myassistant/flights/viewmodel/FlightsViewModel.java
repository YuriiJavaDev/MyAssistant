package com.yurii.pavlenko.myassistant.flights.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.yurii.pavlenko.myassistant.flights.data.FlightRecorder;
import com.yurii.pavlenko.myassistant.flights.data.FlightUnits;
import com.yurii.pavlenko.myassistant.flights.data.FlightUnitsStore;
import com.yurii.pavlenko.myassistant.flights.logic.FlightSnapshot;

/** State of the Flights screen: the instrument readings and the chosen units. */
public class FlightsViewModel extends AndroidViewModel {

    private final FlightRecorder recorder;
    private final FlightUnitsStore unitsStore;
    private final MutableLiveData<FlightUnits> units;

    public FlightsViewModel(@NonNull Application application) {
        super(application);
        recorder = FlightRecorder.get(application);
        unitsStore = new FlightUnitsStore(application);
        units = new MutableLiveData<>(unitsStore.load());
    }

    public LiveData<FlightSnapshot> getSnapshot() {
        return recorder.getSnapshot();
    }

    public LiveData<FlightUnits> getUnits() {
        return units;
    }

    public void saveUnits(FlightUnits chosen) {
        unitsStore.save(chosen);
        units.setValue(chosen);
    }

    public boolean hasCompass() {
        return recorder.hasCompass();
    }

    /** Tells the recorder whether the screen is in front, which is when the compass and load meter run. */
    public void setWatching(boolean watching) {
        recorder.setWatching(watching);
    }
}
