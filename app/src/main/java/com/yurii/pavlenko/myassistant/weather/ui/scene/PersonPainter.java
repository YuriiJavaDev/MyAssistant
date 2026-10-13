package com.yurii.pavlenko.myassistant.weather.ui.scene;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;

import com.yurii.pavlenko.myassistant.weather.logic.Outfit;

/** Draws a small cartoon person whose clothes depend on how cold it is. */
final class PersonPainter {

    private static final int[] LIGHT_TOPS = {0xFFE53935, 0xFF1E88E5, 0xFFFDD835, 0xFF43A047};
    private static final int[] JACKETS = {0xFF1565C0, 0xFF6A1B9A, 0xFF2E7D32, 0xFFEF6C00};
    private static final int[] COATS = {0xFF5D4037, 0xFF455A64, 0xFF7B1FA2, 0xFF283593};
    private static final int[] FURS = {0xFF6D4C41, 0xFF8D6E63, 0xFF9E9E9E, 0xFF4E342E};
    private static final int[] SCARVES = {0xFFE53935, 0xFFFFC107, 0xFF26A69A, 0xFFEC407A};
    private static final int SKIN = 0xFFF1C27D;
    private static final int JEANS = 0xFF37474F;
    private static final int BOOTS = 0xFF212121;
    private static final int FUR_HAT = 0xFF3E2723;

    private final Paint fill = new Paint();
    private final Paint limb = new Paint();
    private final Path shape = new Path();

    PersonPainter() {
        limb.setStyle(Paint.Style.STROKE);
        limb.setStrokeCap(Paint.Cap.ROUND);
    }

    /**
     * @param height  body height in unit-square units
     * @param stride  walking phase in radians
     * @param facing  1 to face right, -1 to face left
     * @param umbrella colour of an umbrella held overhead, or 0 for none
     */
    void draw(Canvas canvas, float x, float baseY, float height, Outfit outfit, int variant,
              float stride, int facing, int umbrella) {
        canvas.save();
        canvas.scale(facing, 1, x, baseY);
        float swing = 0.14f * height * (float) Math.sin(stride);
        float hip = baseY - 0.42f * height;
        float shoulder = baseY - 0.76f * height;

        drawLegs(canvas, x, hip, baseY, height, swing, outfit);
        drawBody(canvas, x, shoulder, hip, height, outfit, variant);
        drawArms(canvas, x, shoulder, height, swing, outfit, variant, umbrella != 0);
        drawHead(canvas, x, baseY - 0.9f * height, height, outfit, variant);
        if (umbrella != 0) {
            drawUmbrella(canvas, x, baseY, height, umbrella);
        }
        canvas.restore();
    }

    private void drawLegs(Canvas canvas, float x, float hip, float baseY, float height, float swing, Outfit outfit) {
        limb.setStrokeWidth(0.1f * height);
        limb.setColor(outfit == Outfit.LIGHT ? SKIN : outfit == Outfit.FUR || outfit == Outfit.COAT ? BOOTS : JEANS);
        canvas.drawLine(x, hip, x + swing, baseY, limb);
        canvas.drawLine(x, hip, x - swing, baseY, limb);
    }

    private void drawBody(Canvas canvas, float x, float shoulder, float hip, float height, Outfit outfit, int variant) {
        float half = 0.15f * height;
        float bottom = hip;
        int color;
        switch (outfit) {
            case FUR:
                half = 0.21f * height;
                bottom = hip + 0.18f * height;
                color = FURS[variant % FURS.length];
                break;
            case COAT:
                bottom = hip + 0.14f * height;
                color = COATS[variant % COATS.length];
                break;
            case JACKET:
                color = JACKETS[variant % JACKETS.length];
                break;
            default:
                color = LIGHT_TOPS[variant % LIGHT_TOPS.length];
                break;
        }
        fill.setColor(color);
        canvas.drawOval(x - half, shoulder - 0.04f * height, x + half, bottom + 0.02f * height, fill);
        canvas.drawRect(x - half, shoulder + 0.05f * height, x + half, bottom, fill);
    }

    private void drawArms(Canvas canvas, float x, float shoulder, float height, float swing, Outfit outfit,
                          int variant, boolean holdsUmbrella) {
        limb.setStrokeWidth(0.08f * height);
        limb.setColor(outfit == Outfit.LIGHT ? SKIN : sleeve(outfit, variant));
        float reach = 0.3f * height;
        canvas.drawLine(x, shoulder + 0.03f * height, x - swing, shoulder + reach, limb);
        if (holdsUmbrella) {
            canvas.drawLine(x, shoulder + 0.03f * height, x + 0.2f * height, shoulder - 0.05f * height, limb);
        } else {
            canvas.drawLine(x, shoulder + 0.03f * height, x + swing, shoulder + reach, limb);
        }
    }

    private static int sleeve(Outfit outfit, int variant) {
        switch (outfit) {
            case FUR: return FURS[variant % FURS.length];
            case COAT: return COATS[variant % COATS.length];
            default: return JACKETS[variant % JACKETS.length];
        }
    }

    private void drawHead(Canvas canvas, float x, float centerY, float height, Outfit outfit, int variant) {
        float radius = 0.11f * height;
        fill.setColor(SKIN);
        canvas.drawCircle(x, centerY, radius, fill);
        switch (outfit) {
            case FUR:
                fill.setColor(FUR_HAT);
                canvas.drawOval(x - radius * 1.35f, centerY - radius * 1.3f, x + radius * 1.35f, centerY + radius * 0.2f, fill);
                canvas.drawRect(x - radius * 1.35f, centerY - radius * 0.3f, x - radius * 0.95f, centerY + radius * 1.1f, fill);
                canvas.drawRect(x + radius * 0.95f, centerY - radius * 0.3f, x + radius * 1.35f, centerY + radius * 1.1f, fill);
                break;
            case COAT:
                fill.setColor(SCARVES[variant % SCARVES.length]);
                canvas.drawOval(x - radius, centerY - radius * 1.1f, x + radius, centerY + radius * 0.1f, fill);
                canvas.drawRect(x - radius * 0.9f, centerY + radius * 0.85f, x + radius * 0.9f, centerY + radius * 1.5f, fill);
                break;
            default:
                fill.setColor(0xFF5D4037);
                canvas.drawOval(x - radius, centerY - radius * 1.1f, x + radius, centerY + radius * 0.05f, fill);
                break;
        }
    }

    private void drawUmbrella(Canvas canvas, float x, float baseY, float height, int color) {
        float handX = x + 0.2f * height;
        float top = baseY - 1.28f * height;
        limb.setStrokeWidth(0.02f * height);
        limb.setColor(0xFF424242);
        canvas.drawLine(handX, baseY - 0.8f * height, handX, top, limb);
        shape.reset();
        shape.moveTo(handX - 0.42f * height, top + 0.1f * height);
        shape.quadTo(handX, top - 0.28f * height, handX + 0.42f * height, top + 0.1f * height);
        shape.close();
        fill.setColor(color);
        canvas.drawPath(shape, fill);
    }
}
