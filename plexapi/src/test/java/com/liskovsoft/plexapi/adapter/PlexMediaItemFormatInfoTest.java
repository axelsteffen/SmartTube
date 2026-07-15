package com.liskovsoft.plexapi.adapter;

import com.liskovsoft.mediaserviceinterfaces.data.MediaItemFormatInfo;
import com.liskovsoft.plexapi.library.PlexMediaItemImpl;
import com.liskovsoft.plexapi.media.PlexStreamInfoImpl;
import com.liskovsoft.plexserviceinterfaces.data.PlexMediaItem;
import com.liskovsoft.plexserviceinterfaces.data.PlexStreamInfo;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class PlexMediaItemFormatInfoTest {
    private static PlexMediaItem sampleItem() {
        return new PlexMediaItemImpl(
                "12345",
                "/library/metadata/12345",
                "Test Movie",
                "movie",
                5_400_000L,
                "https://plex/thumb.jpg",
                2020);
    }

    @Test
    public void from_directPlay_mapsUrlFormats() {
        PlexStreamInfo stream = new PlexStreamInfoImpl(
                "https://plex:32400/library/parts/1/file.mkv?X-Plex-Token=t",
                "video/x-matroska",
                false);

        MediaItemFormatInfo info = PlexMediaItemFormatInfo.from(sampleItem(), stream);

        assertNotNull(info);
        assertTrue(info.containsMedia());
        assertTrue(info.containsUrlFormats());
        assertFalse(info.containsHlsUrl());
        assertFalse(info.containsDashFormats());
        assertEquals("12345", info.getVideoId());
        assertEquals("Test Movie", info.getTitle());
        assertEquals("5400", info.getLengthSeconds());
        assertNull(info.getHlsManifestUrl());
        assertEquals(1, info.createUrlList().size());
        assertEquals(stream.getUrl(), info.createUrlList().get(0));
        assertEquals(stream.getUrl(), info.getUrlFormats().get(0).getUrl());
        assertEquals("video/x-matroska", info.getUrlFormats().get(0).getMimeType());
        assertTrue(info.isStreamSeekable());
        assertFalse(info.isUnplayable());
        assertFalse(info.isLive());
    }

    @Test
    public void from_transcodedHls_mapsHlsManifestUrl() {
        PlexStreamInfo stream = new PlexStreamInfoImpl(
                "https://plex:32400/video/:/transcode/universal/start.m3u8?X-Plex-Token=t",
                "application/x-mpegURL",
                true);

        MediaItemFormatInfo info = PlexMediaItemFormatInfo.from(sampleItem(), stream);

        assertNotNull(info);
        assertTrue(info.containsMedia());
        assertTrue(info.containsHlsUrl());
        assertFalse(info.containsUrlFormats());
        assertEquals(stream.getUrl(), info.getHlsManifestUrl());
        assertTrue(info.createUrlList().isEmpty());
        assertNull(info.getUrlFormats());
    }

    @Test
    public void from_hlsMimeWithoutTranscodedFlag_treatedAsHls() {
        PlexStreamInfo stream = new PlexStreamInfoImpl(
                "https://plex/start.m3u8",
                "application/x-mpegURL",
                false);

        PlexMediaItemFormatInfo info = PlexMediaItemFormatInfo.from(sampleItem(), stream);

        assertNotNull(info);
        assertTrue(info.isTranscoded());
        assertTrue(info.containsHlsUrl());
        assertFalse(info.containsUrlFormats());
    }

    @Test
    public void from_nullOrEmpty_returnsNull() {
        PlexStreamInfo stream = new PlexStreamInfoImpl("https://plex/f.mp4", "video/mp4", false);
        assertNull(PlexMediaItemFormatInfo.from(null, stream));
        assertNull(PlexMediaItemFormatInfo.from(sampleItem(), null));
        assertNull(PlexMediaItemFormatInfo.from(sampleItem(),
                new PlexStreamInfoImpl("", "video/mp4", false)));
        assertNull(PlexMediaItemFormatInfo.from(
                new PlexMediaItemImpl("", "/k", "t", "movie", 0, null, 0), stream));
    }

    @Test
    public void from_mapsExternalSubtitles() {
        PlexStreamInfo stream = new PlexStreamInfoImpl(
                "https://plex:32400/library/parts/1/file.mkv?X-Plex-Token=t",
                "video/x-matroska",
                false,
                0L,
                java.util.Collections.singletonList(
                        new com.liskovsoft.plexapi.media.PlexSubtitleImpl(
                                "https://plex:32400/library/streams/9?X-Plex-Token=t",
                                "en",
                                "English",
                                "srt")));

        MediaItemFormatInfo info = PlexMediaItemFormatInfo.from(sampleItem(), stream);

        assertNotNull(info);
        assertNotNull(info.getSubtitles());
        assertEquals(1, info.getSubtitles().size());
        assertEquals("application/x-subrip", info.getSubtitles().get(0).getMimeType());
        assertEquals(
                "https://plex:32400/library/streams/9?X-Plex-Token=t",
                info.getSubtitles().get(0).getBaseUrl());
    }
}
