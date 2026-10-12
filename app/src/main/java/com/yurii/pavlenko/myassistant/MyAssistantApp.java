package com.yurii.pavlenko.myassistant;

import android.app.Application;

import com.yurii.pavlenko.myassistant.steps.work.StepScheduler;
import com.yurii.pavlenko.myassistant.ui.theme.DisplayModeStore;

public class MyAssistantApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        DisplayModeStore.apply(this);
        StepScheduler.schedule(this);
    }
}
