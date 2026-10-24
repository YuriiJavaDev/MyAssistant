package com.yurii.pavlenko.myassistant.weather.viewmodel;

import android.app.Application;
import android.location.Location;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.yurii.pavlenko.myassistant.weather.data.CurrentWeather;
import com.yurii.pavlenko.myassistant.weather.data.ForecastService;
import com.yurii.pavlenko.myassistant.weather.data.GeocodingService;
import com.yurii.pavlenko.myassistant.weather.data.Place;
import com.yurii.pavlenko.myassistant.weather.data.PlaceChoice;
import com.yurii.pavlenko.myassistant.weather.data.WeatherUnits;
import com.yurii.pavlenko.myassistant.weather.data.WeatherUnitsStore;
import com.yurii.pavlenko.myassistant.weather.location.DeviceLocation;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/** State of the weather screen: the phone's city by default, or any city the user searches for. */
public class WeatherViewModel extends AndroidViewModel {

    private static final long STALE_MILLIS = TimeUnit.MINUTES.toMillis(15);

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final MutableLiveData<WeatherState> state = new MutableLiveData<>(WeatherState.loading());
    private final MutableLiveData<List<Place>> matches = new MutableLiveData<>();
    private final WeatherUnitsStore unitsStore;
    private final MutableLiveData<WeatherUnits> units;

    private volatile Place shownPlace;
    private volatile boolean followsPhone = true;
    private volatile long loadedAt;
    private volatile int request;

    public WeatherViewModel(@NonNull Application application) {
        super(application);
        unitsStore = new WeatherUnitsStore(application);
        units = new MutableLiveData<>(unitsStore.load());
    }

    public LiveData<WeatherUnits> getUnits() {
        return units;
    }

    public void saveUnits(WeatherUnits chosen) {
        unitsStore.save(chosen);
        units.setValue(chosen);
    }

    public LiveData<WeatherState> getState() {
        return state;
    }

    /** Cities found for a search; the screen asks the user to pick when there are several. */
    public LiveData<List<Place>> getMatches() {
        return matches;
    }

    public void clearMatches() {
        matches.setValue(null);
    }

    /** Loads the weather where the phone is. */
    public void showPhoneLocation() {
        followsPhone = true;
        if (!DeviceLocation.hasPermission(getApplication())) {
            state.setValue(WeatherState.needsLocationPermission());
            return;
        }
        int id = begin();
        executor.execute(() -> {
            Location location = DeviceLocation.find(getApplication());
            if (location == null) {
                publish(id, WeatherState.error(WeatherState.Error.NO_LOCATION));
                return;
            }
            Place place = GeocodingService.describe(getApplication(), location.getLatitude(), location.getLongitude());
            loadWeather(id, place);
        });
    }

    public void search(String query) {
        String text = query.trim();
        if (text.isEmpty()) {
            return;
        }
        WeatherState before = state.getValue();
        int id = begin();
        executor.execute(() -> {
            try {
                List<Place> found = GeocodingService.search(getApplication(), text);
                if (id != request) {
                    return;
                }
                Place obvious = PlaceChoice.obvious(found);
                if (found.isEmpty()) {
                    publish(id, WeatherState.error(WeatherState.Error.CITY_NOT_FOUND, text));
                } else if (obvious != null) {
                    followsPhone = false;
                    loadWeather(id, obvious);
                } else {
                    matches.postValue(found);
                    publish(id, backTo(before));
                }
            } catch (IOException e) {
                publish(id, WeatherState.error(WeatherState.Error.CITY_LIST_UNREADABLE));
            }
        });
    }

    public void showPlace(Place place) {
        followsPhone = false;
        int id = begin();
        executor.execute(() -> loadWeather(id, place));
    }

    /** Called when the screen comes back: reloads when the shown weather is old or nothing is shown yet. */
    public void refreshIfStale() {
        WeatherState current = state.getValue();
        boolean hasContent = current != null && current.getStatus() == WeatherState.Status.CONTENT;
        if (!hasContent) {
            if (current == null || current.getStatus() != WeatherState.Status.LOADING) {
                showPhoneLocation();
            }
        } else if (System.currentTimeMillis() - loadedAt > STALE_MILLIS) {
            if (followsPhone) {
                showPhoneLocation();
            } else {
                showPlace(shownPlace);
            }
        }
    }

    private int begin() {
        state.setValue(WeatherState.loading());
        return ++request;
    }

    private void loadWeather(int id, Place place) {
        try {
            CurrentWeather weather = ForecastService.load(place);
            if (id == request) {
                shownPlace = place;
                loadedAt = System.currentTimeMillis();
            }
            publish(id, WeatherState.content(place, weather));
        } catch (IOException e) {
            publish(id, WeatherState.error(WeatherState.Error.NO_NETWORK));
        }
    }

    /** Ignores results of requests that a newer one has replaced. */
    private void publish(int id, WeatherState result) {
        if (id == request) {
            state.postValue(result);
        }
    }

    /** While the user picks among several cities, the screen shows again what it showed before the search. */
    private static WeatherState backTo(WeatherState before) {
        boolean hasNothingToShow = before == null || before.getStatus() == WeatherState.Status.LOADING;
        return hasNothingToShow ? WeatherState.error(WeatherState.Error.CHOOSE_CITY) : before;
    }

    @Override
    protected void onCleared() {
        executor.shutdownNow();
    }
}
