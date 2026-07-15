package com.liskovsoft.plexserviceinterfaces.data;

import java.util.List;

/** One page of Plex library or metadata children (fork-only). */
public interface PlexMediaPage {
    List<PlexMediaItem> getItems();

    int getOffset();

    int getTotalSize();
}
