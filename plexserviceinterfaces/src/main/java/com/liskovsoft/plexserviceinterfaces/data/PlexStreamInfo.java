package com.liskovsoft.plexserviceinterfaces.data;

/**
 * Playable stream metadata for ExoPlayer (Direct Play or HLS transcode).
 */
public interface PlexStreamInfo {
    String getUrl();

    /** Container/MIME hint, e.g. video/mp4 or application/x-mpegURL. */
    String getContainer();

    boolean isTranscoded();
}
