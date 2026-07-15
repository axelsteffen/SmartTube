package com.liskovsoft.plexapi.network.dto;

import com.google.gson.annotations.SerializedName;

/**
 * Media part — {@code key} is the Direct Play path under the PMS base URL.
 */
public class PlexPart {
    @SerializedName("id")
    private long mId;

    @SerializedName("key")
    private String mKey;

    @SerializedName("duration")
    private long mDuration;

    @SerializedName("size")
    private long mSize;

    @SerializedName("container")
    private String mContainer;

    @SerializedName("file")
    private String mFile;

    @SerializedName("decision")
    private String mDecision;

    @SerializedName("protocol")
    private String mProtocol;

    public long getId() {
        return mId;
    }

    public String getKey() {
        return mKey;
    }

    public long getDuration() {
        return mDuration;
    }

    public long getSize() {
        return mSize;
    }

    public String getContainer() {
        return mContainer;
    }

    public String getFile() {
        return mFile;
    }

    public String getDecision() {
        return mDecision;
    }

    public String getProtocol() {
        return mProtocol;
    }
}
