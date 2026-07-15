package com.liskovsoft.plexserviceinterfaces;

import com.liskovsoft.plexserviceinterfaces.data.PlexMediaItem;
import com.liskovsoft.plexserviceinterfaces.data.PlexStreamInfo;

import io.reactivex.Observable;

/**
 * Resolve playable stream URLs and progress for a Plex media item.
 */
public interface PlexMediaService {
    /** Playback state for PMS {@code /:/timeline}. */
    String STATE_PLAYING = "playing";
    String STATE_PAUSED = "paused";
    String STATE_STOPPED = "stopped";

    Observable<PlexStreamInfo> getStreamInfoObserve(PlexMediaItem item);

    /**
     * Reports playback progress to PMS ({@code /:/timeline}).
     *
     * @param positionMs current position in ms
     * @param durationMs item duration in ms
     * @param state      {@link #STATE_PLAYING}, {@link #STATE_PAUSED}, or {@link #STATE_STOPPED}
     */
    Observable<Void> updateProgressObserve(
            PlexMediaItem item, long positionMs, long durationMs, String state);
}
