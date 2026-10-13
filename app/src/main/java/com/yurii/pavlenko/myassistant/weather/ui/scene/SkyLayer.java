package com.yurii.pavlenko.myassistant.weather.ui.scene;

import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;

/** The sky, darkest at the top and lightest at its lower edge. */
final class SkyLayer implements Layer {

    private final Paint paint = new Paint();
    private final float bottom;

    SkyLayer(Palette palette, float bottom) {
        this.bottom = bottom;
        paint.setShader(new LinearGradient(0, 0, 0, bottom, palette.skyTop, palette.skyBottom, Shader.TileMode.CLAMP));
    }

    @Override
    public void draw(Canvas canvas, float seconds) {
        canvas.drawRect(0, 0, 1, bottom, paint);
    }
}
