package com.yurii.pavlenko.myassistant.scan.ui;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;

import com.yurii.pavlenko.myassistant.R;
import com.yurii.pavlenko.myassistant.scan.imaging.PaperFormat;

/** Maps a paper format to the name shown to the user. */
final class PaperFormatText {

    private PaperFormatText() {
    }

    @StringRes
    static int of(@NonNull PaperFormat format) {
        switch (format) {
            case AUTO:
                return R.string.scan_auto;
            case A4:
                return R.string.scan_paper_a4;
            case A5:
                return R.string.scan_paper_a5;
            case LETTER:
                return R.string.scan_paper_letter;
            default:
                return R.string.scan_paper_original;
        }
    }
}
