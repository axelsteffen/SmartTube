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
    private final List<MediaItem> mMediaItems;

    private PlexMediaGroupAdapter(PlexLibrary library, List<MediaItem> mediaItems) {
        mLibrary = library;
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
        return new PlexMediaGroupAdapter(library, result);
    }

    /** Underlying Plex library (section key for later pagination / drill-down). */
    public PlexLibrary getPlexLibrary() {
        return mLibrary;
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
        return mLibrary.getTitle();
    }

    @Override
    public String getChannelId() {
        return null;
    }

    /** Plex library section key — reserved for continuation / pagination. */
    @Override
    public String getParams() {
        return mLibrary.getKey();
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
