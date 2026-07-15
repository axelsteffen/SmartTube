package com.liskovsoft.plexserviceinterfaces.data;

public interface PlexAuthPin {
    String getId();

    /** Short code shown to the user (e.g. for plex.tv/link). */
    String getCode();

    String getAuthUrl();
}
