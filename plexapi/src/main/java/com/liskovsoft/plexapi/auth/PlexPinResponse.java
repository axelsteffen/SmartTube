package com.liskovsoft.plexapi.auth;

import com.google.gson.annotations.SerializedName;

/**
 * Gson DTO for plex.tv {@code /api/v2/pins} responses.
 */
public class PlexPinResponse {
    @SerializedName("id")
    private long mId;

    @SerializedName("code")
    private String mCode;

    @SerializedName("authToken")
    private String mAuthToken;

    @SerializedName("expiresIn")
    private int mExpiresIn;

    public long getId() {
        return mId;
    }

    public String getCode() {
        return mCode;
    }

    public String getAuthToken() {
        return mAuthToken;
    }

    public int getExpiresIn() {
        return mExpiresIn;
    }

    public boolean hasAuthToken() {
        return mAuthToken != null && !mAuthToken.isEmpty();
    }
}
