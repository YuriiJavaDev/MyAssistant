package com.yurii.pavlenko.myassistant;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.yurii.pavlenko.myassistant.fragments.CalculatorFragment;
import com.yurii.pavlenko.myassistant.fragments.CurrencyFragment;
import com.yurii.pavlenko.myassistant.fragments.TasksFragment;
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
                return new TasksFragment();      // Головна сторінка при старті
            case 1:
                return new WeatherFragment();    // Погода
            case 2:
                return new CurrencyFragment();   // Обмін валют
            case 3:
                return new CalculatorFragment(); // Калькулятор
            default:
                return new TasksFragment();      // Захисний запасний варіант
        }
    }

    @Override
    public int getItemCount() {
        return 4; // Всього 4 вкладки
    }
}