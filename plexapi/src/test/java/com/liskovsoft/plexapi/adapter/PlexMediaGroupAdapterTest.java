package com.liskovsoft.plexapi.adapter;

import com.liskovsoft.mediaserviceinterfaces.data.MediaGroup;
import com.liskovsoft.plexapi.library.PlexLibraryImpl;
import com.liskovsoft.plexapi.library.PlexMediaItemImpl;
import com.liskovsoft.plexapi.library.PlexPage;
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
        assertEquals(3, group.getMediaItems().size());
        assertNull(group.getMediaItems().get(0).getVideoId());
        assertNotNull(group.getMediaItems().get(0).getReloadPageKey());
        assertEquals("10", group.getMediaItems().get(1).getVideoId());
        assertEquals("Alpha", group.getMediaItems().get(1).getTitle());
        assertEquals("20", group.getMediaItems().get(2).getVideoId());
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
    public void from_emptyOrNullItems_hasBrowseStubOnly() {
        PlexLibrary library = new PlexLibraryImpl("1", "Movies", "movie");

        MediaGroup emptyList = PlexMediaGroupAdapter.from(library, Collections.emptyList());
        assertNotNull(emptyList);
        assertFalse(emptyList.isEmpty());
        assertEquals(1, emptyList.getMediaItems().size());
        assertNotNull(emptyList.getMediaItems().get(0).getReloadPageKey());

        MediaGroup nullItems = PlexMediaGroupAdapter.from(library, null);
        assertNotNull(nullItems);
        assertFalse(nullItems.isEmpty());
        assertEquals(1, nullItems.getMediaItems().size());
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
        assertEquals(2, group.getMediaItems().size());
        assertEquals("42", group.getMediaItems().get(1).getVideoId());
    }

    @Test
    public void from_setsNextPageKeyWhenMorePagesExist() {
        PlexLibrary library = new PlexLibraryImpl("1", "Movies", "movie");
        List<PlexMediaItem> items = Collections.singletonList(movie("10", "Alpha"));
        PlexPage page = new PlexPage(items, 0, 120);

        MediaGroup group = PlexMediaGroupAdapter.from(library, items, page);

        assertNotNull(group);
        assertEquals("1", group.getNextPageKey());
    }

    @Test
    public void continueFrom_returnsOnlyNewItemsWithUpdatedKey() {
        PlexLibrary library = new PlexLibraryImpl("1", "Movies", "movie");
        List<PlexMediaItem> firstPageItems = Collections.nCopies(50, movie("10", "Alpha"));
        PlexMediaGroupAdapter base = PlexMediaGroupAdapter.from(
                library,
                firstPageItems,
                new PlexPage(firstPageItems, 0, 120));

        assertNotNull(base);
        List<PlexMediaItem> nextItems = Collections.singletonList(movie("20", "Beta"));
        MediaGroup continuation = PlexMediaGroupAdapter.continueFrom(
                base, nextItems, new PlexPage(nextItems, 50, 120));

        assertNotNull(continuation);
        assertEquals(1, continuation.getMediaItems().size());
        assertEquals("20", continuation.getMediaItems().get(0).getVideoId());
        assertEquals("51", continuation.getNextPageKey());
    }

    @Test
    public void fromLibraryGrid_hasNoBrowseStub() {
        PlexLibrary library = new PlexLibraryImpl("1", "Movies", "movie");
        List<PlexMediaItem> items = Collections.nCopies(50, movie("10", "Alpha"));

        MediaGroup group = PlexMediaGroupAdapter.fromLibraryGrid(
                library, items, new PlexPage(items, 0, 100));

        assertNotNull(group);
        assertEquals(50, group.getMediaItems().size());
        assertEquals("10", group.getMediaItems().get(0).getVideoId());
        assertEquals("50", group.getNextPageKey());
    }

    @Test
    public void getPlexLibrary_returnsUnderlying() {
        PlexLibrary library = new PlexLibraryImpl("7", "Kids", "movie");
        PlexMediaGroupAdapter group = PlexMediaGroupAdapter.from(library, Collections.emptyList());
        assertNotNull(group);
        assertEquals(library, group.getPlexLibrary());
    }

    @Test
    public void fromContainer_mapsTitleAndChildItems() {
        PlexMediaItem show = new PlexMediaItemImpl(
                "2001", "/library/metadata/2001", "Breaking Bad", "show", 0, null, 2008);
        List<PlexMediaItem> items = Collections.singletonList(
                new PlexMediaItemImpl("3001", "/k", "Season 1", "season", 0, null, 2008));

        MediaGroup group = PlexMediaGroupAdapter.fromContainer(show, items);

        assertNotNull(group);
        assertEquals("Breaking Bad", group.getTitle());
        assertEquals("2001", group.getParams());
        assertFalse(group.isEmpty());
        assertEquals(1, group.getMediaItems().size());
        assertEquals("3001", group.getMediaItems().get(0).getPlaylistId());
        assertNull(group.getMediaItems().get(0).getVideoId());
        assertNotNull(((PlexMediaGroupAdapter) group).getPlexContainer());
    }

    private static PlexMediaItem movie(String ratingKey, String title) {
        return new PlexMediaItemImpl(
                ratingKey, "/library/metadata/" + ratingKey, title, "movie", 0, null, 2020);
    }
}
