package com.yurii.pavlenko.myassistant.weather.ui.scene;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;

import com.yurii.pavlenko.myassistant.weather.logic.Crowd;
import com.yurii.pavlenko.myassistant.weather.logic.Outfit;

/**
 * People on the beach who are not lying down: couples and dog walkers strolling along the surf,
 * or people with umbrellas who leave the beach in the rain.
 */
final class PromenadeLayer implements Layer {

    private static final float PERSON_HEIGHT = 0.1f;
    private static final float DOG_SIZE = 0.04f;
    private static final float STRIDE_SPEED = 6f;
    private static final float WALK_MIN_X = 0.2f;
    private static final float WALK_MAX_X = 0.8f;
    private static final float COUPLE_GAP = 0.05f;
    private static final int[] UMBRELLAS = {0xFFE53935, 0xFFFFC107, 0xFF1E88E5};

    private final PersonPainter people = new PersonPainter();
    private final DogPainter dog = new DogPainter();
    private final Paint heart = new Paint();
    private final Path heartShape = new Path();
    private final Crowd crowd;
    private final Outfit outfit;

    PromenadeLayer(Crowd crowd, Outfit outfit) {
        this.crowd = crowd;
        this.outfit = outfit;
        heart.setColor(0xFFE91E63);
    }

    @Override
    public void draw(Canvas canvas, float seconds) {
        if (crowd == Crowd.UMBRELLA_WALKERS) {
            drawLeavingInRain(canvas, seconds);
        } else {
            drawCouple(canvas, seconds);
            drawDogWalker(canvas, seconds);
        }
    }

    private void drawCouple(Canvas canvas, float seconds) {
        float u = seconds / 34f;
        float x = pingPong(u);
        int facing = facing(u);
        float y = 0.79f;
        float stride = seconds * STRIDE_SPEED;
        people.draw(canvas, x, y, PERSON_HEIGHT, outfit, 0, stride, facing, 0);
        people.draw(canvas, x + COUPLE_GAP * facing, y, PERSON_HEIGHT * 0.93f, outfit, 1, stride + 0.5f, facing, 0);
        drawHeart(canvas, x + COUPLE_GAP * 0.5f * facing, y - PERSON_HEIGHT * 1.25f, seconds);
    }

    private void drawDogWalker(Canvas canvas, float seconds) {
        float u = seconds / 26f + 0.55f;
        float x = pingPong(u);
        int facing = facing(u);
        float y = 0.845f;
        float stride = seconds * STRIDE_SPEED;
        people.draw(canvas, x, y, PERSON_HEIGHT * 1.05f, outfit, 2, stride, facing, 0);
        dog.draw(canvas, x + 0.11f * facing, y, DOG_SIZE, stride * 1.4f, facing);
    }

    private void drawLeavingInRain(Canvas canvas, float seconds) {
        for (int i = 0; i < 3; i++) {
            float progress = Noise.fraction(seconds / 28f + i / 3f);
            float x = -0.1f + progress * 1.2f;
            float y = 0.78f + 0.035f * i;
            people.draw(canvas, x, y, PERSON_HEIGHT, outfit, i, seconds * STRIDE_SPEED + i, 1, UMBRELLAS[i % UMBRELLAS.length]);
        }
    }

    private void drawHeart(Canvas canvas, float x, float y, float seconds) {
        float beat = 1 + 0.2f * (float) Math.max(0, Math.sin(seconds * 3.2f));
        float size = 0.012f * beat;
        heartShape.reset();
        heartShape.moveTo(x, y + size);
        heartShape.quadTo(x - size * 2.2f, y - size * 0.6f, x, y - size * 0.9f);
        heartShape.quadTo(x + size * 2.2f, y - size * 0.6f, x, y + size);
        heartShape.close();
        canvas.drawPath(heartShape, heart);
    }

    /** Position of someone who walks to one end of the promenade and back. */
    private static float pingPong(float u) {
        float t = Noise.fraction(u);
        float along = 1 - Math.abs(2 * t - 1);
        return WALK_MIN_X + (WALK_MAX_X - WALK_MIN_X) * along;
    }

    private static int facing(float u) {
        return Noise.fraction(u) < 0.5f ? 1 : -1;
    }
}
