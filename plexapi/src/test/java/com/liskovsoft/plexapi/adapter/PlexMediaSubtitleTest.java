package com.liskovsoft.plexapi.adapter;

import com.liskovsoft.mediaserviceinterfaces.data.MediaSubtitle;
import com.liskovsoft.plexapi.media.PlexSubtitleImpl;
import com.liskovsoft.plexserviceinterfaces.data.PlexSubtitle;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class PlexMediaSubtitleTest {
    @Test
    public void mimeFromCodec_mapsSupportedTypes() {
        assertEquals("application/x-subrip", PlexMediaSubtitle.mimeFromCodec("srt"));
        assertEquals("application/x-subrip", PlexMediaSubtitle.mimeFromCodec("SUBRIP"));
        assertEquals("text/x-ssa", PlexMediaSubtitle.mimeFromCodec("ass"));
        assertEquals("text/x-ssa", PlexMediaSubtitle.mimeFromCodec("ssa"));
        assertEquals("text/vtt", PlexMediaSubtitle.mimeFromCodec("vtt"));
        assertEquals("application/ttml+xml", PlexMediaSubtitle.mimeFromCodec("ttml"));
    }

    @Test
    public void mimeFromCodec_skipsImageBased() {
        assertNull(PlexMediaSubtitle.mimeFromCodec("pgs"));
        assertNull(PlexMediaSubtitle.mimeFromCodec("dvdsub"));
        assertNull(PlexMediaSubtitle.mimeFromCodec(null));
        assertNull(PlexMediaSubtitle.mimeFromCodec(""));
    }

    @Test
    public void from_mapsFields() {
        PlexSubtitle plex = new PlexSubtitleImpl(
                "https://plex/library/streams/9?X-Plex-Token=t",
                "en",
                "English",
                "srt");

        MediaSubtitle sub = PlexMediaSubtitle.from(plex);

        assertEquals(plex.getUrl(), sub.getBaseUrl());
        assertEquals("en", sub.getLanguageCode());
        assertEquals("English", sub.getName());
        assertEquals("application/x-subrip", sub.getMimeType());
        assertEquals("srt", sub.getCodecs());
        assertFalse(sub.isTranslatable());
        assertNull(sub.getVssId());
        assertNull(sub.getType());
    }

    @Test
    public void from_rejectsUnsupportedOrEmpty() {
        assertNull(PlexMediaSubtitle.from(null));
        assertNull(PlexMediaSubtitle.from(new PlexSubtitleImpl("", "en", "E", "srt")));
        assertNull(PlexMediaSubtitle.from(
                new PlexSubtitleImpl("https://x", "en", "E", "pgs")));
        assertTrue(PlexMediaSubtitle.from(
                new PlexSubtitleImpl("https://x", "de", null, "ass")) != null);
    }
}
