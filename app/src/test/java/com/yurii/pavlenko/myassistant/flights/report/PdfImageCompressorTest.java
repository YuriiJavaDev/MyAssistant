package com.yurii.pavlenko.myassistant.flights.report;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.Deflater;

import org.junit.Test;

public class PdfImageCompressorTest {

    private static final int SIDE = 16;
    private static final byte[] JPEG = new byte[120];

    static {
        new Random(7).nextBytes(JPEG);
    }

    private static byte[] deflate(byte[] raw) {
        Deflater deflater = new Deflater();
        deflater.setInput(raw);
        deflater.finish();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buffer = new byte[4096];
        while (!deflater.finished()) {
            out.write(buffer, 0, deflater.deflate(buffer));
        }
        deflater.end();
        return out.toByteArray();
    }

    private static byte[] noise(int count, long seed) {
        byte[] data = new byte[count];
        new Random(seed).nextBytes(data);
        return data;
    }

    private static byte[] text(String value) {
        return value.getBytes(StandardCharsets.ISO_8859_1);
    }

    private static void image(ByteArrayOutputStream out, List<Integer> offsets, int number, String colorSpace,
                              String extra, byte[] stream) {
        offsets.add(out.size());
        byte[] head = text(number + " 0 obj\n<</Type /XObject\n/Subtype /Image\n/Width " + SIDE + "\n/Height " + SIDE
                + "\n/ColorSpace " + colorSpace + "\n" + extra + "/BitsPerComponent 8\n/Filter /FlateDecode\n/Length "
                + stream.length + ">> stream\n");
        out.write(head, 0, head.length);
        out.write(stream, 0, stream.length);
        byte[] tail = text("\nendstream\nendobj\n");
        out.write(tail, 0, tail.length);
    }

    /**
     * A file laid out as PdfDocument lays it out: object 1 is the catalog, 2 a photo, 3 a picture with transparency
     * and 4 its mask.
     */
    private static byte[] pdf(byte[] photo) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        List<Integer> offsets = new ArrayList<>();
        byte[] header = text("%PDF-1.4\n%Óëéá\n");
        out.write(header, 0, header.length);
        offsets.add(out.size());
        byte[] catalog = text("1 0 obj\n<</Type /Catalog>>\nendobj\n");
        out.write(catalog, 0, catalog.length);
        image(out, offsets, 2, "[/ICCBased 9 0 R]", "", photo);
        image(out, offsets, 3, "[/ICCBased 9 0 R]", "/SMask 4 0 R\n", deflate(noise(SIDE * SIDE * 3, 3)));
        image(out, offsets, 4, "/DeviceGray", "", deflate(noise(SIDE * SIDE, 4)));
        int xref = out.size();
        StringBuilder table = new StringBuilder("xref\n0 5\n0000000000 65535 f \n");
        for (int offset : offsets) {
            table.append(String.format("%010d 00000 n \n", offset));
        }
        table.append("trailer\n<</Size 5\n/Root 1 0 R>>\nstartxref\n").append(xref).append("\n%%EOF\n");
        byte[] end = text(table.toString());
        out.write(end, 0, end.length);
        return out.toByteArray();
    }

    private static byte[] photoPixels() {
        return noise(SIDE * SIDE * 3, 1);
    }

    private static int count(String text, String part) {
        int found = 0;
        for (int at = text.indexOf(part); at >= 0; at = text.indexOf(part, at + 1)) {
            found++;
        }
        return found;
    }

    @Test
    public void storesThePhotoAsJpegAndNothingElse() {
        byte[] original = pdf(deflate(photoPixels()));
        List<int[]> seen = new ArrayList<>();
        byte[] result = PdfImageCompressor.compress(original, (width, height, rgb) -> {
            seen.add(new int[]{width, height, rgb.length});
            assertArrayEquals(photoPixels(), rgb);
            return JPEG;
        });
        String text = new String(result, StandardCharsets.ISO_8859_1);
        assertEquals(1, seen.size());
        assertArrayEquals(new int[]{SIDE, SIDE, SIDE * SIDE * 3}, seen.get(0));
        assertEquals(1, count(text, "/DCTDecode"));
        assertEquals(2, count(text, "/FlateDecode"));
        assertTrue(text.contains("/Length " + JPEG.length + ">> stream\n"));
        assertTrue(new String(result, StandardCharsets.ISO_8859_1).contains(new String(JPEG, StandardCharsets.ISO_8859_1)));
    }

    @Test
    public void keepsTheTableOfOffsetsRight() {
        byte[] result = PdfImageCompressor.compress(pdf(deflate(photoPixels())), (width, height, rgb) -> JPEG);
        String text = new String(result, StandardCharsets.ISO_8859_1);
        Matcher start = Pattern.compile("startxref\n(\\d+)\n%%EOF\n$").matcher(text);
        assertTrue(start.find());
        int xref = Integer.parseInt(start.group(1));
        assertTrue(text.startsWith("xref\n0 5\n0000000000 65535 f \n", xref));
        for (int number = 1; number <= 4; number++) {
            int line = xref + "xref\n0 5\n".length() + number * 20;
            int offset = Integer.parseInt(text.substring(line, line + 10));
            assertTrue("object " + number, text.startsWith(number + " 0 obj\n", offset));
        }
        assertTrue(text.contains("<</Size 5\n/Root 1 0 R>>"));
    }

    @Test
    public void leavesTheFileAloneWhenTheJpegIsNotSmaller() {
        byte[] original = pdf(deflate(photoPixels()));
        byte[] big = new byte[original.length];
        assertSame(original, PdfImageCompressor.compress(original, (width, height, rgb) -> big));
    }

    @Test
    public void leavesTheFileAloneWhenTheEncoderFails() {
        byte[] original = pdf(deflate(photoPixels()));
        assertSame(original, PdfImageCompressor.compress(original, (width, height, rgb) -> {
            throw new java.io.IOException("no encoder");
        }));
    }

    @Test
    public void skipsAPictureWhoseSizeDoesNotMatchItsPixels() {
        byte[] original = pdf(deflate(noise(SIDE * SIDE * 3 - 3, 1)));
        assertSame(original, PdfImageCompressor.compress(original, (width, height, rgb) -> JPEG));
    }

    @Test
    public void leavesAFileThatIsNotAPdfFromThisWriterAlone() {
        byte[] garbage = text("this is not a pdf at all");
        assertSame(garbage, PdfImageCompressor.compress(garbage, (width, height, rgb) -> JPEG));
    }

    @Test
    public void leavesAFileWhoseObjectCountDoesNotAddUpAlone() {
        byte[] original = pdf(deflate(photoPixels()));
        byte[] odd = new String(original, StandardCharsets.ISO_8859_1).replace("<</Size 5", "<</Size 9")
                .getBytes(StandardCharsets.ISO_8859_1);
        assertSame(odd, PdfImageCompressor.compress(odd, (width, height, rgb) -> JPEG));
    }
}
