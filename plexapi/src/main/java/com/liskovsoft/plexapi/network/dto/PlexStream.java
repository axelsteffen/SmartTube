package com.liskovsoft.plexapi.network.dto;

import com.google.gson.annotations.SerializedName;

/**
 * Media stream under a {@link PlexPart} (video / audio / subtitle).
 * Subtitle streams often expose a {@code key} for external sidecar files.
 */
public class PlexStream {
    /** PMS: 1 = video, 2 = audio, 3 = subtitle. */
    public static final int TYPE_VIDEO = 1;
    public static final int TYPE_AUDIO = 2;
    public static final int TYPE_SUBTITLE = 3;

    @SerializedName("id")
    private long mId;

    @SerializedName("streamType")
    private int mStreamType;

    @SerializedName("codec")
    private String mCodec;

    @SerializedName("language")
    private String mLanguage;

    @SerializedName("languageCode")
    private String mLanguageCode;

    @SerializedName("languageTag")
    private String mLanguageTag;

    @SerializedName("displayTitle")
    private String mDisplayTitle;

    @SerializedName("title")
    private String mTitle;

    @SerializedName("key")
    private String mKey;

    @SerializedName("selected")
    private boolean mSelected;

    @SerializedName("channels")
    private int mChannels;

    public long getId() {
        return mId;
    }

    public int getStreamType() {
        return mStreamType;
    }

    public String getCodec() {
        return mCodec;
    }

    public String getLanguage() {
        return mLanguage;
    }

    public String getLanguageCode() {
        return mLanguageCode;
    }

    public String getLanguageTag() {
        return mLanguageTag;
    }

    public String getDisplayTitle() {
        return mDisplayTitle;
    }

    public String getTitle() {
        return mTitle;
    }

    public String getKey() {
        return mKey;
    }

    public boolean isSelected() {
        return mSelected;
    }

    public int getChannels() {
        return mChannels;
    }
}
