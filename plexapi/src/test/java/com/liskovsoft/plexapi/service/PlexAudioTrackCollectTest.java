package com.liskovsoft.plexapi.service;

import com.google.gson.Gson;
import com.liskovsoft.plexapi.media.PlexAudioTrackImpl;
import com.liskovsoft.plexapi.network.dto.MediaContainer;
import com.liskovsoft.plexapi.network.dto.MediaContainerResponse;
import com.liskovsoft.plexserviceinterfaces.data.PlexAudioTrack;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Pure JVM tests for audio track collection / selection (Phase 4.3).
 */
public class PlexAudioTrackCollectTest {
    private static final Gson GSON = new Gson();

    @Test
    public void collectAudioTracks_mapsStreams() {
        MediaContainer container = parse("{"
                + "\"MediaContainer\":{"
                + "\"Metadata\":[{\"Media\":[{\"Part\":[{"
                + "\"Stream\":["
                + "{\"id\":10,\"streamType\":1,\"codec\":\"h264\"},"
                + "{\"id\":11,\"streamType\":2,\"codec\":\"aac\",\"languageCode\":\"en\","
                + "\"displayTitle\":\"English\",\"channels\":2,\"selected\":true},"
                + "{\"id\":12,\"streamType\":2,\"codec\":\"ac3\",\"language\":\"de\","
                + "\"displayTitle\":\"Deutsch\",\"channels\":6}"
                + "]"
                + "}]}]}]"
                + "}}");

        List<PlexAudioTrack> tracks = PlexMediaServiceImpl.collectAudioTracks(container);
        assertEquals(2, tracks.size());
        assertEquals(11L, tracks.get(0).getId());
        assertEquals("en", tracks.get(0).getLanguageCode());
        assertTrue(tracks.get(0).isSelected());
        assertEquals(2, tracks.get(0).getChannels());
        assertEquals(12L, tracks.get(1).getId());
        assertEquals("de", tracks.get(1).getLanguageCode());
        assertEquals(6, tracks.get(1).getChannels());
        assertFalse(tracks.get(1).isSelected());
    }

    @Test
    public void pickAudioStreamId_overridePreferredSelectedFirst() {
        List<PlexAudioTrack> tracks = Arrays.asList(
                new PlexAudioTrackImpl(11, "en", "English", "aac", 2, true),
                new PlexAudioTrackImpl(12, "de", "Deutsch", "ac3", 6, false),
                new PlexAudioTrackImpl(13, "ja", "Japanese", "aac", 2, false));

        assertEquals(12L, PlexMediaServiceImpl.pickAudioStreamId(tracks, 12L, "en"));
        assertEquals(13L, PlexMediaServiceImpl.pickAudioStreamId(tracks, null, "ja"));
        assertEquals(11L, PlexMediaServiceImpl.pickAudioStreamId(tracks, null, "fr"));
        assertEquals(0L, PlexMediaServiceImpl.pickAudioStreamId(Collections.emptyList(), 12L, "en"));
        assertTrue(PlexMediaServiceImpl.languageMatches("en-US", "en"));
        assertTrue(PlexMediaServiceImpl.languageMatches("de", "de-DE"));
        assertFalse(PlexMediaServiceImpl.languageMatches("en", "de"));
    }

    private static MediaContainer parse(String json) {
        return GSON.fromJson(json, MediaContainerResponse.class).getMediaContainer();
    }
}
