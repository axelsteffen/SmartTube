package com.liskovsoft.plexserviceinterfaces.data;

public interface PlexServer {
    String getClientIdentifier();

    String getName();

    String getBaseUrl();

    /**
     * Per-server token from plex.tv resources ({@code accessToken}).
     * Prefer this for PMS calls over the account auth token.
     */
    String getAccessToken();

    boolean isOwned();

    boolean isOnline();
}
