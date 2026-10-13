package com.yurii.pavlenko.myassistant.weather.ui.scene;

import android.graphics.Canvas;

/**
 * One piece of the scene. The canvas is scaled so the porthole is the unit square, and a layer is
 * a pure function of the time in seconds, so it keeps no animation state.
 */
interface Layer {
    void draw(Canvas canvas, float seconds);
}
