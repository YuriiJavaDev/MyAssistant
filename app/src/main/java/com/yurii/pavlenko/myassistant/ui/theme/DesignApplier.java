package com.yurii.pavlenko.myassistant.ui.theme;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Puts the chosen design on the theme of every activity of the app, before the activity builds its views.
 * Activity.onCreate of the base class reports the creation here, ahead of the subclass's setContentView.
 */
public final class DesignApplier implements Application.ActivityLifecycleCallbacks {

    @Override
    public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {
        AppDesign design = DesignStore.get(activity);
        if (design.hasOverlay()) {
            activity.getTheme().applyStyle(design.overlay, true);
        }
    }

    @Override
    public void onActivityStarted(@NonNull Activity activity) {
    }

    @Override
    public void onActivityResumed(@NonNull Activity activity) {
    }

    @Override
    public void onActivityPaused(@NonNull Activity activity) {
    }

    @Override
    public void onActivityStopped(@NonNull Activity activity) {
    }

    @Override
    public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {
    }

    @Override
    public void onActivityDestroyed(@NonNull Activity activity) {
    }
}
