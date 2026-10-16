package com.yurii.pavlenko.myassistant.flights.report;

import android.content.Context;
import android.util.Log;

import com.yurii.pavlenko.myassistant.flights.logic.Coastline;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.SoftReference;

/** Reads the bundled coastline once and keeps it while memory allows. */
final class CoastlineAsset {

    private static final String TAG = "CoastlineAsset";
    private static final String FILE_NAME = "coastline.bin";

    private static SoftReference<Coastline> cached = new SoftReference<>(null);

    private CoastlineAsset() {
    }

    /** The land outlines, or an empty coastline when the asset cannot be read, so the map shows only its grid. */
    static synchronized Coastline load(Context context) {
        Coastline coastline = cached.get();
        if (coastline == null) {
            try (InputStream in = new BufferedInputStream(context.getAssets().open(FILE_NAME))) {
                coastline = Coastline.read(in);
            } catch (IOException e) {
                Log.e(TAG, "Cannot read the coastline", e);
                return Coastline.empty();
            }
            cached = new SoftReference<>(coastline);
        }
        return coastline;
    }
}
