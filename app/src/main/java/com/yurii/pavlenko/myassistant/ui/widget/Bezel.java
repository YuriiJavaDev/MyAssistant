package com.yurii.pavlenko.myassistant.ui.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;

import androidx.core.content.ContextCompat;

import com.yurii.pavlenko.myassistant.R;

/** The riveted round frame shared by the weather porthole and the flight instruments. */
public final class Bezel {

    private static final float RIM_WIDTH = 0.05f;
    private static final int BOLTS = 12;

    private final Paint rim = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint highlight = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint bolt = new Paint(Paint.ANTI_ALIAS_FLAG);

    public Bezel(Context context) {
        rim.setStyle(Paint.Style.STROKE);
        rim.setColor(ContextCompat.getColor(context, R.color.colorPrimary));
        highlight.setStyle(Paint.Style.STROKE);
        highlight.setColor(ContextCompat.getColor(context, R.color.outline));
        bolt.setColor(ContextCompat.getColor(context, R.color.text_color_btn));
    }

    /** The width of the frame for a round view of the given size. */
    public static float rimWidth(float size) {
        return size * RIM_WIDTH;
    }

    /** Draws the frame around the glass, which fills the view inside the frame. */
    public void draw(Canvas canvas, float size) {
        float radius = size / 2f;
        float rimWidth = rimWidth(size);
        float glassRadius = radius - rimWidth;

        rim.setStrokeWidth(rimWidth);
        canvas.drawCircle(radius, radius, glassRadius + rimWidth / 2f, rim);
        highlight.setStrokeWidth(rimWidth * 0.18f);
        canvas.drawCircle(radius, radius, glassRadius + rimWidth * 0.78f, highlight);
        drawBolts(canvas, radius, glassRadius + rimWidth / 2f, rimWidth * 0.2f);
    }

    private void drawBolts(Canvas canvas, float center, float ringRadius, float boltRadius) {
        for (int i = 0; i < BOLTS; i++) {
            double angle = Math.PI * 2 * i / BOLTS;
            canvas.drawCircle(center + ringRadius * (float) Math.cos(angle),
                    center + ringRadius * (float) Math.sin(angle), boltRadius, bolt);
        }
    }
}
