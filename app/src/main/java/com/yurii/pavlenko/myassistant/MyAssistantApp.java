package com.yurii.pavlenko.myassistant;

import android.app.Application;

import com.yurii.pavlenko.myassistant.steps.work.StepScheduler;
import com.yurii.pavlenko.myassistant.ui.locale.LanguageStore;
import com.yurii.pavlenko.myassistant.ui.theme.DesignApplier;
import com.yurii.pavlenko.myassistant.ui.theme.DisplayModeStore;

public class MyAssistantApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        LanguageStore.ensureChosen();
        DisplayModeStore.apply(this);
        registerActivityLifecycleCallbacks(new DesignApplier());
        StepScheduler.schedule(this);
    }
}
