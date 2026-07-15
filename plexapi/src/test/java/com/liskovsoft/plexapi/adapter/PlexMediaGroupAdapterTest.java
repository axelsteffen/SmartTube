package com.liskovsoft.plexapi.adapter;

import com.liskovsoft.mediaserviceinterfaces.data.MediaGroup;
import com.liskovsoft.plexapi.library.PlexLibraryImpl;
import com.liskovsoft.plexapi.library.PlexMediaItemImpl;
import com.liskovsoft.plexserviceinterfaces.data.PlexLibrary;
import com.liskovsoft.plexserviceinterfaces.data.PlexMediaItem;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class PlexMediaGroupAdapterTest {
    @Test
    public void from_mapsTitleTypeAndItems() {
        PlexLibrary library = new PlexLibraryImpl("1", "Movies", "movie");
        List<PlexMediaItem> items = Arrays.asList(
                movie("10", "Alpha"),
                movie("20", "Beta"));

        MediaGroup group = PlexMediaGroupAdapter.from(library, items);

        assertNotNull(group);
        assertEquals("Movies", group.getTitle());
        assertEquals(MediaGroup.TYPE_MOVIES, group.getType());
        assertEquals("1", group.getParams());
        assertFalse(group.isEmpty());
        assertNotNull(group.getMediaItems());
        assertEquals(2, group.getMediaItems().size());
        assertEquals("10", group.getMediaItems().get(0).getVideoId());
        assertEquals("Alpha", group.getMediaItems().get(0).getTitle());
        assertEquals("20", group.getMediaItems().get(1).getVideoId());
        assertNull(group.getNextPageKey());
        assertNull(group.getChannelId());
    }

    @Test
    public void from_nullOrEmptyLibraryKey_returnsNull() {
        assertNull(PlexMediaGroupAdapter.from(null, Collections.emptyList()));
        assertNull(PlexMediaGroupAdapter.from(
                new PlexLibraryImpl("", "Movies", "movie"),
                Collections.emptyList()));
    }

    @Test
    public void from_emptyOrNullItems_isEmpty() {
        PlexLibrary library = new PlexLibraryImpl("1", "Movies", "movie");

        MediaGroup emptyList = PlexMediaGroupAdapter.from(library, Collections.emptyList());
        assertNotNull(emptyList);
        assertTrue(emptyList.isEmpty());
        assertNull(emptyList.getMediaItems());

        MediaGroup nullItems = PlexMediaGroupAdapter.from(library, null);
        assertNotNull(nullItems);
        assertTrue(nullItems.isEmpty());
        assertNull(nullItems.getMediaItems());
    }

    @Test
    public void from_skipsItemsWithoutRatingKey() {
        PlexLibrary library = new PlexLibraryImpl("1", "Movies", "movie");
        List<PlexMediaItem> items = Arrays.asList(
                new PlexMediaItemImpl("", "/k", "Bad", "movie", 0, null, 0),
                movie("42", "Good"));

        MediaGroup group = PlexMediaGroupAdapter.from(library, items);

        assertNotNull(group);
        assertNotNull(group.getMediaItems());
        assertEquals(1, group.getMediaItems().size());
        assertEquals("42", group.getMediaItems().get(0).getVideoId());
    }

    @Test
    public void getPlexLibrary_returnsUnderlying() {
        PlexLibrary library = new PlexLibraryImpl("7", "Kids", "movie");
        PlexMediaGroupAdapter group = PlexMediaGroupAdapter.from(library, Collections.emptyList());
        assertNotNull(group);
        assertEquals(library, group.getPlexLibrary());
    }

    private static PlexMediaItem movie(String ratingKey, String title) {
        return new PlexMediaItemImpl(
                ratingKey, "/library/metadata/" + ratingKey, title, "movie", 0, null, 2020);
    }
}
