package com.yurii.pavlenko.myassistant.flights.report;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

/**
 * Makes a PDF written by {@code PdfDocument} smaller. That writer keeps every bitmap as raw pixels, which for a
 * photo tile is several times the size of the JPEG it came from, so the opaque photos are rewritten as JPEG.
 * Anything that is not as expected leaves the PDF exactly as it was.
 */
final class PdfImageCompressor {

    /** Encodes 8-bit RGB pixels, row by row, as a JPEG file. */
    interface JpegEncoder {
        byte[] encode(int width, int height, byte[] rgb) throws IOException;
    }

    private static final Charset TEXT = StandardCharsets.ISO_8859_1;
    private static final byte[] OBJECT_MARK = " 0 obj\n".getBytes(TEXT);
    private static final byte[] OBJECT_END = "endobj\n".getBytes(TEXT);
    private static final byte[] STREAM_START = "stream\n".getBytes(TEXT);
    private static final byte[] STREAM_END = "\nendstream\nendobj\n".getBytes(TEXT);
    private static final byte[] NEWLINE = {'\n'};
    private static final byte[] TRAILER = "trailer\n".getBytes(TEXT);
    private static final byte[] START_XREF = "\nstartxref\n".getBytes(TEXT);
    private static final Pattern LENGTH = Pattern.compile("/Length (\\d+)(?![\\d]| 0 R)");
    private static final Pattern WIDTH = Pattern.compile("/Width (\\d+)");
    private static final Pattern HEIGHT = Pattern.compile("/Height (\\d+)");
    private static final Pattern SIZE = Pattern.compile("/Size (\\d+)");

    private PdfImageCompressor() {
    }

    /** The smaller PDF, or the same one when it cannot be made smaller. */
    static byte[] compress(byte[] pdf, JpegEncoder encoder) {
        try {
            byte[] smaller = rewrite(pdf, encoder);
            return smaller != null && smaller.length < pdf.length ? smaller : pdf;
        } catch (IOException | RuntimeException e) {
            return pdf;
        }
    }

