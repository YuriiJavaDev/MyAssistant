package com.yurii.pavlenko.myassistant.about.ui;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.yurii.pavlenko.myassistant.R;

/** Opens a web page in the browser and says so when there is no browser. */
final class AppLinks {

    private AppLinks() {
    }

    static void open(@NonNull Context context, @NonNull String url) {
        try {
            context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
        } catch (RuntimeException e) {
            Toast.makeText(context, R.string.about_link_failed, Toast.LENGTH_SHORT).show();
        }
    }
}
