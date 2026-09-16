package com.yurii.pavlenko.myassistant.ui.navigation;

import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;

/**
 * Helper class responsible for delegating tab navigation setup based on layout configuration
 * and providing common styling utilities.
 */
public class TabNavigationHelper {

    private final TabLayout tabLayout;
    private final TabLayout tabLayoutSecond;
    private final ViewPager2 viewPager;

    public TabNavigationHelper(TabLayout tabLayout, TabLayout tabLayoutSecond, ViewPager2 viewPager) {
        this.tabLayout = tabLayout;
        this.tabLayoutSecond = tabLayoutSecond;
        this.viewPager = viewPager;
    }

    /**
     * Initializes tab navigation by delegating to portrait or landscape handlers.
     */
    public void setupTabs(boolean isTwoRows, int primaryColor, int backgroundColor) {
        if (isTwoRows) {
            PortraitTabNavigationHandler portraitHandler = new PortraitTabNavigationHandler(
                    tabLayout, tabLayoutSecond, viewPager, primaryColor, backgroundColor
            );
            portraitHandler.setup();
        } else {
            LandscapeTabNavigationHandler landscapeHandler = new LandscapeTabNavigationHandler(
                    tabLayout, viewPager
            );
            landscapeHandler.setup();
        }
    }

    /**
     * Disables uppercase text transformation for tab layout items.
     */
    public static void disableAllCaps(TabLayout tabLayout) {
        for (int i = 0; i < tabLayout.getTabCount(); i++) {
            TabLayout.Tab tab = tabLayout.getTabAt(i);
            if (tab != null && tab.view != null) {
                applyNormalCaseToView(tab.view);
            }
        }
    }

    /**
     * Applies normal text casing recursively to view hierarchy.
     */
    private static void applyNormalCaseToView(View view) {
        if (view instanceof TextView) {
            TextView tv = (TextView) view;
            tv.setAllCaps(false);
            tv.setTransformationMethod(null);
        } else if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            for (int i = 0; i < group.getChildCount(); i++) {
                applyNormalCaseToView(group.getChildAt(i));
            }
        }
    }
}