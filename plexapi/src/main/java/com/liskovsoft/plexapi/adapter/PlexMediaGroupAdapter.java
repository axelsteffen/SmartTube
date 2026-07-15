package com.liskovsoft.plexapi.adapter;

import androidx.annotation.Nullable;

import com.liskovsoft.mediaserviceinterfaces.data.MediaGroup;
import com.liskovsoft.mediaserviceinterfaces.data.MediaItem;
import com.liskovsoft.plexserviceinterfaces.data.PlexLibrary;
import com.liskovsoft.plexserviceinterfaces.data.PlexMediaItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Fork-only adapter: wraps a Plex library movie page as MSC {@link MediaGroup}
 * so existing UI ({@code VideoGroup.from(MediaGroup)}) can consume it.
 * <p>
 * Items are adapted via {@link PlexMediaItemAdapter}. Pagination keys are unused
 * for the first-page PoC (library key is exposed via {@link #getParams()}).
 */
public final class PlexMediaGroupAdapter implements MediaGroup {
    private final PlexLibrary mLibrary;
    private final PlexMediaItem mContainer;
    private final List<MediaItem> mMediaItems;

    private PlexMediaGroupAdapter(@Nullable PlexLibrary library,
                                  @Nullable PlexMediaItem container,
                                  List<MediaItem> mediaItems) {
        mLibrary = library;
        mContainer = container;
        mMediaItems = mediaItems;
    }

    /**
     * @return adapter, or {@code null} if {@code library} is null / has no key
     */
    @Nullable
    public static PlexMediaGroupAdapter from(@Nullable PlexLibrary library,
                                             @Nullable List<PlexMediaItem> items) {
        if (library == null || library.getKey() == null || library.getKey().isEmpty()) {
            return null;
        }

        ArrayList<MediaItem> mediaItems = new ArrayList<>();
        if (items != null) {
            for (PlexMediaItem item : items) {
                MediaItem adapted = PlexMediaItemAdapter.from(item);
                if (adapted != null) {
                    mediaItems.add(adapted);
                }
            }
        }

        // Match YouTubeMediaGroup: null when empty (avoids duplicate-append quirks)
        List<MediaItem> result = mediaItems.isEmpty() ? null : mediaItems;
        return new PlexMediaGroupAdapter(library, null, result);
    }

    /**
     * Children of a show or season (Phase 3.3 drill-down).
     */
    @Nullable
    public static PlexMediaGroupAdapter fromContainer(@Nullable PlexMediaItem container,
                                                      @Nullable List<PlexMediaItem> items) {
        if (container == null || container.getRatingKey() == null || container.getRatingKey().isEmpty()) {
            return null;
        }

        ArrayList<MediaItem> mediaItems = new ArrayList<>();
        if (items != null) {
            for (PlexMediaItem item : items) {
                MediaItem adapted = PlexMediaItemAdapter.from(item);
                if (adapted != null) {
                    mediaItems.add(adapted);
                }
            }
        }

        List<MediaItem> result = mediaItems.isEmpty() ? null : mediaItems;
        return new PlexMediaGroupAdapter(null, container, result);
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
        return null;
    }

    @Override
    public String getChannelUrl() {
        return null;
    }

    @Override
    public boolean isEmpty() {
        return mMediaItems == null || mMediaItems.isEmpty();
    }
}
