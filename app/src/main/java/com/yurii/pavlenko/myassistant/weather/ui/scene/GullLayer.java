package com.yurii.pavlenko.myassistant.weather.ui.scene;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;

/** Seagulls gliding over the waves on the wind. */
final class GullLayer implements Layer {

    private static final int GULLS = 3;

    private final Paint wing = new Paint();
    private final Paint body = new Paint();
    private final Path stroke = new Path();
    private final float speed;

    GullLayer(double windMetresPerSecond) {
        speed = 1 + (float) windMetresPerSecond / 10f;
        wing.setColor(0xFFFFFFFF);
        wing.setStyle(Paint.Style.STROKE);
        wing.setStrokeWidth(0.008f);
        wing.setStrokeCap(Paint.Cap.ROUND);
        wing.setStrokeJoin(Paint.Join.ROUND);
        body.setColor(0xFFFFFFFF);
    }

    @Override
    public void draw(Canvas canvas, float seconds) {
        for (int i = 0; i < GULLS; i++) {
            float x = 0.5f + 0.36f * (float) Math.sin(seconds * 0.22f * speed + i * 2.1f);
            float y = 0.27f + 0.08f * i + 0.04f * (float) Math.sin(seconds * 0.6f + i);
            float flap = 0.35f * (float) Math.sin(seconds * 2.4f * speed + i * 1.3f);
            drawGull(canvas, x, y, flap);
        }
    }

    private void drawGull(Canvas canvas, float x, float y, float flap) {
        float lift = 0.014f * flap;
        stroke.reset();
        stroke.moveTo(x - 0.05f, y + 0.008f - lift);
        stroke.quadTo(x - 0.025f, y - 0.022f - lift, x, y);
        stroke.quadTo(x + 0.025f, y - 0.022f - lift, x + 0.05f, y + 0.008f - lift);
        canvas.drawPath(stroke, wing);
        canvas.drawOval(x - 0.012f, y - 0.004f, x + 0.012f, y + 0.008f, body);
    }
}
