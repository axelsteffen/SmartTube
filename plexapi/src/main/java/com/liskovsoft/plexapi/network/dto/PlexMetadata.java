package com.liskovsoft.plexapi.network.dto;

import com.google.gson.annotations.SerializedName;

import java.util.Collections;
import java.util.List;

/**
 * Metadata item (movie, show, episode, …).
 */
public class PlexMetadata {
    @SerializedName("ratingKey")
    private String mRatingKey;

    @SerializedName("key")
    private String mKey;

    @SerializedName("type")
    private String mType;

    @SerializedName("title")
    private String mTitle;

    @SerializedName("summary")
    private String mSummary;

    @SerializedName("year")
    private int mYear;

    @SerializedName("duration")
    private long mDuration;

    /** Resume offset in milliseconds (PMS {@code viewOffset}). */
    @SerializedName("viewOffset")
    private long mViewOffset;

    @SerializedName("thumb")
    private String mThumb;

    @SerializedName("art")
    private String mArt;

    @SerializedName("Media")
    private List<PlexMedia> mMedia;

    public String getRatingKey() {
        return mRatingKey;
    }

    public String getKey() {
        return mKey;
    }

    public String getType() {
        return mType;
    }

    public String getTitle() {
        return mTitle;
    }

    public String getSummary() {
        return mSummary;
    }

    public int getYear() {
        return mYear;
    }

    public long getDuration() {
        return mDuration;
    }

    public long getViewOffset() {
        return mViewOffset;
    }

    public String getThumb() {
        return mThumb;
    }

    public String getArt() {
        return mArt;
    }

    public List<PlexMedia> getMedia() {
        return mMedia != null ? mMedia : Collections.emptyList();
    }
}
