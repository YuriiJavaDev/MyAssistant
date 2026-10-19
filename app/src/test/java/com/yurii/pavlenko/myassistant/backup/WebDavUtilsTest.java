package com.yurii.pavlenko.myassistant.backup;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class WebDavUtilsTest {

    @Test
    public void acceptsHttpsInAnyCase() {
        assertTrue(WebDavUtils.isSecureUrl("https://cloud.example.com/dav"));
        assertTrue(WebDavUtils.isSecureUrl("  HTTPS://cloud.example.com/dav "));
    }

    @Test
    public void rejectsPlainHttpAndEmptyValues() {
        assertFalse(WebDavUtils.isSecureUrl("http://cloud.example.com/dav"));
        assertFalse(WebDavUtils.isSecureUrl("cloud.example.com"));
        assertFalse(WebDavUtils.isSecureUrl("https:/cloud"));
        assertFalse(WebDavUtils.isSecureUrl(""));
        assertFalse(WebDavUtils.isSecureUrl(null));
    }

    @Test
    public void buildsTargetUrlWithSingleSeparator() {
        assertEquals("https://h/dav/a.db", WebDavUtils.buildTargetUrl("https://h/dav", "a.db"));
        assertEquals("https://h/dav/a.db", WebDavUtils.buildTargetUrl(" https://h/dav/ ", "a.db"));
    }
}
