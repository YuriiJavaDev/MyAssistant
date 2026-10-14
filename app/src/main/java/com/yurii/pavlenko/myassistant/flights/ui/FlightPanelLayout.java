package com.yurii.pavlenko.myassistant.flights.ui;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

/** The panel of round instruments: taller than wide, and sized from whichever side is given. */
public class FlightPanelLayout extends ConstraintLayout {

    private static final float HEIGHT_PER_WIDTH = 1.45f;

    public FlightPanelLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        boolean heightGiven = MeasureSpec.getMode(heightMeasureSpec) == MeasureSpec.EXACTLY;
        boolean widthGiven = MeasureSpec.getMode(widthMeasureSpec) == MeasureSpec.EXACTLY;
        int width;
        int height;
        if (heightGiven && !widthGiven) {
            height = MeasureSpec.getSize(heightMeasureSpec);
            width = Math.round(height / HEIGHT_PER_WIDTH);
        } else {
            width = MeasureSpec.getSize(widthMeasureSpec);
            height = Math.round(width * HEIGHT_PER_WIDTH);
        }
        super.onMeasure(MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY));
    }
}
