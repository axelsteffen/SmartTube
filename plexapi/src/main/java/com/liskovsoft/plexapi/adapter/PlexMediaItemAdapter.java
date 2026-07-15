package com.liskovsoft.plexapi.adapter;

import android.media.Rating;

import androidx.annotation.Nullable;

import com.liskovsoft.mediaserviceinterfaces.data.MediaItem;
import com.liskovsoft.plexapi.library.PlexMediaItemImpl;
import com.liskovsoft.plexserviceinterfaces.data.PlexBackedMediaItem;
import com.liskovsoft.plexserviceinterfaces.data.PlexLibrary;
import com.liskovsoft.plexserviceinterfaces.data.PlexMediaItem;

/**
 * Fork-only adapter: wraps {@link PlexMediaItem} as MSC {@link MediaItem}
 * so existing UI ({@code Video.from(MediaItem)}) can consume Plex items.
 * <p>
 * {@link #getVideoId()} maps to Plex {@code ratingKey} for later stream/playback routing.
 * Implements {@link PlexBackedMediaItem} so {@code Video.from} tags {@code mediaSource = PLEX}.
 */
public final class PlexMediaItemAdapter implements MediaItem, PlexBackedMediaItem {
    private static final String TYPE_MOVIE = "movie";
    private static final String TYPE_SHOW = "show";
    private static final String TYPE_SEASON = "season";
    /** Opens full library grid via {@link #getReloadPageKey()} (Phase 3.4). */
    private static final String TYPE_LIBRARY = "library";

    private final PlexMediaItem mItem;
    private final int mId;
    @Nullable
    private final String mLibraryType;

    private PlexMediaItemAdapter(PlexMediaItem item, @Nullable String libraryType) {
        mItem = item;
        mLibraryType = libraryType;
        String ratingKey = item.getRatingKey();
        mId = ratingKey != null ? Math.abs(ratingKey.hashCode()) : 0;
    }

    @Nullable
    public static PlexMediaItemAdapter from(@Nullable PlexMediaItem item) {
        if (item == null || item.getRatingKey() == null || item.getRatingKey().isEmpty()) {
            return null;
        }
        return new PlexMediaItemAdapter(item, null);
    }

    /**
     * First card in a Plex library row — opens the full paginated library grid.
     */
    @Nullable
    public static PlexMediaItemAdapter fromLibraryBrowse(@Nullable PlexLibrary library) {
        if (library == null || library.getKey() == null || library.getKey().isEmpty()) {
            return null;
        }
        PlexMediaItem stub = new PlexMediaItemImpl(
                library.getKey(),
                library.getKey(),
                library.getTitle(),
                TYPE_LIBRARY,
                0L,
                null,
                0);
        return new PlexMediaItemAdapter(stub, library.getType());
    }

    /** Underlying Plex domain item (for stream lookup by ratingKey). */
    public PlexMediaItem getPlexItem() {
        return mItem;
    }

    @Override
    public int getType() {
        return isContainer() ? TYPE_PLAYLIST : TYPE_VIDEO;
    }

    @Override
    public boolean isLive() {
        return false;
    }

    @Override
    public boolean isUpcoming() {
        return false;
    }

    @Override
    public boolean isShorts() {
        return false;
    }

    @Override
    public int getPercentWatched() {
        long durationMs = mItem.getDurationMs();
        long viewOffsetMs = mItem.getViewOffsetMs();
        if (durationMs <= 0L || viewOffsetMs <= 0L) {
            return -1;
        }
        int percent = (int) Math.min(100, (viewOffsetMs * 100L) / durationMs);
        return percent > 0 ? percent : -1;
    }

    @Override
    public int getStartTimeSeconds() {
        long viewOffsetMs = mItem.getViewOffsetMs();
        if (viewOffsetMs <= 0L) {
            return -1;
        }
        return (int) (viewOffsetMs / 1000L);
    }

    @Override
    public String getAuthor() {
        return null;
    }

    @Override
    public String getFeedbackToken() {
        return null;
    }

    @Override
    public String getFeedbackToken2() {
        return null;
    }

    @Override
    public String getPlaylistId() {
        return isContainer() ? mItem.getRatingKey() : null;
    }

    @Override
    public int getPlaylistIndex() {
        return -1;
    }

    @Override
    public String getParams() {
        return isLibraryBrowse() ? mLibraryType : null;
    }

    @Override
    public String getReloadPageKey() {
        return isLibraryBrowse() ? mItem.getRatingKey() : null;
    }

    @Override
    public boolean hasNewContent() {
        return false;
    }

    @Override
    public int getId() {
        return mId;
    }

    @Override
    public String getTitle() {
        return mItem.getTitle();
    }

    @Override
    public CharSequence getSecondTitle() {
        int year = mItem.getYear();
        return year > 0 ? String.valueOf(year) : null;
    }

    @Override
    public String getVideoId() {
        if (isLibraryBrowse() || isContainer()) {
            return null;
        }
        return mItem.getRatingKey();
    }

    @Override
    public String getContentType() {
        return null;
    }

    @Override
    public long getDurationMs() {
        return mItem.getDurationMs();
    }

    @Override
    public String getBadgeText() {
        return null;
    }

    @Override
    public String getProductionDate() {
        int year = mItem.getYear();
        return year > 0 ? String.valueOf(year) : null;
    }

    @Override
    public long getPublishedDate() {
        return -1;
    }

    @Override
    public String getCardImageUrl() {
        return mItem.getThumbUrl();
    }

    @Override
    public String getBackgroundImageUrl() {
        return mItem.getThumbUrl();
    }

    @Override
    public int getWidth() {
        return 1280;
    }

    @Override
    public int getHeight() {
        return 720;
    }

    @Override
    public String getChannelId() {
        return null;
    }

    @Override
    public String getVideoPreviewUrl() {
        return null;
    }

    @Override
    public String getAudioChannelConfig() {
        return "2.0";
    }

    @Override
    public String getPurchasePrice() {
        return "$0.00";
    }

    @Override
    public String getRentalPrice() {
        return "$0.00";
    }

    @Override
    public int getRatingStyle() {
        return Rating.RATING_5_STARS;
    }

    @Override
    public double getRatingScore() {
        return 0;
    }

    @Override
    public boolean isMovie() {
        return TYPE_MOVIE.equalsIgnoreCase(mItem.getType());
    }

    @Override
    public boolean hasUploads() {
        return isContainer() || isLibraryBrowse();
    }

    @Override
    public String getClickTrackingParams() {
        return null;
    }

    @Override
    public String getSearchQuery() {
        return null;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof MediaItem) {
            MediaItem other = (MediaItem) obj;
            if (isContainer()) {
                String playlistId = getPlaylistId();
                return playlistId != null && playlistId.equals(other.getPlaylistId());
            }
            String videoId = getVideoId();
            return videoId != null && videoId.equals(other.getVideoId());
        }
        return false;
    }

    private boolean isContainer() {
        String type = mItem.getType();
        return TYPE_SHOW.equalsIgnoreCase(type) || TYPE_SEASON.equalsIgnoreCase(type);
    }

    private boolean isLibraryBrowse() {
        return TYPE_LIBRARY.equalsIgnoreCase(mItem.getType());
    }

    @Override
    public int hashCode() {
        if (isContainer()) {
            String playlistId = getPlaylistId();
            return playlistId != null ? playlistId.hashCode() : 0;
        }
        String videoId = getVideoId();
        return videoId != null ? videoId.hashCode() : 0;
    }
}
