package com.liskovsoft.plexapi.adapter;

import androidx.annotation.Nullable;

import com.liskovsoft.mediaserviceinterfaces.data.MediaGroup;
import com.liskovsoft.mediaserviceinterfaces.data.MediaItem;
import com.liskovsoft.plexapi.library.PlexPage;
import com.liskovsoft.plexserviceinterfaces.data.PlexLibrary;
import com.liskovsoft.plexserviceinterfaces.data.PlexMediaItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Fork-only adapter: wraps a Plex library movie page as MSC {@link MediaGroup}
 * so existing UI ({@code VideoGroup.from(MediaGroup)}) can consume it.
 * <p>
 * Items are adapted via {@link PlexMediaItemAdapter}. Pagination uses
 * {@link #getNextPageKey()} as the next PMS offset (string).
 */
public final class PlexMediaGroupAdapter implements MediaGroup {
    private static final String TYPE_LIBRARY_STUB = "library";

    private final PlexLibrary mLibrary;
    private final PlexMediaItem mContainer;
    private final List<MediaItem> mMediaItems;
    private final String mNextPageKey;

    private PlexMediaGroupAdapter(@Nullable PlexLibrary library,
                                  @Nullable PlexMediaItem container,
                                  List<MediaItem> mediaItems,
                                  @Nullable String nextPageKey) {
        mLibrary = library;
        mContainer = container;
        mMediaItems = mediaItems;
        mNextPageKey = nextPageKey;
    }

    /**
     * @return adapter, or {@code null} if {@code library} is null / has no key
     */
    @Nullable
    public static PlexMediaGroupAdapter from(@Nullable PlexLibrary library,
                                             @Nullable List<PlexMediaItem> items) {
        return from(library, items, null);
    }

    @Nullable
    public static PlexMediaGroupAdapter from(@Nullable PlexLibrary library,
                                             @Nullable List<PlexMediaItem> items,
                                             @Nullable PlexPage page) {
        if (library == null || library.getKey() == null || library.getKey().isEmpty()) {
            return null;
        }

        ArrayList<MediaItem> mediaItems = new ArrayList<>();
        MediaItem browseStub = PlexMediaItemAdapter.fromLibraryBrowse(library);
        if (browseStub != null) {
            mediaItems.add(browseStub);
        }

        appendItems(mediaItems, items);

        List<MediaItem> result = mediaItems.isEmpty() ? null : mediaItems;
        return new PlexMediaGroupAdapter(library, null, result, nextPageKeyFrom(page));
    }

    /**
     * Full-library grid (Phase 3.4): same items as a row but without the browse stub.
     */
    @Nullable
    public static PlexMediaGroupAdapter fromLibraryGrid(@Nullable PlexLibrary library,
                                                        @Nullable List<PlexMediaItem> items,
                                                        @Nullable PlexPage page) {
        if (library == null || library.getKey() == null || library.getKey().isEmpty()) {
            return null;
        }

        ArrayList<MediaItem> mediaItems = new ArrayList<>();
        appendItems(mediaItems, items);

        List<MediaItem> result = mediaItems.isEmpty() ? null : mediaItems;
        return new PlexMediaGroupAdapter(library, null, result, nextPageKeyFrom(page));
    }

    /**
     * Children of a show or season (Phase 3.3 drill-down).
     */
    @Nullable
    public static PlexMediaGroupAdapter fromContainer(@Nullable PlexMediaItem container,
                                                      @Nullable List<PlexMediaItem> items) {
        return fromContainer(container, items, null);
    }

    @Nullable
    public static PlexMediaGroupAdapter fromContainer(@Nullable PlexMediaItem container,
                                                      @Nullable List<PlexMediaItem> items,
                                                      @Nullable PlexPage page) {
        if (container == null || container.getRatingKey() == null || container.getRatingKey().isEmpty()) {
            return null;
        }

        ArrayList<MediaItem> mediaItems = new ArrayList<>();
        appendItems(mediaItems, items);

        List<MediaItem> result = mediaItems.isEmpty() ? null : mediaItems;
        return new PlexMediaGroupAdapter(null, container, result, nextPageKeyFrom(page));
    }

    /**
     * Continuation page: new items only, with updated {@link #getNextPageKey()}.
     */
    @Nullable
    public static PlexMediaGroupAdapter continueFrom(@Nullable PlexMediaGroupAdapter base,
                                                     @Nullable List<PlexMediaItem> items,
                                                     @Nullable PlexPage page) {
        if (base == null) {
            return null;
        }

        ArrayList<MediaItem> mediaItems = new ArrayList<>();
        appendItems(mediaItems, items);

        if (mediaItems.isEmpty()) {
            return null;
        }

        return new PlexMediaGroupAdapter(
                base.mLibrary,
                base.mContainer,
                mediaItems,
                nextPageKeyFrom(page));
    }

    /** Underlying Plex library (section key for later pagination / drill-down). */
    public PlexLibrary getPlexLibrary() {
        return mLibrary;
    }

    /** Parent show/season when this group lists children. */
    @Nullable
    public PlexMediaItem getPlexContainer() {
        return mContainer;
    }

    public boolean isLibraryGroup() {
        return mLibrary != null;
    }

    public boolean isContainerGroup() {
        return mContainer != null;
    }

    @Override
    public int getType() {
        return TYPE_MOVIES;
    }

    @Nullable
    @Override
    public List<MediaItem> getMediaItems() {
        return mMediaItems;
    }

    @Override
    public String getTitle() {
        if (mLibrary != null) {
            return mLibrary.getTitle();
        }
        return mContainer != null ? mContainer.getTitle() : null;
    }

    @Override
    public String getChannelId() {
        return null;
    }

    /** Plex library section key or parent ratingKey — reserved for pagination. */
    @Override
    public String getParams() {
        if (mLibrary != null) {
            return mLibrary.getKey();
        }
        return mContainer != null ? mContainer.getRatingKey() : null;
    }

    @Override
    public String getReloadPageKey() {
        return null;
    }

    @Override
    public String getNextPageKey() {
        return mNextPageKey;
    }

    @Override
    public String getChannelUrl() {
        return null;
    }

    @Override
    public boolean isEmpty() {
        return mMediaItems == null || mMediaItems.isEmpty();
    }

    @Nullable
    private static String nextPageKeyFrom(@Nullable PlexPage page) {
        if (page == null) {
            return null;
        }
        int nextOffset = page.getNextOffset();
        return nextOffset >= 0 ? String.valueOf(nextOffset) : null;
    }

    private static void appendItems(ArrayList<MediaItem> mediaItems, @Nullable List<PlexMediaItem> items) {
        if (items == null) {
            return;
        }
        for (PlexMediaItem item : items) {
            if (TYPE_LIBRARY_STUB.equalsIgnoreCase(item.getType())) {
                continue;
            }
            MediaItem adapted = PlexMediaItemAdapter.from(item);
            if (adapted != null) {
                mediaItems.add(adapted);
            }
        }
    }
}
