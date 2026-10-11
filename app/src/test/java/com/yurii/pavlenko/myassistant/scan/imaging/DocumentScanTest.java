package com.yurii.pavlenko.myassistant.scan.imaging;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class DocumentScanTest {

    private static final int MAX_SIDE = 1400;

    @Test
    public void detectorFindsTheTiltedSheet() {
        Quad found = DocumentDetector.detect(SyntheticPhoto.create());
        Quad expected = SyntheticPhoto.pageCorners();

        assertTrue(found.isConvex());
        for (int i = 0; i < 4; i++) {
            assertEquals("corner x " + i, expected.x(i), found.x(i), 0.03);
            assertEquals("corner y " + i, expected.y(i), found.y(i), 0.03);
        }
    }

    @Test
    public void detectorFallsBackOnFlatImage() {
        Quad found = DocumentDetector.detect(PixelImage.blank(400, 300, Colors.rgb(120, 120, 120)));

        assertEquals(0.05, found.x(0), 1e-9);
        assertEquals(0.95, found.x(2), 1e-9);
    }

    @Test
    public void warpOfWholeImageKeepsPixels() {
        PixelImage source = SyntheticPhoto.create();
        PixelImage warped = PerspectiveWarp.warp(source, Quad.inset(0), source.getWidth(), source.getHeight());

        assertEquals(source.get(600, 450), warped.get(600, 450));
        assertEquals(source.get(300, 200), warped.get(300, 200));
    }

    @Test
    public void warpMapsPageCornersToOutputCorners() {
        PixelImage source = SyntheticPhoto.create();
        PixelImage warped = PerspectiveWarp.warp(source, SyntheticPhoto.pageCorners(), 400, 560);

        int[] pixel = {warped.get(3, 3), warped.get(396, 3), warped.get(396, 556), warped.get(3, 556)};
        for (int argb : pixel) {
            assertTrue("corner must be paper, was " + Integer.toHexString(argb), Colors.luminance(argb) > 100);
        }
    }

    @Test
    public void colorScanHasWhitePaperEvenInTheShadowAndKeepsRedStamp() {
        ScanResult result = scan(ScanFilter.COLOR);
        PixelImage image = result.getImage();

        assertEquals(PaperFormat.A4, result.getPaperFormat());
        assertEquals(Math.sqrt(2), (double) image.getHeight() / image.getWidth(), 0.01);

        assertTrue("left paper", Colors.luminance(paperAt(image, 0.05, 0.60)) > 235);
        assertTrue("right paper (shadowed in the photo)", Colors.luminance(paperAt(image, 0.95, 0.60)) > 235);

        int stamp = paperAt(image, 0.75, 0.16);
        assertTrue("stamp stays red", Colors.red(stamp) > Colors.green(stamp) + 100);
    }

    @Test
    public void deepCornerShadowAndNoiseBecomeEvenWhitePaper() {
        PixelImage photo = SyntheticPhoto.createHarsh();
        PixelImage image = ScanPipeline.process(photo, SyntheticPhoto.pageCorners(), ScanOptions.defaults(), MAX_SIDE).getImage();

        double[][] spots = {{0.12, 0.06}, {0.5, 0.06}, {0.9, 0.06}, {0.05, 0.5}, {0.5, 0.97}, {0.95, 0.97}};
        for (double[] spot : spots) {
            double[] stats = patchStats(image, spot[0], spot[1]);
            assertTrue("paper mean at " + spot[0] + "," + spot[1] + " was " + stats[0], stats[0] > 250);
            assertTrue("paper noise at " + spot[0] + "," + spot[1] + " was " + stats[1], stats[1] < 2.5);
        }
    }

    @Test
    public void brightnessAndContrastMoveTheTones() {
        ScanOptions darker = ScanOptions.defaults().withBrightness(-0.5);
        ScanOptions flat = ScanOptions.defaults().withContrast(-0.5);

        double paperDarker = patchStats(scan(darker).getImage(), 0.5, 0.97)[0];
        double paperNormal = patchStats(scan(ScanOptions.defaults()).getImage(), 0.5, 0.97)[0];
        int inkNormal = darkestInk(scan(ScanOptions.defaults()).getImage());
        int inkFlat = darkestInk(scan(flat).getImage());

        assertTrue("negative brightness darkens paper", paperDarker < paperNormal - 20);
        assertTrue("negative contrast lifts ink", inkFlat > inkNormal + 15);
    }

    @Test
    public void zeroShadowStrengthKeepsTheShadow() {
        PixelImage photo = SyntheticPhoto.createHarsh();
        ScanOptions none = ScanOptions.defaults().withShadowStrength(0);
        PixelImage image = ScanPipeline.process(photo, SyntheticPhoto.pageCorners(), none, MAX_SIDE).getImage();

        double corner = patchStats(image, 0.12, 0.06)[0];
        double far = patchStats(image, 0.9, 0.97)[0];
        assertTrue("shadow stays visible: " + corner + " vs " + far, corner < far - 40);
    }

    @Test
    public void textStaysDark() {
        PixelImage image = scan(ScanFilter.COLOR).getImage();

        int darkest = 255;
        for (int x = (int) (0.12 * image.getWidth()); x < 0.4 * image.getWidth(); x++) {
            for (int y = (int) (0.28 * image.getHeight()); y < 0.5 * image.getHeight(); y++) {
                darkest = Math.min(darkest, (int) Colors.luminance(image.get(x, y)));
            }
        }
        assertTrue("ink must be dark, darkest was " + darkest, darkest < 90);
    }

    @Test
    public void blackWhiteScanContainsOnlyBlackAndWhite() {
        PixelImage image = scan(ScanFilter.BLACK_WHITE).getImage();

        int ink = 0;
        for (int argb : image.getPixels()) {
            assertTrue(argb == 0xFF000000 || argb == 0xFFFFFFFF);
            if (argb == 0xFF000000) {
                ink++;
            }
        }
        double inkShare = (double) ink / image.getPixels().length;
        assertTrue("ink share " + inkShare, inkShare > 0.02 && inkShare < 0.40);
    }

    @Test
    public void grayscaleScanHasNoColor() {
        for (int argb : scan(ScanFilter.GRAYSCALE).getImage().getPixels()) {
            assertEquals(Colors.red(argb), Colors.green(argb));
            assertEquals(Colors.green(argb), Colors.blue(argb));
        }
    }

    @Test
    public void quarterTurnSwapsSidesAndStaysOnPaperFormat() {
        ScanOptions rotated = ScanOptions.defaults().rotatedClockwise();
        ScanResult result = ScanPipeline.process(SyntheticPhoto.create(), SyntheticPhoto.pageCorners(), rotated, MAX_SIDE);

        assertTrue(result.getImage().getWidth() > result.getImage().getHeight());
        assertEquals(PaperFormat.A4, result.getPaperFormat());
    }

    @Test
    public void rotationRoundTripRestoresImage() {
        PixelImage image = SyntheticPhoto.create().downscaledTo(100);

        PixelImage back = image.rotatedClockwise(1).rotatedClockwise(3);

        assertEquals(image.getWidth(), back.getWidth());
        assertEquals(image.get(10, 20), back.get(10, 20));
        assertEquals(image.get(image.getWidth() - 1, image.getHeight() - 1), back.get(image.getWidth() - 1, image.getHeight() - 1));
    }

    @Test
    public void downscaleKeepsAspectAndSmallImagesUntouched() {
        PixelImage image = PixelImage.blank(400, 200, 0xFF112233);

        PixelImage small = image.downscaledTo(100);

        assertEquals(100, small.getWidth());
        assertEquals(50, small.getHeight());
        assertEquals(0xFF112233, small.get(5, 5));
        assertEquals(image, image.downscaledTo(1000));
    }

    @Test
    public void autoFormatSnapsToLetterAndKeepsUnusualShapes() {
        assertEquals(PaperFormat.LETTER, PaperFormat.AUTO.resolve(850, 1100));
        assertEquals(PaperFormat.A4, PaperFormat.AUTO.resolve(1000, 1414));
        assertEquals(PaperFormat.ORIGINAL, PaperFormat.AUTO.resolve(1000, 1000));
        assertEquals(PaperFormat.A5, PaperFormat.A5.resolve(1000, 1000));
    }

    @Test
    public void outputSizeFollowsOrientationAndCapsLongSide() {
        int[] portrait = PaperFormat.A4.outputSize(800, 1100, 1400);
        int[] landscape = PaperFormat.A4.outputSize(1100, 800, 1400);
        int[] capped = PaperFormat.ORIGINAL.outputSize(4000, 3000, 2400);

        assertTrue(portrait[1] > portrait[0]);
        assertTrue(landscape[0] > landscape[1]);
        assertEquals(2400, capped[0]);
        assertEquals(1800, capped[1]);
    }

    @Test
    public void pdfPageMatchesFormatAndOrientation() {
        assertEquals(842, PaperFormat.A4.pdfPageSize(800, 1100)[1]);
        assertEquals(842, PaperFormat.A4.pdfPageSize(1100, 800)[0]);
        assertEquals(595, PaperFormat.ORIGINAL.pdfPageSize(1000, 1000)[0]);
        assertEquals(595, PaperFormat.ORIGINAL.pdfPageSize(1000, 1000)[1]);
    }

    @Test
    public void quadRejectsCrossedCornersAndMovesEdges() {
        Quad crossed = Quad.inset(0.1).withCorner(0, 0.9, 0.9);
        Quad moved = Quad.inset(0.1).withEdgeMoved(0, 0, 0.05);

        assertFalse(crossed.isConvex());
        assertEquals(0.15, moved.y(0), 1e-9);
        assertEquals(0.15, moved.y(1), 1e-9);
        assertEquals(0.9, moved.y(2), 1e-9);
    }

    private static ScanResult scan(ScanFilter filter) {
        return scan(ScanOptions.defaults().withFilter(filter));
    }

    private static ScanResult scan(ScanOptions options) {
        return ScanPipeline.process(SyntheticPhoto.create(), SyntheticPhoto.pageCorners(), options, MAX_SIDE);
    }

    private static int darkestInk(PixelImage image) {
        int darkest = 255;
        for (int x = (int) (0.12 * image.getWidth()); x < 0.4 * image.getWidth(); x++) {
            for (int y = (int) (0.28 * image.getHeight()); y < 0.5 * image.getHeight(); y++) {
                darkest = Math.min(darkest, (int) Colors.luminance(image.get(x, y)));
            }
        }
        return darkest;
    }

    /** Mean and standard deviation of luminance in a small patch around a page-relative position. */
    private static double[] patchStats(PixelImage image, double u, double v) {
        int cx = (int) (u * (image.getWidth() - 1));
        int cy = (int) (v * (image.getHeight() - 1));
        int radius = 12;
        double sum = 0;
        double sumSquares = 0;
        int count = 0;
        for (int y = Math.max(0, cy - radius); y <= Math.min(image.getHeight() - 1, cy + radius); y++) {
            for (int x = Math.max(0, cx - radius); x <= Math.min(image.getWidth() - 1, cx + radius); x++) {
                double value = Colors.luminance(image.get(x, y));
                sum += value;
                sumSquares += value * value;
                count++;
            }
        }
        double mean = sum / count;
        return new double[]{mean, Math.sqrt(Math.max(0, sumSquares / count - mean * mean))};
    }

    /** Pixel of the scan at page-relative position; paper without text is expected there. */
    private static int paperAt(PixelImage image, double u, double v) {
        return image.get((int) (u * (image.getWidth() - 1)), (int) (v * (image.getHeight() - 1)));
    }
}
