package com.liskovsoft.plexapi.adapter;

import android.media.Rating;

import androidx.annotation.Nullable;

import com.liskovsoft.mediaserviceinterfaces.data.MediaItem;
import com.liskovsoft.plexserviceinterfaces.data.PlexMediaItem;

/**
 * Fork-only adapter: wraps {@link PlexMediaItem} as MSC {@link MediaItem}
 * so existing UI ({@code Video.from(MediaItem)}) can consume Plex items.
 * <p>
 * {@link #getVideoId()} maps to Plex {@code ratingKey} for later stream/playback routing.
 */
public final class PlexMediaItemAdapter implements MediaItem {
    private static final String TYPE_MOVIE = "movie";

    private final PlexMediaItem mItem;
    private final int mId;

    private PlexMediaItemAdapter(PlexMediaItem item) {
        mItem = item;
        String ratingKey = item.getRatingKey();
        mId = ratingKey != null ? Math.abs(ratingKey.hashCode()) : 0;
    }

    @Nullable
    public static PlexMediaItemAdapter from(@Nullable PlexMediaItem item) {
        if (item == null || item.getRatingKey() == null || item.getRatingKey().isEmpty()) {
            return null;
        }
        return new PlexMediaItemAdapter(item);
    }

    /** Underlying Plex domain item (for stream lookup by ratingKey). */
    public PlexMediaItem getPlexItem() {
        return mItem;
    }

    @Override
    public int getType() {
        return TYPE_VIDEO;
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
        return -1;
    }

    @Override
    public int getStartTimeSeconds() {
        return -1;
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
        return null;
    }

    @Override
    public int getPlaylistIndex() {
        return -1;
    }

    @Override
    public String getParams() {
        return null;
    }

    @Override
    public String getReloadPageKey() {
        return null;
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
        return false;
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
            String videoId = getVideoId();
            return videoId != null && videoId.equals(((MediaItem) obj).getVideoId());
        }
        return false;
    }

    @Override
    public int hashCode() {
        String videoId = getVideoId();
        return videoId != null ? videoId.hashCode() : 0;
    }
}
