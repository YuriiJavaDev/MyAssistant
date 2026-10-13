package com.yurii.pavlenko.myassistant.weather.ui.scene;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.Shader;

/** A sun with a glow that breathes and rays that turn slowly. */
final class SunLayer implements Layer {

    private static final float X = 0.74f;
    private static final float Y = 0.22f;
    private static final int RAYS = 12;

    private final Paint glow = new Paint();
    private final Paint disc = new Paint();
    private final Paint ray = new Paint();

    SunLayer() {
        glow.setShader(new RadialGradient(X, Y, 0.24f, new int[]{0x99FFF2A8, 0x00FFF2A8}, null, Shader.TileMode.CLAMP));
        disc.setColor(0xFFFFD93B);
        ray.setColor(0xFFFFE066);
        ray.setStyle(Paint.Style.STROKE);
        ray.setStrokeWidth(0.012f);
        ray.setStrokeCap(Paint.Cap.ROUND);
    }

    @Override
    public void draw(Canvas canvas, float seconds) {
        float pulse = 1 + 0.06f * (float) Math.sin(seconds * 1.6);
        canvas.drawCircle(X, Y, 0.24f * pulse, glow);

        canvas.save();
        canvas.rotate(seconds * 6, X, Y);
        for (int i = 0; i < RAYS; i++) {
            double angle = Math.PI * 2 * i / RAYS;
            float inner = 0.085f;
            float outer = i % 2 == 0 ? 0.125f : 0.105f;
            canvas.drawLine(X + inner * (float) Math.cos(angle), Y + inner * (float) Math.sin(angle),
                    X + outer * (float) Math.cos(angle), Y + outer * (float) Math.sin(angle), ray);
        }
        canvas.restore();
        canvas.drawCircle(X, Y, 0.068f, disc);
    }
}
