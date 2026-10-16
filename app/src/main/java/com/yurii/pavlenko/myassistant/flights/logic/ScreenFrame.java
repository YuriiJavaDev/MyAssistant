package com.yurii.pavlenko.myassistant.flights.logic;

/**
 * Re-expresses a rotation matrix in the frame of the screen as the user sees it. The sensors report the phone
 * in its natural, upright frame; with the screen turned sideways its top edge is another edge of the phone.
 */
public final class ScreenFrame {

    private static final int SIZE = 3;

    private ScreenFrame() {
    }

    /**
     * @param matrix the 3x3 rotation matrix from the device to the world, row by row
     * @param quarterTurns how far the screen is turned from natural, counterclockwise, in quarter turns, as
     *                     the display rotation of Android reports it: 0, 1 for 90, 2 for 180 and 3 for 270 degrees
     * @return the matrix from the screen to the world, whose first column is the right of the screen, the second
     * its top and the third the way out of the screen. With the screen turned a quarter counterclockwise
     * the natural top of the phone is on the left of the screen and its natural right is the top.
     */
    public static float[] of(float[] matrix, int quarterTurns) {
        int right;
        int top;
        float rightSign;
        float topSign;
        switch (Math.floorMod(quarterTurns, 4)) {
            case 1:
                right = 1;
                top = 0;
                rightSign = -1;
                topSign = 1;
                break;
            case 2:
                right = 0;
                top = 1;
                rightSign = -1;
                topSign = -1;
                break;
            case 3:
                right = 1;
                top = 0;
                rightSign = 1;
                topSign = -1;
                break;
            default:
                return matrix.clone();
        }
        float[] screen = new float[SIZE * SIZE];
        for (int row = 0; row < SIZE; row++) {
            screen[row * SIZE] = rightSign * matrix[row * SIZE + right];
            screen[row * SIZE + 1] = topSign * matrix[row * SIZE + top];
            screen[row * SIZE + 2] = matrix[row * SIZE + 2];
        }
        return screen;
    }
}
