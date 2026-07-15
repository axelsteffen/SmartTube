package com.liskovsoft.plexserviceinterfaces.data;

public interface PlexServer {
    String getClientIdentifier();

    String getName();

    String getBaseUrl();

    boolean isOwned();

    boolean isOnline();
}
