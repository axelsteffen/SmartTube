package com.liskovsoft.plexapi.network;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class PlexUrlHelperTest {
    @Test
    public void absoluteUrl_joinsRelativeKeyAndToken() {
        String url = PlexUrlHelper.absoluteUrl(
                "https://10.0.0.5:32400",
                "/library/parts/827/file.mkv",
                "tok123");
        assertEquals(
                "https://10.0.0.5:32400/library/parts/827/file.mkv?X-Plex-Token=tok123",
                url);
    }

    @Test
    public void absoluteUrl_preservesAbsoluteHttps() {
        String url = PlexUrlHelper.absoluteUrl(
                "https://ignored:32400/",
                "https://cdn.example/part.mkv",
                "tok");
        assertEquals("https://cdn.example/part.mkv?X-Plex-Token=tok", url);
    }

    @Test
    public void absoluteUrl_nullKey() {
        assertNull(PlexUrlHelper.absoluteUrl("https://host/", null, "t"));
    }

    @Test
    public void normalizeBaseUrl_addsSlash() {
        assertEquals("https://host:32400/", PlexUrlHelper.normalizeBaseUrl("https://host:32400"));
    }
}
