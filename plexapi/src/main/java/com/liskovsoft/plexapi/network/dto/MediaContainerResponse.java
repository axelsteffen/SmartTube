package com.liskovsoft.plexapi.network.dto;

import com.google.gson.annotations.SerializedName;

/**
 * Top-level JSON envelope used by PMS when {@code Accept: application/json}.
 */
public class MediaContainerResponse {
    @SerializedName("MediaContainer")
    private MediaContainer mMediaContainer;

    public MediaContainer getMediaContainer() {
        return mMediaContainer;
    }
}
