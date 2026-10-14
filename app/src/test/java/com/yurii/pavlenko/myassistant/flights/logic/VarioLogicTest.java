package com.yurii.pavlenko.myassistant.flights.logic;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class VarioLogicTest {

    private static final double PRESSURE_PER_METRE = 0.12;

    private static double pressureAt(double metres) {
        return 1013.25 - PRESSURE_PER_METRE * metres;
    }

    @Test
    public void barometerFollowsASteadyClimbWithinTwoSeconds() {
        BaroVario vario = new BaroVario();
        for (int i = 0; i <= 100; i++) {
            long millis = i * 50L;
            vario.add(millis, pressureAt(2.0 * millis / 1000));
        }
        assertEquals(2.0, vario.metresPerSecond(), 0.25);
    }

    @Test
    public void barometerReadsZeroInStillAir() {
        BaroVario vario = new BaroVario();
        for (int i = 0; i <= 100; i++) {
            vario.add(i * 50L, pressureAt(300));
        }
        assertEquals(0, vario.metresPerSecond(), 0.05);
        assertEquals(BaroVario.altitudeOf(pressureAt(300)), vario.altitudeMetres(), 0.5);
    }

    @Test
    public void standardAtmosphereGivesSeaLevelAndKnownHeights() {
        assertEquals(0, BaroVario.altitudeOf(1013.25), 1e-9);
        assertEquals(1000, BaroVario.altitudeOf(898.7), 15);
    }

    @Test
    public void altitudeEstimateTakesChangesFromTheBarometerAndLevelFromGps() {
        AltitudeEstimate estimate = new AltitudeEstimate();
        estimate.onBarometer(100);
        estimate.onGps(300);
        assertEquals(300, estimate.metres(), 1e-9);
        estimate.onBarometer(110);
        assertEquals(310, estimate.metres(), 1e-9);
        for (int i = 0; i < 200; i++) {
            estimate.onGps(250);
        }
        assertEquals(250, estimate.metres(), 1);
    }

    @Test
    public void altitudeEstimateWorksFromGpsAlone() {
        AltitudeEstimate estimate = new AltitudeEstimate();
        assertEquals(0, estimate.metres(), 1e-9);
        estimate.onGps(500);
        assertEquals(500, estimate.metres(), 1e-9);
    }

    @Test
    public void tonesFollowTheClimbAndSinkRates() {
        VarioSoundSettings settings = VarioSoundSettings.defaults();
        assertEquals(VarioTone.Kind.SILENT, VarioTone.of(0, settings).getKind());
        assertEquals(VarioTone.Kind.SILENT, VarioTone.of(0.1, settings).getKind());
        assertEquals(VarioTone.Kind.SILENT, VarioTone.of(-0.9, settings).getKind());

        VarioTone slow = VarioTone.of(0.5, settings);
        VarioTone fast = VarioTone.of(4, settings);
        assertEquals(VarioTone.Kind.BEEP, slow.getKind());
        assertTrue(fast.getFrequencyHz() > slow.getFrequencyHz());
        assertTrue(fast.getPeriodSeconds() < slow.getPeriodSeconds());

        VarioTone sink = VarioTone.of(-1.5, settings);
        assertEquals(VarioTone.Kind.BEEP, sink.getKind());
        assertTrue(sink.getFrequencyHz() < slow.getFrequencyHz());
        assertEquals(VarioTone.Kind.RASP, VarioTone.of(-2.5, settings).getKind());
    }

    @Test
    public void thresholdsCanBeTunedAndStayConsistent() {
        VarioSoundSettings tuned = new VarioSoundSettings(true, 0.5, 2.0, 3.0);
        assertEquals(VarioTone.Kind.SILENT, VarioTone.of(0.4, tuned).getKind());
        assertEquals(VarioTone.Kind.BEEP, VarioTone.of(0.5, tuned).getKind());
        assertEquals(VarioTone.Kind.SILENT, VarioTone.of(-1.5, tuned).getKind());
        assertEquals(VarioTone.Kind.RASP, VarioTone.of(-3.2, tuned).getKind());

        VarioSoundSettings inverted = new VarioSoundSettings(true, 5, 2.5, 1.0);
        assertEquals(VarioSoundSettings.CLIMB_START_MAX, inverted.getClimbStart(), 1e-9);
        assertEquals(2.5, inverted.getSinkAlarm(), 1e-9);
    }

    @Test
    public void synthBeepsInsideTheBeatAndIsSilentOutsideIt() {
        int rate = 8000;
        ToneSynth synth = new ToneSynth(rate);
        VarioTone tone = VarioTone.of(1, VarioSoundSettings.defaults());
        short[] buffer = new short[rate];
        synth.fill(buffer, rate, tone);
        int beepEnd = (int) (tone.getBeepSeconds() * rate);
        int periodEnd = (int) (tone.getPeriodSeconds() * rate);
        assertTrue(peak(buffer, 0, beepEnd) > 5000);
        assertEquals(0, peak(buffer, beepEnd + 10, periodEnd - 10));
        assertTrue(peak(buffer, periodEnd + 10, periodEnd + beepEnd - 10) > 5000);
    }

    @Test
    public void synthIsSilentForSilenceAndLoudForTheAlarm() {
        ToneSynth synth = new ToneSynth(8000);
        short[] buffer = new short[800];
        synth.fill(buffer, 800, VarioTone.SILENT);
        assertEquals(0, peak(buffer, 0, 800));
        synth.fill(buffer, 800, VarioTone.of(-5, VarioSoundSettings.defaults()));
        assertTrue(peak(buffer, 0, 800) > 20000);
    }

    @Test
    public void readsPitchAndRollFromTheRotationMatrix() {
        float[] flatLevel = {1, 0, 0, 0, 1, 0, 0, 0, 1};
        Orientation level = Orientation.fromRotationMatrix(flatLevel);
        assertEquals(0, level.getPitch(), 1e-6);
        assertEquals(0, level.getRoll(), 1e-6);

        float s = (float) Math.sin(Math.toRadians(20));
        float c = (float) Math.cos(Math.toRadians(20));
        float[] flatTopUp = {1, 0, 0, 0, c, -s, 0, s, c};
        assertEquals(20, Orientation.fromRotationMatrix(flatTopUp).getPitch(), 1e-3);

        float[] flatRightDown = {c, 0, s, 0, 1, 0, -s, 0, c};
        assertEquals(20, Orientation.fromRotationMatrix(flatRightDown).getRoll(), 1e-3);

        float[] uprightLevel = {1, 0, 0, 0, 0, -1, 0, 1, 0};
        Orientation upright = Orientation.fromRotationMatrix(uprightLevel);
        assertEquals(0, upright.getPitch(), 1e-6);
        assertEquals(0, upright.getRoll(), 1e-6);

        float[] uprightCameraUp = {1, 0, 0, 0, -s, -c, 0, c, -s};
        assertEquals(20, Orientation.fromRotationMatrix(uprightCameraUp).getPitch(), 1e-3);
    }

    @Test
    public void restartingTheAltitudeCountsTheHighPointAgain() {
        FlightStats stats = FlightStats.empty().plus(new FlightPoint(0, 32, 34.8, 400, 5), 1);
        assertEquals(400, stats.getMaxAltitude(), 1e-9);
        stats = stats.withAltitudeRestarted(0);
        assertEquals(0, stats.getMaxAltitude(), 1e-9);
        stats = stats.plus(new FlightPoint(5000, 32, 34.8, 120, 5), 1);
        assertEquals(120, stats.getMaxAltitude(), 1e-9);
    }

    @Test
    public void climbUnitsLayOutTheScale() {
        assertEquals(8, ClimbUnit.METRES_PER_SECOND.scaleEnd(), 1e-9);
        assertEquals(1600, ClimbUnit.FEET_PER_MINUTE.scaleEnd(), 1e-9);
        assertEquals(196.85, ClimbUnit.FEET_PER_MINUTE.fromMetresPerSecond(1), 1e-2);
    }

    private static int peak(short[] samples, int from, int to) {
        int peak = 0;
        for (int i = from; i < to; i++) {
            peak = Math.max(peak, Math.abs(samples[i]));
        }
        return peak;
    }
}
