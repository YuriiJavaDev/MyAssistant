package com.yurii.pavlenko.myassistant.weather.ui.scene;

/** Repeatable pseudo-random numbers, so scattered things stay where they are from frame to frame. */
final class Noise {

    private Noise() {
    }

    /** A value in [0, 1) that depends only on the arguments. */
    static float unit(int index, int salt) {
        double x = Math.sin(index * 12.9898 + salt * 78.233) * 43758.5453;
        return (float) (x - Math.floor(x));
    }

    static float fraction(float value) {
        return value - (float) Math.floor(value);
    }
}
