package com.yurii.pavlenko.myassistant.weather.ui.scene;

import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;

/** The beach under the sea; the surf then covers the part the water has reached. */
final class SandLayer implements Layer {

    private final Paint paint = new Paint();

    SandLayer(Palette palette) {
        paint.setShader(new LinearGradient(0, SceneGeometry.HORIZON, 0, 1, palette.wetSand, palette.sand, Shader.TileMode.CLAMP));
    }

    @Override
    public void draw(Canvas canvas, float seconds) {
        canvas.drawRect(0, SceneGeometry.HORIZON, 1, 1, paint);
    }
}
