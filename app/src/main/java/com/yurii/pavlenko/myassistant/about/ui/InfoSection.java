package com.yurii.pavlenko.myassistant.about.ui;

import androidx.annotation.ArrayRes;
import androidx.annotation.StringRes;

import com.yurii.pavlenko.myassistant.R;

/** The pages that can be read in the app without internet. */
public enum InfoSection {

    GUIDE(R.string.about_open_guide, R.array.info_guide, 0),
    PRIVACY(R.string.about_open_privacy, R.array.info_privacy, 0),
    LICENSE(R.string.about_open_license, R.array.info_license_summary, R.array.info_license_text);

    @StringRes
    final int title;
    @ArrayRes
    final int texts;
    @ArrayRes
    final int moreTexts;

    InfoSection(@StringRes int title, @ArrayRes int texts, @ArrayRes int moreTexts) {
        this.title = title;
        this.texts = texts;
        this.moreTexts = moreTexts;
    }
}
