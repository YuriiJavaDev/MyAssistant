package com.yurii.pavlenko.myassistant.network;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class HttpTextTest {

    @Test
    public void readsBodyWithinLimit() throws IOException {
        byte[] body = "{\"ok\":true}".getBytes(StandardCharsets.UTF_8);
        assertEquals("{\"ok\":true}", HttpText.readAll(new ByteArrayInputStream(body), body.length));
    }

    @Test
    public void decodesUtf8() throws IOException {
        byte[] body = "Київ".getBytes(StandardCharsets.UTF_8);
        assertEquals("Київ", HttpText.readAll(new ByteArrayInputStream(body), 100));
    }

    @Test
    public void rejectsBodyOverLimit() {
        byte[] body = new byte[10_000];
        try {
            HttpText.readAll(new ByteArrayInputStream(body), 9_999);
            fail("Expected an IOException");
        } catch (IOException expected) {
            // the body is longer than the limit
        }
    }
}
