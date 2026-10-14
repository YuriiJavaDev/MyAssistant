package com.yurii.pavlenko.myassistant.weather.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Path;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import com.yurii.pavlenko.myassistant.ui.widget.Bezel;
import com.yurii.pavlenko.myassistant.weather.ui.scene.Scene;

/** A round ship's window in which an animated scene plays. */
public class PortholeView extends View {

    private final Bezel bezel;
    private final Path window = new Path();
    private final long startMillis = SystemClock.uptimeMillis();

    private Scene scene;

    public PortholeView(Context context) {
        this(context, null);
    }

    public PortholeView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        bezel = new Bezel(context);
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
        float rimWidth = Bezel.rimWidth(size);
        float glassRadius = radius - rimWidth;

        canvas.save();
        window.reset();
        window.addCircle(radius, radius, glassRadius, Path.Direction.CW);
        canvas.clipPath(window);
        canvas.translate(rimWidth, rimWidth);
        scene.draw(canvas, glassRadius * 2, (SystemClock.uptimeMillis() - startMillis) / 1000f);
        canvas.restore();

        bezel.draw(canvas, size);

        postInvalidateOnAnimation();
    }
}
