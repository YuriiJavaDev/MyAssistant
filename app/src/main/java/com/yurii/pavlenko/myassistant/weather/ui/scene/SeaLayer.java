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
    private static final int CREST_ROWS = 4;
    private static final int CREST_STEPS = 20;
    private static final float CREST_MIN_ROUGHNESS = 0.15f;

    private final Paint water = new Paint();
    private final Paint glint = new Paint();
    private final Paint foam = new Paint();
    private final Paint wet = new Paint();
    private final Path body = new Path();
    private final Path edge = new Path();
    private final Paint crest = new Paint();
    private final Path crestLine = new Path();
    private final float roughness;

    SeaLayer(Palette palette, float roughness) {
        this.roughness = roughness;
        crest.setColor(0xFFFFFFFF);
        crest.setStyle(Paint.Style.STROKE);
        crest.setStrokeWidth(0.006f);
        crest.setStrokeCap(Paint.Cap.ROUND);
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
            float y = SceneGeometry.shoreY(x, seconds, roughness);
            body.lineTo(x, y);
            if (i == STEPS) {
                edge.moveTo(x, y);
            } else {
                edge.lineTo(x, y);
            }
        }
        body.close();
        canvas.drawPath(body, water);

        drawCrests(canvas, seconds);
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

    /** Lines of whitecaps that appear and swell as the sea gets rougher. */
    private void drawCrests(Canvas canvas, float seconds) {
        if (roughness < CREST_MIN_ROUGHNESS) {
            return;
        }
        crest.setAlpha((int) (70 + 130 * roughness));
        float amplitude = 0.006f + 0.016f * roughness;
        for (int row = 0; row < CREST_ROWS; row++) {
            float baseY = SceneGeometry.HORIZON + 0.06f + 0.05f * row;
            crestLine.reset();
            for (int i = 0; i <= CREST_STEPS; i++) {
                float x = i / (float) CREST_STEPS;
                float y = baseY + amplitude * (float) Math.sin(x * 14 + seconds * (1.5f + 0.3f * row) + row);
                if (i == 0) {
                    crestLine.moveTo(x, y);
                } else {
                    crestLine.lineTo(x, y);
                }
            }
            canvas.drawPath(crestLine, crest);
        }
    }
}