    private static byte[] rewrite(byte[] pdf, JpegEncoder encoder) throws IOException {
        int first = firstObject(pdf);
        List<PdfObject> objects = new ArrayList<>();
        int position = first;
        PdfObject object;
        while ((object = PdfObject.parse(pdf, position)) != null) {
            objects.add(object);
            position = object.end;
        }
        int trailerStart = indexOf(pdf, TRAILER, position);
        int trailerEnd = indexOf(pdf, START_XREF, trailerStart);
        if (objects.isEmpty() || trailerStart < 0 || trailerEnd < 0) {
            throw new IOException("Not a PDF written by PdfDocument");
        }
        String trailer = new String(pdf, trailerStart, trailerEnd - trailerStart, TEXT);
        int highest = 0;
        for (PdfObject each : objects) {
            highest = Math.max(highest, each.number);
        }
        Matcher size = SIZE.matcher(trailer);
        if (!size.find() || Integer.parseInt(size.group(1)) != highest + 1) {
            throw new IOException("Unexpected object numbering");
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream(pdf.length / 2);
        out.write(pdf, 0, first);
        int[] offsets = new int[highest + 1];
        boolean changed = false;
        for (PdfObject each : objects) {
            offsets[each.number] = out.size();
            byte[] jpeg = each.hasStream() ? photoAsJpeg(pdf, each, encoder) : null;
            if (jpeg == null) {
                out.write(pdf, each.start, each.end - each.start);
            } else {
                writeAsJpeg(out, each, jpeg);
                changed = true;
            }
        }
        if (!changed) {
            return null;
        }
        int xrefStart = out.size();
        StringBuilder xref = new StringBuilder("xref\n0 ").append(highest + 1).append('\n')
                .append("0000000000 65535 f \n");
        for (int number = 1; number <= highest; number++) {
            if (offsets[number] == 0) {
                xref.append("0000000000 00000 f \n");
            } else {
                xref.append(String.format(Locale.US, "%010d 00000 n \n", offsets[number]));
            }
        }
        xref.append(trailer).append("\nstartxref\n").append(xrefStart).append("\n%%EOF\n");
        out.write(xref.toString().getBytes(TEXT));
        return out.toByteArray();
    }

    /** The JPEG for an opaque RGB photo that is smaller than the stored pixels; null for any other object. */
    private static byte[] photoAsJpeg(byte[] pdf, PdfObject object, JpegEncoder encoder) throws IOException {
        String dictionary = object.dictionary;
        if (!dictionary.contains("/Subtype /Image") || !dictionary.contains("/Filter /FlateDecode")
                || !dictionary.contains("/ColorSpace [/ICCBased") || !dictionary.contains("/BitsPerComponent 8")
                || dictionary.contains("/SMask") || dictionary.contains("/Mask")) {
            return null;
        }
        int width = numberIn(WIDTH, dictionary);
        int height = numberIn(HEIGHT, dictionary);
        byte[] rgb = width > 0 && height > 0 ? inflate(pdf, object.streamStart, object.streamLength, width * height * 3) : null;
        if (rgb == null) {
            return null;
        }
        byte[] jpeg = encoder.encode(width, height, rgb);
        return jpeg.length < object.streamLength ? jpeg : null;
    }

    private static void writeAsJpeg(ByteArrayOutputStream out, PdfObject object, byte[] jpeg) throws IOException {
        String dictionary = object.dictionary.replace("/Filter /FlateDecode", "/Filter /DCTDecode");
        dictionary = LENGTH.matcher(dictionary).replaceFirst("/Length " + jpeg.length);
        out.write((object.number + " 0 obj\n" + dictionary + "stream\n").getBytes(TEXT));
        out.write(jpeg);
        out.write(STREAM_END);
    }

    private static int numberIn(Pattern pattern, String text) {
        Matcher matcher = pattern.matcher(text);
        return matcher.find() ? Integer.parseInt(matcher.group(1)) : 0;
    }

    /** The inflated bytes when they are exactly the expected count; null when the data is something else. */
    private static byte[] inflate(byte[] data, int offset, int length, int expected) {
        Inflater inflater = new Inflater();
        try {
            inflater.setInput(data, offset, length);
            byte[] raw = new byte[expected];
            int done = 0;
            while (done < expected) {
                int count = inflater.inflate(raw, done, expected - done);
                if (count == 0) {
                    return null;
                }
                done += count;
            }
            return raw;
        } catch (DataFormatException e) {
            return null;
        } finally {
            inflater.end();
        }
    }

    /** The header of the file ends where the first object begins. */
    private static int firstObject(byte[] pdf) throws IOException {
        int line = 0;
        while (line >= 0 && line < pdf.length) {
            if (PdfObject.parse(pdf, line) != null) {
                return line;
            }
            int newline = indexOf(pdf, NEWLINE, line);
            line = newline < 0 ? -1 : newline + 1;
        }
        throw new IOException("No objects found");
    }

    static int indexOf(byte[] data, byte[] pattern, int from) {
        outer:
        for (int i = Math.max(0, from); i <= data.length - pattern.length; i++) {
            for (int j = 0; j < pattern.length; j++) {
                if (data[i + j] != pattern[j]) {
                    continue outer;
                }
            }
            return i;
        }
        return -1;
    }

    /** One indirect object of the file, found by its position; a stream is read by its declared length. */
    private static final class PdfObject {
        final int number;
        final int start;
        final int end;
        final String dictionary;
        final int streamStart;
        final int streamLength;

        private PdfObject(int number, int start, int end, String dictionary, int streamStart, int streamLength) {
            this.number = number;
            this.start = start;
            this.end = end;
            this.dictionary = dictionary;
            this.streamStart = streamStart;
            this.streamLength = streamLength;
        }

        boolean hasStream() {
            return dictionary != null;
        }

        /** The object that starts at the position, or null when there is none, as at the table of offsets. */
        static PdfObject parse(byte[] pdf, int start) throws IOException {
            int cursor = start;
            int number = 0;
            while (cursor < pdf.length && pdf[cursor] >= '0' && pdf[cursor] <= '9') {
                number = number * 10 + (pdf[cursor] - '0');
                cursor++;
            }
            if (cursor == start || !startsWith(pdf, cursor, OBJECT_MARK)) {
                return null;
            }
            int body = cursor + OBJECT_MARK.length;
            int objectEnd = indexOf(pdf, OBJECT_END, body);
            if (objectEnd < 0) {
                throw new IOException("Object " + number + " is not closed");
            }
            int keyword = indexOf(pdf, STREAM_START, body);
            if (keyword < 0 || keyword > objectEnd) {
                return new PdfObject(number, start, objectEnd + OBJECT_END.length, null, -1, 0);
            }
            String dictionary = new String(pdf, body, keyword - body, TEXT);
            Matcher length = LENGTH.matcher(dictionary);
            if (!length.find()) {
                throw new IOException("Object " + number + " has no direct length");
            }
            int streamStart = keyword + STREAM_START.length;
            int streamLength = Integer.parseInt(length.group(1));
            if (!startsWith(pdf, streamStart + streamLength, STREAM_END)) {
                throw new IOException("Object " + number + " has a stream of another length");
            }
            return new PdfObject(number, start, streamStart + streamLength + STREAM_END.length,
                    dictionary, streamStart, streamLength);
        }

        private static boolean startsWith(byte[] data, int at, byte[] prefix) {
            if (at < 0 || at + prefix.length > data.length) {
                return false;
            }
            for (int i = 0; i < prefix.length; i++) {
                if (data[at + i] != prefix[i]) {
                    return false;
                }
            }
            return true;
        }
    }
}
