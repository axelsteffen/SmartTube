package com.liskovsoft.plexserviceinterfaces.data;

public interface PlexMediaItem {
    String getRatingKey();

    String getKey();

    String getTitle();

    String getType();

    long getDurationMs();

    String getThumbUrl();

    int getYear();
}
