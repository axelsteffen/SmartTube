package com.liskovsoft.plexserviceinterfaces.data;

public interface PlexMediaItem {
    String getRatingKey();

    String getKey();

    String getTitle();

    String getType();

    long getDurationMs();

    /** Playback resume offset from PMS ({@code viewOffset}), in milliseconds. */
    long getViewOffsetMs();

    String getThumbUrl();

    int getYear();
}
