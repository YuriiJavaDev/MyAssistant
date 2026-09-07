package com.yurii.pavlenko.myassistant;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.yurii.pavlenko.myassistant.fragments.CalculatorFragment;
import com.yurii.pavlenko.myassistant.fragments.CurrencyFragment;
import com.yurii.pavlenko.myassistant.tasks.ui.TasksFragment;
import com.yurii.pavlenko.myassistant.fragments.WeatherFragment;

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
            default:
                return new TasksFragment(); // Default protection option (not the start page)
        }
    }

    @Override
    public int getItemCount() {
        return 4;
    }
}