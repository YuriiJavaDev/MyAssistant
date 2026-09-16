package com.yurii.pavlenko.myassistant.ui.navigation;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.yurii.pavlenko.myassistant.R;
import com.yurii.pavlenko.myassistant.ViewPagerAdapter;
import com.yurii.pavlenko.myassistant.databinding.ActivityMainBinding;

/**
 * Configures ViewPager2 adapter, tab titles, and tab navigation based on current orientation.
 */
public class ViewPagerConfigurator {

    private static final String[] ALL_TITLES = {"Tasks", "Weather", "Currency", "Calc", "Scan", "Steps", "Flights"};
    private static final String[] FIRST_ROW_TITLES = {"Tasks", "Weather", "Currency", "Calc"};
    private static final String[] SECOND_ROW_TITLES = {"Scan", "Steps", "Flights"};

    /**
     * Sets up the ViewPager adapter, populates tabs for single or dual row layout,
     * and initializes tab navigation synchronization.
     */
    public static void configure(AppCompatActivity activity, ActivityMainBinding binding) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(activity);
        binding.viewPager.setAdapter(adapter);

        boolean isTwoRows = binding.tabLayoutSecond != null;

        if (isTwoRows) {
            for (String title : FIRST_ROW_TITLES) {
                binding.tabLayout.addTab(binding.tabLayout.newTab().setText(title));
            }
            for (String title : SECOND_ROW_TITLES) {
                binding.tabLayoutSecond.addTab(binding.tabLayoutSecond.newTab().setText(title));
            }
        } else {
            for (String title : ALL_TITLES) {
                binding.tabLayout.addTab(binding.tabLayout.newTab().setText(title));
            }
        }

        TabNavigationHelper navigationHelper = new TabNavigationHelper(
                binding.tabLayout,
                binding.tabLayoutSecond,
                binding.viewPager
        );

        int primaryColor = ContextCompat.getColor(activity, R.color.colorPrimary);
        int backgroundColor = ContextCompat.getColor(activity, R.color.background);

        navigationHelper.setupTabs(isTwoRows, primaryColor, backgroundColor);
    }
}