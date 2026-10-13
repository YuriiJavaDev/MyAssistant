package com.yurii.pavlenko.myassistant.calculator.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.yurii.pavlenko.myassistant.calculator.data.CalculatorStore;
import com.yurii.pavlenko.myassistant.calculator.logic.CalculatorDisplay;
import com.yurii.pavlenko.myassistant.calculator.logic.CalculatorEngine;
import com.yurii.pavlenko.myassistant.calculator.logic.HistoryEntry;
import com.yurii.pavlenko.myassistant.calculator.logic.Key;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Keeps the calculator alive while the phone is turned between portrait and landscape, so that
 * an expression typed in one orientation can be finished in the other.
 */
public class CalculatorViewModel extends AndroidViewModel {

    private final CalculatorStore store;
    private final CalculatorEngine engine;
    private final MutableLiveData<CalculatorDisplay> display = new MutableLiveData<>();
    private boolean scientificShown;

    public CalculatorViewModel(@NonNull Application application) {
        super(application);
        store = new CalculatorStore(application);
        Random random = new Random();
        engine = new CalculatorEngine(store.loadAngleMode(), store.loadMemory(), random::nextDouble);
        display.setValue(engine.display());
    }

    public LiveData<CalculatorDisplay> getDisplay() {
        return display;
    }

    public void press(Key key) {
        engine.press(key);
        publish();
    }

    public void toggleAngleMode() {
        engine.toggleAngleMode();
        publish();
    }

    public void restore(HistoryEntry entry) {
        engine.restore(entry.getExpression());
        publish();
    }

    public List<HistoryEntry> history() {
        return new ArrayList<>(engine.getHistory());
    }

    /** Whether the portrait screen shows the scientific keys. */
    public boolean isScientificShown() {
        return scientificShown;
    }

    public void toggleScientific() {
        scientificShown = !scientificShown;
    }

    private void publish() {
        store.save(engine.getMemory(), engine.getAngleMode());
        display.setValue(engine.display());
    }
}
