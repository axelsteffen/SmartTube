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
}
