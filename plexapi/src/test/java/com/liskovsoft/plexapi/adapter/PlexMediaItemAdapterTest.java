package com.liskovsoft.plexapi.adapter;

import com.liskovsoft.mediaserviceinterfaces.data.MediaItem;
import com.liskovsoft.plexapi.library.PlexMediaItemImpl;
import com.liskovsoft.plexserviceinterfaces.data.PlexBackedMediaItem;
import com.liskovsoft.plexserviceinterfaces.data.PlexMediaItem;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class PlexMediaItemAdapterTest {
    @Test
    public void from_mapsCoreFields() {
        PlexMediaItem plex = new PlexMediaItemImpl(
                "12345",
                "/library/metadata/12345",
                "Test Movie",
                "movie",
                5_400_000L,
                "https://plex/thumb.jpg?X-Plex-Token=t",
                2020);

        MediaItem item = PlexMediaItemAdapter.from(plex);

        assertNotNull(item);
        assertEquals(MediaItem.TYPE_VIDEO, item.getType());
        assertEquals("12345", item.getVideoId());
        assertEquals("Test Movie", item.getTitle());
        assertEquals(5_400_000L, item.getDurationMs());
        assertEquals("https://plex/thumb.jpg?X-Plex-Token=t", item.getCardImageUrl());
        assertEquals("https://plex/thumb.jpg?X-Plex-Token=t", item.getBackgroundImageUrl());
        assertEquals("2020", item.getSecondTitle());
        assertEquals("2020", item.getProductionDate());
        assertTrue(item.isMovie());
        assertFalse(item.isLive());
        assertFalse(item.isShorts());
    }

    @Test
    public void from_nullOrEmptyRatingKey_returnsNull() {
        assertNull(PlexMediaItemAdapter.from(null));
        assertNull(PlexMediaItemAdapter.from(new PlexMediaItemImpl(
                "", "/k", "t", "movie", 0, null, 0)));
    }

    @Test
    public void from_show_mapsAsPlaylistContainer() {
        PlexMediaItem plex = new PlexMediaItemImpl(
                "2001", "/library/metadata/2001", "Breaking Bad", "show", 0, null, 2008);

        MediaItem item = PlexMediaItemAdapter.from(plex);

        assertNotNull(item);
        assertEquals(MediaItem.TYPE_PLAYLIST, item.getType());
        assertNull(item.getVideoId());
        assertEquals("2001", item.getPlaylistId());
        assertTrue(item.hasUploads());
        assertFalse(item.isMovie());
    }

    @Test
    public void from_season_mapsAsPlaylistContainer() {
        PlexMediaItem plex = new PlexMediaItemImpl(
                "3001", "/library/metadata/3001", "Season 1", "season", 0, null, 2008);

        MediaItem item = PlexMediaItemAdapter.from(plex);

        assertNotNull(item);
        assertEquals(MediaItem.TYPE_PLAYLIST, item.getType());
        assertNull(item.getVideoId());
        assertEquals("3001", item.getPlaylistId());
        assertTrue(item.hasUploads());
    }

    @Test
    public void from_episode_mapsAsVideo() {
        PlexMediaItem plex = new PlexMediaItemImpl(
                "4001", "/library/metadata/4001", "Pilot", "episode", 3_600_000L, null, 2008);

        MediaItem item = PlexMediaItemAdapter.from(plex);

        assertNotNull(item);
        assertEquals(MediaItem.TYPE_VIDEO, item.getType());
        assertEquals("4001", item.getVideoId());
        assertNull(item.getPlaylistId());
        assertFalse(item.hasUploads());
    }

    @Test
    public void from_nonMovie_isMovieFalse() {
        PlexMediaItem plex = new PlexMediaItemImpl(
                "9", "/library/metadata/9", "Episode", "episode", 0, null, 0);
        MediaItem item = PlexMediaItemAdapter.from(plex);
        assertNotNull(item);
        assertFalse(item.isMovie());
    }

    @Test
    public void equals_byRatingKey() {
        MediaItem a = PlexMediaItemAdapter.from(new PlexMediaItemImpl(
                "42", "/k", "A", "movie", 0, null, 0));
        MediaItem b = PlexMediaItemAdapter.from(new PlexMediaItemImpl(
                "42", "/k", "B", "movie", 0, null, 0));
        MediaItem c = PlexMediaItemAdapter.from(new PlexMediaItemImpl(
                "99", "/k", "C", "movie", 0, null, 0));
        assertNotNull(a);
        assertEquals(a, b);
        assertFalse(a.equals(c));
    }

    @Test
    public void from_implementsPlexBackedMediaItem() {
        MediaItem item = PlexMediaItemAdapter.from(new PlexMediaItemImpl(
                "1", "/k", "T", "movie", 0, null, 0));
        assertNotNull(item);
        assertTrue(item instanceof PlexBackedMediaItem);
    }

    @Test
    public void from_mapsViewOffsetToProgress() {
        PlexMediaItem plex = new PlexMediaItemImpl(
                "12345",
                "/library/metadata/12345",
                "Test Movie",
                "movie",
                5_400_000L,
                null,
                2020,
                540_000L); // 10%

        MediaItem item = PlexMediaItemAdapter.from(plex);

        assertNotNull(item);
        assertEquals(10, item.getPercentWatched());
        assertEquals(540, item.getStartTimeSeconds());
    }
}
