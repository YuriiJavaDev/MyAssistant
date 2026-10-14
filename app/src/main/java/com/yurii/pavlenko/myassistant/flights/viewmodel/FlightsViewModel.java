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
import com.yurii.pavlenko.myassistant.flights.logic.VarioSoundSettings;

/** State of the Flights screen: the instrument readings, the chosen units and the sound settings. */
public class FlightsViewModel extends AndroidViewModel {

    private final FlightRecorder recorder;
    private final FlightUnitsStore unitsStore;
    private final MutableLiveData<FlightUnits> units;
    private final MutableLiveData<VarioSoundSettings> sound;

    public FlightsViewModel(@NonNull Application application) {
        super(application);
        recorder = FlightRecorder.get(application);
        unitsStore = new FlightUnitsStore(application);
        units = new MutableLiveData<>(unitsStore.load());
        sound = new MutableLiveData<>(recorder.getSoundSettings());
    }

    public LiveData<FlightSnapshot> getSnapshot() {
        return recorder.getSnapshot();
    }

    public LiveData<FlightUnits> getUnits() {
        return units;
    }

    public LiveData<VarioSoundSettings> getSound() {
        return sound;
    }

    public void saveUnits(FlightUnits chosen) {
        unitsStore.save(chosen);
        units.setValue(chosen);
    }

    public void saveSound(VarioSoundSettings chosen) {
        recorder.setSoundSettings(chosen);
        sound.setValue(chosen);
    }

    /** Tells the recorder whether the screen is in front, which is when the instruments run. */
    public void setWatching(boolean watching) {
        recorder.setWatching(watching);
    }

    public void zeroAltitude() {
        recorder.zeroAltitude();
    }

    public void clearAltitudeZero() {
        recorder.clearAltitudeZero();
    }
}
