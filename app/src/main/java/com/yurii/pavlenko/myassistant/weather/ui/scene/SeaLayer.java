package com.yurii.pavlenko.myassistant.weather.ui.scene;

import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Shader;

/** The sea with shimmering waves, and surf that rolls up the beach and back. */
final class SeaLayer implements Layer {

    private static final int STEPS = 24;
    private static final int GLINTS = 14;

    private final Paint water = new Paint();
    private final Paint glint = new Paint();
    private final Paint foam = new Paint();
    private final Paint wet = new Paint();
    private final Path body = new Path();
    private final Path edge = new Path();

    SeaLayer(Palette palette) {
        water.setShader(new LinearGradient(0, SceneGeometry.HORIZON, 0, SceneGeometry.SHORE, palette.seaFar, palette.seaNear, Shader.TileMode.CLAMP));
        glint.setColor(0x66FFFFFF);
        glint.setStyle(Paint.Style.STROKE);
        glint.setStrokeWidth(0.006f);
        glint.setStrokeCap(Paint.Cap.ROUND);
        foam.setColor(0xDDFFFFFF);
        foam.setStyle(Paint.Style.STROKE);
        foam.setStrokeWidth(0.014f);
        foam.setStrokeCap(Paint.Cap.ROUND);
        wet.setColor(palette.wetSand);
        wet.setStyle(Paint.Style.STROKE);
        wet.setStrokeWidth(0.03f);
    }

    @Override
    public void draw(Canvas canvas, float seconds) {
        body.reset();
        edge.reset();
        body.moveTo(0, SceneGeometry.HORIZON);
        body.lineTo(1, SceneGeometry.HORIZON);
        for (int i = STEPS; i >= 0; i--) {
            float x = i / (float) STEPS;
            float y = SceneGeometry.shoreY(x, seconds);
            body.lineTo(x, y);
            if (i == STEPS) {
                edge.moveTo(x, y);
            } else {
                edge.lineTo(x, y);
            }
        }
        body.close();
        canvas.drawPath(body, water);

        for (int i = 0; i < GLINTS; i++) {
            float x = Noise.unit(i, 9);
            float y = SceneGeometry.HORIZON + 0.03f + (SceneGeometry.SHORE - SceneGeometry.HORIZON - 0.08f) * Noise.unit(i, 10);
            float shimmer = 0.5f + 0.5f * (float) Math.sin(seconds * 1.5f + i * 1.7f);
            glint.setAlpha((int) (140 * shimmer));
            float half = 0.02f + 0.02f * (y - SceneGeometry.HORIZON);
            canvas.drawLine(x - half, y, x + half, y, glint);
        }

        canvas.save();
        canvas.translate(0, 0.022f);
        canvas.drawPath(edge, wet);
        canvas.restore();
        canvas.drawPath(edge, foam);
    }
}
