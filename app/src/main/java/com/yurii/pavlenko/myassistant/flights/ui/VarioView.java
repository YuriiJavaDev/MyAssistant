package com.yurii.pavlenko.myassistant.flights.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.yurii.pavlenko.myassistant.R;
import com.yurii.pavlenko.myassistant.flights.logic.ClimbUnit;

import java.util.Locale;

/**
 * The variometer: a needle over a scale that is green for climb above the zero on the left and red
 * for sink below it, with the climb rate in digits in a small window on the right.
 */
public class VarioView extends InstrumentView {

    private static final float ZERO_ANGLE = 180f;
    private static final float SIDE_SWEEP = 140f;
    private static final float RIM_FRACTION = 0.93f;

    private final Paint band = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint sector = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint tick = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint label = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint needle = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint window = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint windowRim = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint readout = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint caption = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF arc = new RectF();
    private final int climbColor;
    private final int sinkColor;

    private ClimbUnit unit = ClimbUnit.METRES_PER_SECOND;
    private double metresPerSecond;

    public VarioView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        climbColor = ContextCompat.getColor(context, R.color.instrument_climb);
        sinkColor = ContextCompat.getColor(context, R.color.instrument_sink);
        band.setStyle(Paint.Style.STROKE);
        sector.setStyle(Paint.Style.FILL);
        tick.setColor(ink);
        tick.setStrokeCap(Paint.Cap.ROUND);
        label.setColor(ink);
        label.setTextAlign(Paint.Align.CENTER);
        needle.setColor(accent);
        needle.setStrokeCap(Paint.Cap.ROUND);
        window.setColor(ContextCompat.getColor(context, R.color.instrument_face));
        windowRim.setStyle(Paint.Style.STROKE);
        windowRim.setColor(dimInk);
        readout.setTextAlign(Paint.Align.CENTER);
        readout.setFakeBoldText(true);
        caption.setTextAlign(Paint.Align.CENTER);
        caption.setColor(dimInk);
    }

    public void setUnit(ClimbUnit unit) {
        this.unit = unit;
        invalidate();
    }

    /** @param metresPerSecond the climb rate; negative when sinking */
    public void setClimbRate(double metresPerSecond) {
        this.metresPerSecond = metresPerSecond;
        invalidate();
    }

    @Override
    void drawFace(Canvas canvas, float size) {
        float center = size / 2f;
        float radius = size / 2f;
        drawSector(canvas, center, radius);
        drawBands(canvas, center, radius);
        drawScale(canvas, center, radius);
        drawScaleNote(canvas, center, radius);
        drawNeedle(canvas, center, radius);
        drawWindow(canvas, center, radius);
    }

    /** The share of the scale the climb rate covers, from -1 at the bottom of the sink side to 1 at the top. */
    private double scaleShare() {
        double share = unit.fromMetresPerSecond(metresPerSecond) / unit.scaleEnd();
        return Math.max(-1, Math.min(1, share));
    }

    private float angleOf(double share) {
        return ZERO_ANGLE + SIDE_SWEEP * (float) share;
    }

    private void drawSector(Canvas canvas, float center, float radius) {
        double share = scaleShare();
        float inner = radius * RIM_FRACTION * 0.86f;
        arc.set(center - inner, center - inner, center + inner, center + inner);
        sector.setColor(share >= 0 ? climbColor : sinkColor);
        sector.setAlpha(70);
        float start = share >= 0 ? ZERO_ANGLE : angleOf(share);
        canvas.drawArc(arc, start, Math.abs(SIDE_SWEEP * (float) share), true, sector);
    }

    private void drawBands(Canvas canvas, float center, float radius) {
        float ring = radius * RIM_FRACTION;
        arc.set(center - ring, center - ring, center + ring, center + ring);
        band.setStrokeWidth(radius * 0.035f);
        band.setColor(climbColor);
        canvas.drawArc(arc, ZERO_ANGLE, SIDE_SWEEP, false, band);
        band.setColor(sinkColor);
        canvas.drawArc(arc, ZERO_ANGLE - SIDE_SWEEP, SIDE_SWEEP, false, band);
    }

    private void drawScale(Canvas canvas, float center, float radius) {
        int steps = ClimbUnit.SCALE_STEPS * 2;
        float outer = radius * RIM_FRACTION - radius * 0.04f;
        label.setTextSize(radius * 0.12f);
        for (int side = -1; side <= 1; side += 2) {
            for (int i = (side == -1 ? 1 : 0); i <= steps; i++) {
                boolean whole = i % 2 == 0;
                float angle = angleOf(side * (double) i / steps);
                tick.setStrokeWidth(radius * (whole ? 0.03f : 0.015f));
                canvas.save();
                canvas.rotate(angle, center, center);
                canvas.drawLine(center + outer - radius * (whole ? 0.1f : 0.05f), center, center + outer, center, tick);
                canvas.restore();
                if (whole && i > 0) {
                    drawLabel(canvas, center, radius, angle, i / 2);
                }
            }
        }
    }

    private void drawLabel(Canvas canvas, float center, float radius, float angle, int step) {
        double radians = Math.toRadians(angle);
        float labelRadius = radius * 0.68f;
        canvas.drawText(unit.scaleLabel(step), center + (float) Math.cos(radians) * labelRadius,
                center + (float) Math.sin(radians) * labelRadius + label.getTextSize() / 3f, label);
    }

    private void drawScaleNote(Canvas canvas, float center, float radius) {
        caption.setTextSize(radius * 0.09f);
        canvas.drawText(unit.scaleNote(), center, center + radius * 0.4f, caption);
    }

    private void drawNeedle(Canvas canvas, float center, float radius) {
        needle.setStrokeWidth(radius * 0.045f);
        canvas.save();
        canvas.rotate(angleOf(scaleShare()), center, center);
        canvas.drawLine(center - radius * 0.12f, center, center + radius * 0.45f, center, needle);
        canvas.restore();
        canvas.drawCircle(center, center, radius * 0.07f, needle);
    }

    private void drawWindow(Canvas canvas, float center, float radius) {
        float windowRadius = radius * 0.34f;
        float x = center + radius * 0.5f;
        canvas.drawCircle(x, center, windowRadius, window);
        windowRim.setStrokeWidth(radius * 0.012f);
        canvas.drawCircle(x, center, windowRadius, windowRim);

        caption.setTextSize(radius * 0.11f);
        canvas.drawText(unit.symbol(), x, center - windowRadius * 0.4f, caption);
        readout.setTextSize(radius * 0.24f);
        readout.setColor(metresPerSecond >= 0 ? climbColor : sinkColor);
        canvas.drawText(String.format(Locale.US, "%." + unit.decimals() + "f", unit.fromMetresPerSecond(metresPerSecond)),
                x, center + windowRadius * 0.4f, readout);
    }
}
