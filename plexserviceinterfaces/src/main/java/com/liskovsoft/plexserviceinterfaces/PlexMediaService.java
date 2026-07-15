package com.liskovsoft.plexserviceinterfaces;

import androidx.annotation.Nullable;

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
     * Resolves stream URL with optional audio track override (Phase 4.3).
     *
     * @param audioStreamId      PMS audio stream id, or {@code null} to auto-pick
     * @param preferredLanguage  preferred ISO language (e.g. from player prefs), or {@code null}
     */
    Observable<PlexStreamInfo> getStreamInfoObserve(
            PlexMediaItem item,
            @Nullable Long audioStreamId,
            @Nullable String preferredLanguage);

    /**
     * Resolves stream URL with optional audio override and forced transcode (Phase 4.5).
     *
     * @param forceTranscode when {@code true}, skip Direct Play and request HLS via decision
     *                       with {@code directPlay=0}/{@code directStream=0}
     */
    Observable<PlexStreamInfo> getStreamInfoObserve(
            PlexMediaItem item,
            @Nullable Long audioStreamId,
            @Nullable String preferredLanguage,
            boolean forceTranscode);

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
