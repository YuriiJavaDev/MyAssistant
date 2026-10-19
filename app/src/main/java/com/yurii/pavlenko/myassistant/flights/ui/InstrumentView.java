package com.yurii.pavlenko.myassistant.flights.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.yurii.pavlenko.myassistant.R;
import com.yurii.pavlenko.myassistant.ui.theme.ThemeColors;
import com.yurii.pavlenko.myassistant.ui.widget.Bezel;

/** A round instrument: a dark face inside the riveted frame. Subclasses draw what is on the face. */
abstract class InstrumentView extends View {

    final int ink;
    final int dimInk;
    final int accent;
    private final Bezel bezel;
    private final Paint face = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path glass = new Path();

    InstrumentView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        bezel = new Bezel(context);
        face.setColor(ContextCompat.getColor(context, R.color.instrument_face));
        ink = ContextCompat.getColor(context, R.color.instrument_ink);
        dimInk = ContextCompat.getColor(context, R.color.instrument_ink_dim);
        accent = ThemeColors.get(context, R.attr.maButtonLabel);
    }

    /** Draws the markings and the readout; the face is a square of the given size, centred on its middle. */
    abstract void drawFace(Canvas canvas, float size);

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int size = MeasureSpec.getSize(widthMeasureSpec);
        setMeasuredDimension(size, size);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        float size = Math.min(getWidth(), getHeight());
        float rimWidth = Bezel.rimWidth(size);
        float glassSize = size - 2 * rimWidth;

        canvas.save();
        glass.reset();
        glass.addCircle(size / 2f, size / 2f, glassSize / 2f, Path.Direction.CW);
        canvas.clipPath(glass);
        canvas.translate(rimWidth, rimWidth);
        canvas.drawCircle(glassSize / 2f, glassSize / 2f, glassSize / 2f, face);
        drawFace(canvas, glassSize);
        canvas.restore();

        bezel.draw(canvas, size);
    }
}
