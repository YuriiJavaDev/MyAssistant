package com.yurii.pavlenko.myassistant.weather.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.yurii.pavlenko.myassistant.R;
import com.yurii.pavlenko.myassistant.weather.ui.scene.Scene;

/** A round ship's window in which an animated scene plays. */
public class PortholeView extends View {

    private static final float RIM_WIDTH = 0.05f;
    private static final int BOLTS = 12;

    private final Paint rim = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint highlight = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint bolt = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path window = new Path();
    private final long startMillis = SystemClock.uptimeMillis();

    private Scene scene;

    public PortholeView(Context context) {
        this(context, null);
    }

    public PortholeView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        rim.setStyle(Paint.Style.STROKE);
        rim.setColor(ContextCompat.getColor(context, R.color.colorPrimary));
        highlight.setStyle(Paint.Style.STROKE);
        highlight.setColor(ContextCompat.getColor(context, R.color.outline));
        bolt.setColor(ContextCompat.getColor(context, R.color.text_color_btn));
    }

    public void show(Scene newScene) {
        scene = newScene;
        invalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int size = Math.min(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.getSize(heightMeasureSpec));
        if (MeasureSpec.getMode(heightMeasureSpec) == MeasureSpec.UNSPECIFIED) {
            size = MeasureSpec.getSize(widthMeasureSpec);
        }
        setMeasuredDimension(size, size);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (scene == null) {
            return;
        }
        float size = Math.min(getWidth(), getHeight());
        float radius = size / 2f;
        float rimWidth = size * RIM_WIDTH;
        float glassRadius = radius - rimWidth;

        canvas.save();
        window.reset();
        window.addCircle(radius, radius, glassRadius, Path.Direction.CW);
        canvas.clipPath(window);
        canvas.translate(rimWidth, rimWidth);
        scene.draw(canvas, glassRadius * 2, (SystemClock.uptimeMillis() - startMillis) / 1000f);
        canvas.restore();

        rim.setStrokeWidth(rimWidth);
        canvas.drawCircle(radius, radius, glassRadius + rimWidth / 2f, rim);
        highlight.setStrokeWidth(rimWidth * 0.18f);
        canvas.drawCircle(radius, radius, glassRadius + rimWidth * 0.78f, highlight);
        drawBolts(canvas, radius, glassRadius + rimWidth / 2f, rimWidth * 0.2f);

        postInvalidateOnAnimation();
    }

    private void drawBolts(Canvas canvas, float center, float ringRadius, float boltRadius) {
        for (int i = 0; i < BOLTS; i++) {
            double angle = Math.PI * 2 * i / BOLTS;
            canvas.drawCircle(center + ringRadius * (float) Math.cos(angle),
                    center + ringRadius * (float) Math.sin(angle), boltRadius, bolt);
        }
    }
}
