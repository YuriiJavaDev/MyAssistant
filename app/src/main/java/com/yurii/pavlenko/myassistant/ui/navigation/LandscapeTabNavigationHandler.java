package com.yurii.pavlenko.myassistant.ui.navigation;

import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;

/**
 * Handles tab selection and synchronization for landscape orientation (single row of tabs).
 */
public class LandscapeTabNavigationHandler {

    private final TabLayout tabLayout;
    private final ViewPager2 viewPager;
    private boolean isSyncing = false;

    public LandscapeTabNavigationHandler(TabLayout tabLayout, ViewPager2 viewPager) {
        this.tabLayout = tabLayout;
        this.viewPager = viewPager;
    }

    public void setup() {
        setupTabListener();
        setupPageChangeCallback();
        TabNavigationHelper.disableAllCaps(tabLayout);
    }

    private void setupTabListener() {
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (isSyncing) return;
                int position = tab.getPosition();
                if (viewPager.getCurrentItem() != position) {
                    viewPager.setCurrentItem(position);
                }
            }
            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void setupPageChangeCallback() {
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                isSyncing = true;

                if (tabLayout.getSelectedTabPosition() != position) {
                    tabLayout.selectTab(tabLayout.getTabAt(position));
                }

                for (int i = 0; i < tabLayout.getTabCount(); i++) {
                    TabLayout.Tab tab = tabLayout.getTabAt(i);
                    if (tab != null && tab.view != null) {
                        tab.view.setSelected(i == position);
                    }
                }

                TabNavigationHelper.disableAllCaps(tabLayout);
                isSyncing = false;
            }
        });
    }
}