package com.yurii.pavlenko.myassistant.weather.ui.scene;

/** Where things are in the unit-square porthole. */
final class SceneGeometry {

    static final float HORIZON = 0.50f;
    static final float SHORE = 0.72f;

    private SceneGeometry() {
    }

    /** The shoreline, which the surf moves up and down the beach. */
    static float shoreY(float x, float seconds) {
        return SHORE + 0.018f * (float) Math.sin(seconds * 0.9) + 0.006f * (float) Math.sin(x * 18 + seconds * 2);
    }

    /** Where the bottom edge of the porthole circle is at the given x. */
    static float rimBottom(float x) {
        float dx = x - 0.5f;
        return 0.5f + (float) Math.sqrt(Math.max(0, 0.25f - dx * dx));
    }
}
