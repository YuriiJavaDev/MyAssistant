package com.yurii.pavlenko.myassistant.weather.ui.scene;

import android.graphics.Canvas;
import android.graphics.Paint;

/** Banks of mist drifting slowly over the water and the beach. */
final class FogLayer implements Layer {

    private static final int BANKS = 5;

    private final Paint paint = new Paint();

    FogLayer() {
        paint.setColor(0x66FFFFFF);
    }

    @Override
    public void draw(Canvas canvas, float seconds) {
        for (int i = 0; i < BANKS; i++) {
            float y = 0.40f + 0.13f * i;
            float drift = 0.12f * (float) Math.sin(seconds * 0.25f + i * 1.3f);
            canvas.drawOval(-0.2f + drift, y - 0.05f, 0.75f + drift, y + 0.05f, paint);
            canvas.drawOval(0.3f - drift, y - 0.04f, 1.2f - drift, y + 0.06f, paint);
        }
    }
}
