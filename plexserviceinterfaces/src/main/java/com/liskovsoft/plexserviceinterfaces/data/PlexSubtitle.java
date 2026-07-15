package com.liskovsoft.plexserviceinterfaces.data;

/**
 * External (sidecar) subtitle stream from PMS metadata ({@code Stream} with key).
 */
public interface PlexSubtitle {
    /** Absolute download URL including {@code X-Plex-Token}. */
    String getUrl();

    /** BCP-47 / ISO language code when available. */
    String getLanguageCode();

    /** Display label (e.g. "English (SRT)"). */
    String getName();

    /** PMS codec string: {@code srt}, {@code ass}, {@code ssa}, {@code vtt}, … */
    String getCodec();
}
