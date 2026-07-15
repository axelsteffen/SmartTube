package com.liskovsoft.plexserviceinterfaces.data;

import java.util.List;

/**
 * Playable stream metadata for ExoPlayer (Direct Play or HLS transcode).
 */
public interface PlexStreamInfo {
    String getUrl();

    /** Container/MIME hint, e.g. video/mp4 or application/x-mpegURL. */
    String getContainer();

    boolean isTranscoded();

    /** PMS {@code viewOffset} captured with metadata (ms); 0 if unknown. */
    long getViewOffsetMs();

    /** External sidecar subtitles (empty if none / unsupported codecs only). */
    List<PlexSubtitle> getSubtitles();

    /** Audio streams from metadata (empty if none). */
    List<PlexAudioTrack> getAudioTracks();

    /**
     * Audio stream id chosen for this resolve (preferred / override / selected).
     * {@code 0} if none / Direct Play without preference applied at PMS.
     */
    long getSelectedAudioStreamId();
}
