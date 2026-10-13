package com.yurii.pavlenko.myassistant.weather.ui.scene;

/** Where things are in the unit-square porthole. */
final class SceneGeometry {

    static final float HORIZON = 0.50f;
    static final float SHORE = 0.72f;

    private SceneGeometry() {
    }

    /** The shoreline, which the surf moves up and down the beach; a rough sea throws it further. */
    static float shoreY(float x, float seconds, float roughness) {
        float surge = 0.018f * (1 + 1.5f * roughness) * (float) Math.sin(seconds * (0.9f + 0.5f * roughness));
        float ripple = 0.006f * (1 + 2 * roughness) * (float) Math.sin(x * 18 + seconds * 2);
        float chop = 0.008f * roughness * (float) Math.sin(x * 40 + seconds * 3.5f);
        return SHORE + surge + ripple + chop;
    }

    /** Where the bottom edge of the porthole circle is at the given x. */
    static float rimBottom(float x) {
        float dx = x - 0.5f;
        return 0.5f + (float) Math.sqrt(Math.max(0, 0.25f - dx * dx));
    }
}
