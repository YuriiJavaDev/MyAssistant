package com.yurii.pavlenko.myassistant.ui.navigation;

import androidx.appcompat.app.AppCompatActivity;

import com.yurii.pavlenko.myassistant.R;
import com.yurii.pavlenko.myassistant.ViewPagerAdapter;
import com.yurii.pavlenko.myassistant.databinding.ActivityMainBinding;
import com.yurii.pavlenko.myassistant.ui.theme.ThemeColors;

/**
 * Configures ViewPager2 adapter, tab titles, and tab navigation based on current orientation.
 */
public class ViewPagerConfigurator {

    private static final int[] ALL_TITLES = {R.string.app_tab_tasks, R.string.app_tab_weather, R.string.app_tab_exchange,
            R.string.app_tab_calculator, R.string.app_tab_scan, R.string.app_tab_steps, R.string.app_tab_flights};
    private static final int[] FIRST_ROW_TITLES = {R.string.app_tab_tasks, R.string.app_tab_weather, R.string.app_tab_exchange,
            R.string.app_tab_calculator};
    private static final int[] SECOND_ROW_TITLES = {R.string.app_tab_scan, R.string.app_tab_steps, R.string.app_tab_flights};

    /**
     * Sets up the ViewPager adapter, populates tabs for single or dual row layout,
     * and initializes tab navigation synchronization.
     */
    public static void configure(AppCompatActivity activity, ActivityMainBinding binding) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(activity);
        binding.viewPager.setAdapter(adapter);

        boolean isTwoRows = binding.tabLayoutSecond != null;

        if (isTwoRows) {
            for (int title : FIRST_ROW_TITLES) {
                binding.tabLayout.addTab(binding.tabLayout.newTab().setText(title));
            }
            for (int title : SECOND_ROW_TITLES) {
                binding.tabLayoutSecond.addTab(binding.tabLayoutSecond.newTab().setText(title));
            }
        } else {
            for (int title : ALL_TITLES) {
                binding.tabLayout.addTab(binding.tabLayout.newTab().setText(title));
            }
        }

        TabNavigationHelper navigationHelper = new TabNavigationHelper(
                binding.tabLayout,
                binding.tabLayoutSecond,
                binding.viewPager
        );

        int primaryColor = ThemeColors.get(activity, R.attr.maAccent);
        int backgroundColor = ThemeColors.get(activity, R.attr.maBackground);

        navigationHelper.setupTabs(isTwoRows, primaryColor, backgroundColor);
    }
}