package com.liskovsoft.plexapi.auth;

import com.liskovsoft.plexserviceinterfaces.data.PlexAuthPin;

public final class PlexAuthPinImpl implements PlexAuthPin {
    public static final String DEFAULT_AUTH_URL = "https://plex.tv/link";

    private final String mId;
    private final String mCode;
    private final String mAuthUrl;

    public PlexAuthPinImpl(String id, String code) {
        this(id, code, DEFAULT_AUTH_URL);
    }

    public PlexAuthPinImpl(String id, String code, String authUrl) {
        mId = id;
        mCode = code;
        mAuthUrl = authUrl != null ? authUrl : DEFAULT_AUTH_URL;
    }

    @Override
    public String getId() {
        return mId;
    }

    @Override
    public String getCode() {
        return mCode;
    }

    @Override
    public String getAuthUrl() {
        return mAuthUrl;
    }
}
