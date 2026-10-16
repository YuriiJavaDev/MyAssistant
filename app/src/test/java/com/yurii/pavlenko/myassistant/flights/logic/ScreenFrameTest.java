package com.yurii.pavlenko.myassistant.flights.logic;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class ScreenFrameTest {

    private static final float S = (float) Math.sin(Math.toRadians(20));
    private static final float C = (float) Math.cos(Math.toRadians(20));

    /** The device matrix that, seen through a screen turned by the given quarter turns, equals the screen matrix. */
    private static float[] deviceMatrix(float[] screen, int quarterTurns) {
        float[] device = new float[9];
        for (int row = 0; row < 3; row++) {
            float right = screen[row * 3];
            float top = screen[row * 3 + 1];
            float out = screen[row * 3 + 2];
            float x;
            float y;
            switch (quarterTurns) {
                case 1: x = top; y = -right; break;
                case 2: x = -right; y = -top; break;
                case 3: x = -top; y = right; break;
                default: x = right; y = top; break;
            }
            device[row * 3] = x;
            device[row * 3 + 1] = y;
            device[row * 3 + 2] = out;
        }
        return device;
    }

    @Test
    public void aScreenTurnedSidewaysPointsWithItsTopEdge() {
        // A phone lying face up with its natural top to the east. Turned a quarter counterclockwise, the natural top
        // is on the left of the screen, so the left of the screen is east and the top of the screen is south.
        float[] flatTopEast = {0, 1, 0, -1, 0, 0, 0, 0, 1};
        assertEquals(90, Orientation.fromRotationMatrix(flatTopEast, 0).getHeading(), 1e-3);
        assertEquals(180, Orientation.fromRotationMatrix(flatTopEast, 1).getHeading(), 1e-3);
        assertEquals(270, Orientation.fromRotationMatrix(flatTopEast, 2).getHeading(), 1e-3);
        assertEquals(0, Orientation.fromRotationMatrix(flatTopEast, 3).getHeading(), 1e-3);
    }

    @Test
    public void readsTheSameWhicheverWayTheScreenIsTurned() {
        float[][] screens = {
                {1, 0, 0, 0, 1, 0, 0, 0, 1},
                {1, 0, 0, 0, C, -S, 0, S, C},
                {C, 0, S, 0, 1, 0, -S, 0, C},
                {1, 0, 0, 0, 0, -1, 0, 1, 0},
                {1, 0, 0, 0, -S, -C, 0, C, -S},
        };
        for (float[] screen : screens) {
            Orientation expected = Orientation.fromRotationMatrix(screen, 0);
            for (int turns = 1; turns < 4; turns++) {
                Orientation seen = Orientation.fromRotationMatrix(deviceMatrix(screen, turns), turns);
                assertEquals(expected.getHeading(), seen.getHeading(), 1e-3);
                assertEquals(expected.getPitch(), seen.getPitch(), 1e-3);
                assertEquals(expected.getRoll(), seen.getRoll(), 1e-3);
            }
        }
    }

    @Test
    public void liftingTheFarEdgeOfASidewaysScreenRaisesTheNose() {
        float[] screenTopUp = {1, 0, 0, 0, C, -S, 0, S, C};
        Orientation seen = Orientation.fromRotationMatrix(deviceMatrix(screenTopUp, 1), 1);
        assertEquals(20, seen.getPitch(), 1e-3);
        assertEquals(0, seen.getRoll(), 1e-3);
    }

    @Test
    public void aSidewaysScreenHeldUprightHasTheSkyAbove() {
        // Screen turned a quarter counterclockwise, phone upright, camera looking north. Written from the axes
        // alone, as the columns of the device x, y and z axes in the world (east, north, up): the natural right
        // of the phone is up, its natural top points west and the way out of the screen points south.
        float[] device = {0, -1, 0, 0, 0, -1, 1, 0, 0};
        Orientation seen = Orientation.fromRotationMatrix(device, 1);
        assertEquals(0, seen.getHeading(), 1e-3);
        assertEquals(0, seen.getPitch(), 1e-3);
        assertEquals(0, seen.getRoll(), 1e-3);
    }

    @Test
    public void aSidewaysScreenHeldUprightIsLevel() {
        float[] screenUpright = {1, 0, 0, 0, 0, -1, 0, 1, 0};
        for (int turns = 1; turns < 4; turns++) {
            Orientation seen = Orientation.fromRotationMatrix(deviceMatrix(screenUpright, turns), turns);
            assertEquals(0, seen.getPitch(), 1e-3);
            assertEquals(0, seen.getRoll(), 1e-3);
        }
    }
}
