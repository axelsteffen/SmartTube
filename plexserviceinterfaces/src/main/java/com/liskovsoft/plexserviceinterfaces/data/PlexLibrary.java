package com.liskovsoft.plexserviceinterfaces.data;

public interface PlexLibrary {
    String getKey();

    String getTitle();

    /** Plex section type, e.g. movie, show, artist. */
    String getType();
}
