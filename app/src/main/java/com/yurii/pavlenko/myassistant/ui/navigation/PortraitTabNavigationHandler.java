package com.yurii.pavlenko.myassistant.ui.navigation;

import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;

/**
 * Handles tab selection and synchronization for portrait orientation (two rows of tabs).
 */
public class PortraitTabNavigationHandler {

    private final TabLayout tabLayout;
    private final TabLayout tabLayoutSecond;
    private final ViewPager2 viewPager;
    private final int primaryColor;
    private final int backgroundColor;
    private boolean isSyncing = false;

    public PortraitTabNavigationHandler(TabLayout tabLayout, TabLayout tabLayoutSecond,
                                        ViewPager2 viewPager, int primaryColor, int backgroundColor) {
        this.tabLayout = tabLayout;
        this.tabLayoutSecond = tabLayoutSecond;
        this.viewPager = viewPager;
        this.primaryColor = primaryColor;
        this.backgroundColor = backgroundColor;
    }

    public void setup() {
        setupPrimaryTabListener();
        setupSecondaryTabListener();
        setupPageChangeCallback();

        TabNavigationHelper.disableAllCaps(tabLayout);
        if (tabLayoutSecond != null) {
            TabNavigationHelper.disableAllCaps(tabLayoutSecond);
        }
    }

    private void setupPrimaryTabListener() {
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

    private void setupSecondaryTabListener() {
        if (tabLayoutSecond == null) return;
        tabLayoutSecond.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (isSyncing) return;
                int position = tab.getPosition() + 4;
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

                updateIndicators(position);
                updateSelectionStates(position);

                TabNavigationHelper.disableAllCaps(tabLayout);
                if (tabLayoutSecond != null) {
                    TabNavigationHelper.disableAllCaps(tabLayoutSecond);
                }
                isSyncing = false;
            }
        });
    }

    private void updateIndicators(int position) {
        if (position < 4) {
            if (tabLayout.getSelectedTabPosition() != position) {
                tabLayout.selectTab(tabLayout.getTabAt(position));
            }
            if (tabLayoutSecond != null) {
                tabLayoutSecond.selectTab(null);
            }
            tabLayout.setSelectedTabIndicatorColor(primaryColor);
            if (tabLayoutSecond != null) {
                tabLayoutSecond.setSelectedTabIndicatorColor(backgroundColor);
            }
        } else {
            int secondRowPos = position - 4;
            if (tabLayoutSecond != null && tabLayoutSecond.getSelectedTabPosition() != secondRowPos) {
                tabLayoutSecond.selectTab(tabLayoutSecond.getTabAt(secondRowPos));
            }
            tabLayout.selectTab(null);
            tabLayout.setSelectedTabIndicatorColor(backgroundColor);
            if (tabLayoutSecond != null) {
                tabLayoutSecond.setSelectedTabIndicatorColor(primaryColor);
            }
        }
    }

    private void updateSelectionStates(int position) {
        for (int i = 0; i < tabLayout.getTabCount(); i++) {
            TabLayout.Tab tab = tabLayout.getTabAt(i);
            if (tab != null && tab.view != null) {
                tab.view.setSelected(position < 4 && i == position);
            }
        }
        if (tabLayoutSecond != null) {
            for (int i = 0; i < tabLayoutSecond.getTabCount(); i++) {
                TabLayout.Tab tab = tabLayoutSecond.getTabAt(i);
                if (tab != null && tab.view != null) {
                    tab.view.setSelected(position >= 4 && i == (position - 4));
                }
            }
        }
    }
}