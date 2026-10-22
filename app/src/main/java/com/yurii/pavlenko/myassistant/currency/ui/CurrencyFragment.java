package com.yurii.pavlenko.myassistant.currency.ui;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.yurii.pavlenko.myassistant.R;
import com.yurii.pavlenko.myassistant.currency.logic.Conversion;
import com.yurii.pavlenko.myassistant.currency.logic.CurrencyPair;
import com.yurii.pavlenko.myassistant.currency.logic.ExchangeRates;
import com.yurii.pavlenko.myassistant.currency.logic.MoneyFormat;
import com.yurii.pavlenko.myassistant.currency.viewmodel.CurrencyViewModel;
import com.yurii.pavlenko.myassistant.currency.viewmodel.RatesState;
import com.yurii.pavlenko.myassistant.databinding.FragmentCurrencyBinding;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/** The Exchange tab: converts any amount between two currencies at the latest daily rates. */
public class CurrencyFragment extends Fragment {

    private static final String DATE_PATTERN = "d MMM yyyy, HH:mm";

    private FragmentCurrencyBinding binding;
    private CurrencyViewModel viewModel;
    private List<String> codes = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentCurrencyBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(CurrencyViewModel.class);

        binding.amountInput.setText(viewModel.getAmount());
        binding.amountInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                viewModel.setAmount(s.toString());
                showResult();
            }
        });

        AdapterView.OnItemSelectedListener onPick = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View item, int position, long id) {
                CurrencyPair picked = new CurrencyPair(
                        codes.get(binding.fromSpinner.getSelectedItemPosition()),
                        codes.get(binding.toSpinner.getSelectedItemPosition()));
                if (!picked.equals(viewModel.getPair().getValue())) {
                    viewModel.choose(picked);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        };
        binding.fromSpinner.setOnItemSelectedListener(onPick);
        binding.toSpinner.setOnItemSelectedListener(onPick);
        binding.swapButton.setOnClickListener(v -> {
            CurrencyPair pair = viewModel.getPair().getValue();
            if (pair != null) {
                viewModel.choose(pair.swapped());
            }
        });
        binding.refreshButton.setOnClickListener(v -> viewModel.refresh());

        viewModel.getState().observe(getViewLifecycleOwner(), state -> render());
        viewModel.getPair().observe(getViewLifecycleOwner(), pair -> render());
    }

    @Override
    public void onResume() {
        super.onResume();
        viewModel.refreshIfStale();
    }

    private void render() {
        RatesState state = viewModel.getState().getValue();
        CurrencyPair pair = viewModel.getPair().getValue();
        boolean ready = state != null && state.getStatus() == RatesState.Status.READY && pair != null;
        binding.resultPanel.setVisibility(ready ? View.VISIBLE : View.GONE);
        if (!ready) {
            binding.statusText.setText(state != null && state.getStatus() == RatesState.Status.ERROR
                    ? R.string.currency_unavailable : R.string.currency_loading);
            return;
        }
        fillSpinners(state.getRates(), pair);
        showResult();
        showStatus(state);
    }

    private void fillSpinners(ExchangeRates rates, CurrencyPair pair) {
        if (!rates.codes().equals(codes)) {
            codes = rates.codes();
            List<String> labels = new ArrayList<>();
            for (String code : codes) {
                labels.add(MoneyFormat.label(code, Locale.getDefault()));
            }
            binding.fromSpinner.setAdapter(adapterOf(labels));
            binding.toSpinner.setAdapter(adapterOf(labels));
        }
        binding.fromSpinner.setSelection(codes.indexOf(pair.getFrom()), false);
        binding.toSpinner.setSelection(codes.indexOf(pair.getTo()), false);
    }

    private ArrayAdapter<String> adapterOf(List<String> labels) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), R.layout.item_spinner, labels);
        adapter.setDropDownViewResource(R.layout.item_spinner);
        return adapter;
    }

    private void showResult() {
        RatesState state = viewModel.getState().getValue();
        CurrencyPair pair = viewModel.getPair().getValue();
        if (binding == null || state == null || state.getRates() == null || pair == null) {
            return;
        }
        Locale locale = Locale.getDefault();
        Conversion conversion = Conversion.of(state.getRates(), pair, String.valueOf(binding.amountInput.getText()));
        if (conversion == null) {
            binding.resultText.setText(R.string.currency_invalid_amount);
        } else {
            binding.resultText.setText(getString(R.string.currency_result_format,
                    MoneyFormat.amount(conversion.getResult(), locale), pair.getTo()));
        }
        double rate = state.getRates().rate(pair.getFrom(), pair.getTo());
        binding.rateText.setText(getString(R.string.currency_rate_format,
                pair.getFrom(), MoneyFormat.rate(rate, locale), pair.getTo()));
    }

    private void showStatus(RatesState state) {
        String updated = new SimpleDateFormat(DATE_PATTERN, Locale.getDefault())
                .format(new Date(state.getRates().getUpdatedAtMillis()));
        binding.statusText.setText(getString(
                state.isOutdated() ? R.string.currency_outdated : R.string.currency_updated, updated));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null; // memory leak prevention
    }
}
