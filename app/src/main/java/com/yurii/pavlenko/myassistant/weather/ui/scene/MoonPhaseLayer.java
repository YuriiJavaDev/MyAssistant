package com.yurii.pavlenko.myassistant.weather.ui.scene;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RadialGradient;
import android.graphics.Shader;

import com.yurii.pavlenko.myassistant.weather.logic.MoonPhase;

/** The moon as it looks tonight: the lit part, the dim rest of the disc, and a glow that breathes. */
final class MoonPhaseLayer implements Layer {

    private static final float CENTER = 0.5f;
    private static final float RADIUS = 0.27f;
    private static final int OUTLINE_POINTS = 48;
    private final Paint glow = new Paint();
    private final Paint dark = new Paint();
    private final Paint lit = new Paint();
    private final MoonSurface surface = new MoonSurface(CENTER, CENTER, RADIUS);
    private final Path litPart = new Path();
    private final float illumination;
    private final boolean mirrored;

    /** In the southern hemisphere the moon is seen upside down, which mirrors the lit side. */
    MoonPhaseLayer(MoonPhase phase, boolean southernHemisphere) {
        this.mirrored = southernHemisphere;
        this.illumination = (float) phase.illumination();
        dark.setColor(0xFF262E4A);
        lit.setColor(0xFFF4ECC9);
        glow.setShader(new RadialGradient(CENTER, CENTER, RADIUS * 2.2f,
                new int[]{0x66FFF6D6, 0x00FFF6D6}, null, Shader.TileMode.CLAMP));
        buildLitPart(phase);
    }

    /** The lit part is bounded by the moon's edge on one side and the terminator, half an ellipse, on the other. */
    private void buildLitPart(MoonPhase phase) {
        float side = phase.isWaxing() ? 1 : -1;
        float terminator = side * (float) Math.cos(2 * Math.PI * phase.cycle());
        litPart.reset();
        for (int i = 0; i <= OUTLINE_POINTS; i++) {
            double angle = -Math.PI / 2 + Math.PI * i / OUTLINE_POINTS;
            float x = CENTER + side * RADIUS * (float) Math.cos(angle);
            float y = CENTER + RADIUS * (float) Math.sin(angle);
            if (i == 0) {
                litPart.moveTo(x, y);
            } else {
                litPart.lineTo(x, y);
            }
        }
        for (int i = OUTLINE_POINTS; i >= 0; i--) {
            double angle = -Math.PI / 2 + Math.PI * i / OUTLINE_POINTS;
            litPart.lineTo(CENTER + terminator * RADIUS * (float) Math.cos(angle),
                    CENTER + RADIUS * (float) Math.sin(angle));
        }
        litPart.close();
    }

    @Override
    public void draw(Canvas canvas, float seconds) {
        float breath = 1 + 0.05f * (float) Math.sin(seconds * 1.1);
        float bob = 0.006f * (float) Math.sin(seconds * 0.6);

        canvas.save();
        canvas.translate(0, bob);
        if (mirrored) {
            canvas.scale(-1, 1, CENTER, CENTER);
        }
        glow.setAlpha((int) (255 * (0.25f + 0.75f * illumination)));
        canvas.drawCircle(CENTER, CENTER, RADIUS * 2.2f * breath, glow);
        canvas.drawCircle(CENTER, CENTER, RADIUS, dark);
        canvas.drawPath(litPart, lit);

        canvas.save();
        canvas.clipPath(litPart);
        surface.draw(canvas, CENTER, CENTER, RADIUS);
        canvas.restore();
        canvas.restore();
    }
}
