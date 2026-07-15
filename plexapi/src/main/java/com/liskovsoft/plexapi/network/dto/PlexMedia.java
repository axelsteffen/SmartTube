package com.liskovsoft.plexapi.network.dto;

import com.google.gson.annotations.SerializedName;

import java.util.Collections;
import java.util.List;

/**
 * Media representation under {@link PlexMetadata} (codecs, container, parts).
 */
public class PlexMedia {
    @SerializedName("id")
    private long mId;

    @SerializedName("duration")
    private long mDuration;

    @SerializedName("bitrate")
    private int mBitrate;

    @SerializedName("width")
    private int mWidth;

    @SerializedName("height")
    private int mHeight;

    @SerializedName("container")
    private String mContainer;

    @SerializedName("videoCodec")
    private String mVideoCodec;

    @SerializedName("audioCodec")
    private String mAudioCodec;

    @SerializedName("Part")
    private List<PlexPart> mParts;

    public long getId() {
        return mId;
    }

    public long getDuration() {
        return mDuration;
    }

    public int getBitrate() {
        return mBitrate;
    }

    public int getWidth() {
        return mWidth;
    }

    public int getHeight() {
        return mHeight;
    }

    public String getContainer() {
        return mContainer;
    }

    public String getVideoCodec() {
        return mVideoCodec;
    }

    public String getAudioCodec() {
        return mAudioCodec;
    }

    public List<PlexPart> getParts() {
        return mParts != null ? mParts : Collections.emptyList();
    }
}
