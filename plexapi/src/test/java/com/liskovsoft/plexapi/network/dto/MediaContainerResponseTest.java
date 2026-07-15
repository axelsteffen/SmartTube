package com.liskovsoft.plexapi.network.dto;

import com.google.gson.Gson;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class MediaContainerResponseTest {
    private final Gson mGson = new Gson();

    @Test
    public void parsesLibrarySections() {
        String json = "{"
                + "\"MediaContainer\":{"
                + "\"size\":1,"
                + "\"Directory\":[{"
                + "\"key\":\"1\","
                + "\"type\":\"movie\","
                + "\"title\":\"Movies\""
                + "}]"
                + "}}";

        MediaContainerResponse response = mGson.fromJson(json, MediaContainerResponse.class);
        assertNotNull(response.getMediaContainer());
        assertEquals(1, response.getMediaContainer().getDirectories().size());
        PlexDirectory dir = response.getMediaContainer().getDirectories().get(0);
        assertEquals("1", dir.getKey());
        assertEquals("movie", dir.getType());
        assertEquals("Movies", dir.getTitle());
    }

    @Test
    public void parsesMetadataWithPart() {
        String json = "{"
                + "\"MediaContainer\":{"
                + "\"Metadata\":[{"
                + "\"ratingKey\":\"1049\","
                + "\"key\":\"/library/metadata/1049\","
                + "\"type\":\"movie\","
                + "\"title\":\"Zoolander\","
                + "\"year\":2001,"
                + "\"duration\":5129000,"
                + "\"thumb\":\"/library/metadata/1049/thumb/1\","
                + "\"Media\":[{"
                + "\"container\":\"mkv\","
                + "\"Part\":[{"
                + "\"id\":827,"
                + "\"key\":\"/library/parts/827/file.mkv\","
                + "\"container\":\"mkv\""
                + "}]"
                + "}]"
                + "}]"
                + "}}";

        MediaContainerResponse response = mGson.fromJson(json, MediaContainerResponse.class);
        PlexMetadata meta = response.getMediaContainer().getMetadata().get(0);
        assertEquals("1049", meta.getRatingKey());
        assertEquals("Zoolander", meta.getTitle());
        assertEquals(5129000L, meta.getDuration());
        PlexPart part = meta.getMedia().get(0).getParts().get(0);
        assertEquals("/library/parts/827/file.mkv", part.getKey());
    }

    @Test
    public void parsesPlexResourceServer() {
        String json = "{"
                + "\"name\":\"Home\","
                + "\"product\":\"Plex Media Server\","
                + "\"clientIdentifier\":\"abc\","
                + "\"owned\":true,"
                + "\"provides\":\"server\","
                + "\"accessToken\":\"server-token\","
                + "\"connections\":[{"
                + "\"protocol\":\"https\","
                + "\"address\":\"10.0.0.5\","
                + "\"port\":32400,"
                + "\"uri\":\"https://10.0.0.5:32400\","
                + "\"local\":true"
                + "}]"
                + "}";

        PlexResource resource = mGson.fromJson(json, PlexResource.class);
        assertTrue(resource.isServer());
        assertEquals("Home", resource.getName());
        assertEquals("server-token", resource.getAccessToken());
        assertEquals("https://10.0.0.5:32400", resource.getConnections().get(0).getUri());
    }
}
