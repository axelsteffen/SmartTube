package com.liskovsoft.plexserviceinterfaces.data;

/**
 * Marker for MSC {@code MediaItem} adapters that wrap Plex content.
 * Used by fork UI ({@code Video.from}) to set {@code mediaSource = PLEX}
 * without depending on plexapi implementation classes.
 */
public interface PlexBackedMediaItem {
}
