package com.yurii.pavlenko.myassistant.tasks.ui;

import android.content.Context;

import androidx.annotation.StringRes;

import com.yurii.pavlenko.myassistant.R;
import com.yurii.pavlenko.myassistant.tasks.logic.Importance;

/** The translated names of the importance levels. */
public final class ImportanceText {

    private ImportanceText() {
    }

    @StringRes
    public static int labelRes(Importance importance) {
        switch (importance) {
            case URGENT:
                return R.string.tasks_importance_urgent;
            case IMPORTANT:
                return R.string.tasks_importance_important;
            case NORMAL:
            default:
                return R.string.tasks_importance_normal;
        }
    }

    /** The labels of all levels, in the order of {@link Importance#values()}. */
    public static String[] labels(Context context) {
        Importance[] all = Importance.values();
        String[] labels = new String[all.length];
        for (int i = 0; i < all.length; i++) {
            labels[i] = context.getString(labelRes(all[i]));
        }
        return labels;
    }
}
