package com.liskovsoft.plexapi.media;

import androidx.annotation.Nullable;

import com.liskovsoft.plexserviceinterfaces.data.PlexAudioTrack;

/**
 * Immutable audio track from PMS metadata.
 */
public final class PlexAudioTrackImpl implements PlexAudioTrack {
    private final long mId;
    private final String mLanguageCode;
    private final String mName;
    private final String mCodec;
    private final int mChannels;
    private final boolean mSelected;

    public PlexAudioTrackImpl(
            long id,
            @Nullable String languageCode,
            @Nullable String name,
            @Nullable String codec,
            int channels,
            boolean selected) {
        mId = id;
        mLanguageCode = languageCode;
        mName = name;
        mCodec = codec;
        mChannels = Math.max(0, channels);
        mSelected = selected;
    }

    @Override
    public long getId() {
        return mId;
    }

    @Override
    public String getLanguageCode() {
        return mLanguageCode;
    }

    @Override
    public String getName() {
        return mName;
    }

    @Override
    public String getCodec() {
        return mCodec;
    }

    @Override
    public int getChannels() {
        return mChannels;
    }

    @Override
    public boolean isSelected() {
        return mSelected;
    }
}
