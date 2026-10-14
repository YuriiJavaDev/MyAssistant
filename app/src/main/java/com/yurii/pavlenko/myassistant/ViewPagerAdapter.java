package com.yurii.pavlenko.myassistant;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.yurii.pavlenko.myassistant.calculator.ui.CalculatorFragment;
import com.yurii.pavlenko.myassistant.currency.ui.CurrencyFragment;
import com.yurii.pavlenko.myassistant.flights.ui.FlightsFragment;
import com.yurii.pavlenko.myassistant.scan.ui.ScanFragment;
import com.yurii.pavlenko.myassistant.steps.ui.StepsFragment;
import com.yurii.pavlenko.myassistant.tasks.ui.TasksFragment;
import com.yurii.pavlenko.myassistant.weather.ui.WeatherFragment;

public class ViewPagerAdapter extends FragmentStateAdapter {

    public ViewPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new TasksFragment(); // Home page at startup
            case 1:
                return new WeatherFragment();
            case 2:
                return new CurrencyFragment();
            case 3:
                return new CalculatorFragment();
            case 4:
                return new ScanFragment();
            case 5:
                return new StepsFragment();
            case 6:
                return new FlightsFragment();
            default:
                return new TasksFragment(); // Default fallback
        }
    }

    @Override
    public int getItemCount() {
        return 7;
    }
}