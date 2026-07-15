package com.liskovsoft.plexapi.service;

import com.google.gson.Gson;
import com.liskovsoft.plexapi.adapter.PlexMediaSubtitle;
import com.liskovsoft.plexapi.network.dto.MediaContainer;
import com.liskovsoft.plexapi.network.dto.MediaContainerResponse;
import com.liskovsoft.plexserviceinterfaces.data.PlexSubtitle;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Pure JVM tests (no Robolectric) for subtitle collection from metadata JSON.
 */
public class PlexSubtitleCollectTest {
    private static final Gson GSON = new Gson();

    @Test
    public void collectExternalSubtitles_keepsSidecarsSkipsEmbeddedAndPgs() {
        MediaContainer container = parse("{"
                + "\"MediaContainer\":{"
                + "\"Metadata\":[{"
                + "\"Media\":[{"
                + "\"Part\":[{"
                + "\"key\":\"/library/parts/1/file.mkv\","
                + "\"Stream\":["
                + "{\"streamType\":3,\"codec\":\"srt\",\"languageCode\":\"en\","
                + "\"displayTitle\":\"English\",\"key\":\"/library/streams/3\"},"
                + "{\"streamType\":3,\"codec\":\"ass\",\"language\":\"de\","
                + "\"key\":\"/library/streams/4\"},"
                + "{\"streamType\":3,\"codec\":\"pgs\",\"languageCode\":\"ja\","
                + "\"key\":\"/library/streams/5\"},"
                + "{\"streamType\":3,\"codec\":\"srt\",\"languageCode\":\"fr\"},"
                + "{\"streamType\":1,\"codec\":\"h264\"}"
                + "]"
                + "}]"
                + "}]"
                + "}]"
                + "}}");

        List<PlexSubtitle> subs = PlexMediaServiceImpl.collectExternalSubtitles(
                container, "https://plex:32400/", "tok");

        assertEquals(2, subs.size());
        assertTrue(subs.get(0).getUrl().contains("library/streams/3"));
        assertTrue(subs.get(0).getUrl().contains("X-Plex-Token=tok"));
        assertEquals("en", subs.get(0).getLanguageCode());
        assertEquals("application/x-subrip", PlexMediaSubtitle.mimeFromCodec(subs.get(0).getCodec()));
        assertEquals("de", subs.get(1).getLanguageCode());
        assertEquals("ass", subs.get(1).getCodec());
    }

    private static MediaContainer parse(String json) {
        MediaContainerResponse response = GSON.fromJson(json, MediaContainerResponse.class);
        return response.getMediaContainer();
    }
}
