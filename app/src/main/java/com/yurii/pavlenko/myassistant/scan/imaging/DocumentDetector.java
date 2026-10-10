package com.yurii.pavlenko.myassistant.scan.imaging;

/**
 * Finds the page in a photo: the largest bright region that stands out from the darker surroundings.
 * Works for a light sheet on a darker surface; for anything else it returns a safe default
 * and the user adjusts the corners by hand.
 */
public final class DocumentDetector {

    private static final int ANALYSIS_SIDE = 320;
    private static final double MIN_AREA_FRACTION = 0.15;
    private static final double MAX_REGION_FRACTION = 0.92;
    private static final double FALLBACK_MARGIN = 0.05;

    private DocumentDetector() {
    }

    public static Quad detect(PixelImage source) {
        PixelImage small = source.downscaledTo(ANALYSIS_SIDE);
        int width = small.getWidth();
        int height = small.getHeight();

        float[] smooth = BoxBlur.apply(small.luminance(), width, height, 2);
        boolean[] bright = threshold(smooth, otsuThreshold(smooth));
        boolean[] page = largestRegion(bright, width, height);

        int regionSize = count(page);
        double regionFraction = (double) regionSize / (width * height);
        if (regionFraction < MIN_AREA_FRACTION || regionFraction > MAX_REGION_FRACTION) {
            return Quad.inset(FALLBACK_MARGIN);
        }

        Quad found = extremeCorners(page, width, height);
        return found.isConvex() && found.area() >= MIN_AREA_FRACTION ? found : Quad.inset(FALLBACK_MARGIN);
    }

    private static Quad extremeCorners(boolean[] region, int width, int height) {
        int[] best = new int[4];
        double[] score = {Double.MAX_VALUE, -Double.MAX_VALUE, -Double.MAX_VALUE, Double.MAX_VALUE};
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (!region[y * width + x]) {
                    continue;
                }
                double sum = x + y;
                double diff = x - y;
                if (sum < score[0]) { score[0] = sum; best[0] = y * width + x; }
                if (diff > score[1]) { score[1] = diff; best[1] = y * width + x; }
                if (sum > score[2]) { score[2] = sum; best[2] = y * width + x; }
                if (diff < score[3]) { score[3] = diff; best[3] = y * width + x; }
            }
        }
        double[] xs = new double[4];
        double[] ys = new double[4];
        for (int i = 0; i < 4; i++) {
            xs[i] = ((best[i] % width) + 0.5) / width;
            ys[i] = ((best[i] / width) + 0.5) / height;
        }
        return new Quad(xs, ys);
    }

    private static int otsuThreshold(float[] luminance) {
        int[] histogram = new int[256];
        for (float value : luminance) {
            histogram[Math.min(255, Math.max(0, (int) value))]++;
        }
        long total = luminance.length;
        double sumAll = 0;
        for (int i = 0; i < 256; i++) {
            sumAll += (double) i * histogram[i];
        }
        double sumBackground = 0;
        long weightBackground = 0;
        double bestVariance = -1;
        int threshold = 128;
        for (int i = 0; i < 256; i++) {
            weightBackground += histogram[i];
            if (weightBackground == 0) {
                continue;
            }
            long weightForeground = total - weightBackground;
            if (weightForeground == 0) {
                break;
            }
            sumBackground += (double) i * histogram[i];
            double meanBackground = sumBackground / weightBackground;
            double meanForeground = (sumAll - sumBackground) / weightForeground;
            double variance = (double) weightBackground * weightForeground
                    * (meanBackground - meanForeground) * (meanBackground - meanForeground);
            if (variance > bestVariance) {
                bestVariance = variance;
                threshold = i;
            }
        }
        return threshold;
    }

    private static boolean[] threshold(float[] luminance, int threshold) {
        boolean[] result = new boolean[luminance.length];
        for (int i = 0; i < luminance.length; i++) {
            result[i] = luminance[i] > threshold;
        }
        return result;
    }

    private static boolean[] largestRegion(boolean[] mask, int width, int height) {
        int[] label = new int[mask.length];
        int[] stack = new int[mask.length];
        int bestLabel = 0;
        int bestSize = 0;
        int nextLabel = 0;

        for (int start = 0; start < mask.length; start++) {
            if (!mask[start] || label[start] != 0) {
                continue;
            }
            nextLabel++;
            int size = 0;
            int top = 0;
            stack[top++] = start;
            label[start] = nextLabel;
            while (top > 0) {
                int index = stack[--top];
                size++;
                int x = index % width;
                int y = index / width;
                if (x > 0 && mask[index - 1] && label[index - 1] == 0) { label[index - 1] = nextLabel; stack[top++] = index - 1; }
                if (x < width - 1 && mask[index + 1] && label[index + 1] == 0) { label[index + 1] = nextLabel; stack[top++] = index + 1; }
                if (y > 0 && mask[index - width] && label[index - width] == 0) { label[index - width] = nextLabel; stack[top++] = index - width; }
                if (y < height - 1 && mask[index + width] && label[index + width] == 0) { label[index + width] = nextLabel; stack[top++] = index + width; }
            }
            if (size > bestSize) {
                bestSize = size;
                bestLabel = nextLabel;
            }
        }

        boolean[] region = new boolean[mask.length];
        for (int i = 0; i < mask.length; i++) {
            region[i] = bestLabel != 0 && label[i] == bestLabel;
        }
        return region;
    }

    private static int count(boolean[] mask) {
        int total = 0;
        for (boolean value : mask) {
            if (value) {
                total++;
            }
        }
        return total;
    }
}
