package com.yurii.pavlenko.myassistant.currency.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.yurii.pavlenko.myassistant.currency.data.HomeCurrency;
import com.yurii.pavlenko.myassistant.currency.data.PairStore;
import com.yurii.pavlenko.myassistant.currency.data.RatesService;
import com.yurii.pavlenko.myassistant.currency.data.RatesStore;
import com.yurii.pavlenko.myassistant.currency.logic.CurrencyPair;
import com.yurii.pavlenko.myassistant.currency.logic.DefaultPair;
import com.yurii.pavlenko.myassistant.currency.logic.ExchangeRates;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/** State of the converter: the rates, the chosen pair of currencies and the typed amount. */
public class CurrencyViewModel extends AndroidViewModel {

    private static final long STALE_MILLIS = TimeUnit.HOURS.toMillis(1);

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final RatesStore ratesStore;
    private final PairStore pairStore;
    private final MutableLiveData<RatesState> state = new MutableLiveData<>();
    private final MutableLiveData<CurrencyPair> pair = new MutableLiveData<>();
    private volatile boolean loading;
    private volatile long checkedAt;
    private String amount;

    public CurrencyViewModel(@NonNull Application application) {
        super(application);
        ratesStore = new RatesStore(application);
        pairStore = new PairStore(application);
        amount = pairStore.loadAmount();
        ExchangeRates saved = ratesStore.load();
        if (saved != null) {
            show(saved, false);
        } else {
            state.setValue(RatesState.loading());
        }
    }

    public LiveData<RatesState> getState() {
        return state;
    }

    public LiveData<CurrencyPair> getPair() {
        return pair;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String text) {
        amount = text;
        pairStore.saveAmount(text);
    }

    public void choose(CurrencyPair chosen) {
        pairStore.savePair(chosen);
        pair.setValue(chosen);
    }

    /** Downloads the rates when the saved ones are older than an hour or missing. */
    public void refreshIfStale() {
        RatesState current = state.getValue();
        boolean hasRates = current != null && current.getStatus() == RatesState.Status.READY;
        boolean old = System.currentTimeMillis() - checkedAt > STALE_MILLIS;
        if (!hasRates || old) {
            refresh();
        }
    }

    public void refresh() {
        if (loading) {
            return;
        }
        loading = true;
        RatesState before = state.getValue();
        if (before == null || before.getStatus() != RatesState.Status.READY) {
            state.setValue(RatesState.loading());
        }
        executor.execute(() -> {
            try {
                ExchangeRates fresh = RatesService.load();
                ratesStore.save(fresh);
                checkedAt = System.currentTimeMillis();
                postReady(fresh, false);
            } catch (IOException e) {
                ExchangeRates saved = ratesStore.load();
                if (saved != null) {
                    postReady(saved, true);
                } else {
                    state.postValue(RatesState.error());
                }
            } finally {
                loading = false;
            }
        });
    }

    private void show(ExchangeRates rates, boolean outdated) {
        selectPairFor(rates);
        state.setValue(RatesState.ready(rates, outdated));
    }

    private void postReady(ExchangeRates rates, boolean outdated) {
        if (pair.getValue() == null) {
            pair.postValue(initialPair(rates));
        }
        state.postValue(RatesState.ready(rates, outdated));
    }

    private void selectPairFor(ExchangeRates rates) {
        if (pair.getValue() == null) {
            pair.setValue(initialPair(rates));
        }
    }

    /** The pair the user chose before, or the home currency against the dollar. */
    private CurrencyPair initialPair(ExchangeRates rates) {
        CurrencyPair saved = pairStore.loadPair();
        boolean usable = saved != null && rates.has(saved.getFrom()) && rates.has(saved.getTo());
        return usable ? saved : DefaultPair.choose(HomeCurrency.find(getApplication()), rates);
    }

    @Override
    protected void onCleared() {
        executor.shutdownNow();
    }
}
