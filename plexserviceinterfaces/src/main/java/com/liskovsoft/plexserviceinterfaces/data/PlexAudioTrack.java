package com.liskovsoft.plexserviceinterfaces.data;

/**
 * Audio stream from PMS metadata ({@code Stream} with {@code streamType=2}).
 */
public interface PlexAudioTrack {
    /** PMS stream id (used as {@code audioStreamID} on decision). */
    long getId();

    /** BCP-47 / ISO language code when available. */
    String getLanguageCode();

    /** Display label (e.g. "English (AAC 5.1)"). */
    String getName();

    /** PMS codec string, e.g. {@code aac}, {@code ac3}. */
    String getCodec();

    /** Channel count when known; 0 if unknown. */
    int getChannels();

    /** Whether PMS marks this track as currently selected. */
    boolean isSelected();
}
