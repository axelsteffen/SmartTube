package com.liskovsoft.plexapi.network.dto;

import com.google.gson.annotations.SerializedName;

/**
 * Library section (or other directory) entry inside a {@link MediaContainer}.
 */
public class PlexDirectory {
    @SerializedName("key")
    private String mKey;

    @SerializedName("title")
    private String mTitle;

    @SerializedName("type")
    private String mType;

    @SerializedName("agent")
    private String mAgent;

    @SerializedName("uuid")
    private String mUuid;

    @SerializedName("refreshing")
    private Boolean mRefreshing;

    public String getKey() {
        return mKey;
    }

    public String getTitle() {
        return mTitle;
    }

    public String getType() {
        return mType;
    }

    public String getAgent() {
        return mAgent;
    }

    public String getUuid() {
        return mUuid;
    }

    public boolean isRefreshing() {
        return Boolean.TRUE.equals(mRefreshing);
    }
}
