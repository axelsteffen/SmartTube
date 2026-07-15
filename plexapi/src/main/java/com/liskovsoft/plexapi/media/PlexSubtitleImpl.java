package com.liskovsoft.plexapi.media;

import androidx.annotation.Nullable;

import com.liskovsoft.plexserviceinterfaces.data.PlexSubtitle;

/**
 * Immutable external subtitle track resolved from PMS metadata.
 */
public final class PlexSubtitleImpl implements PlexSubtitle {
    private final String mUrl;
    private final String mLanguageCode;
    private final String mName;
    private final String mCodec;

    public PlexSubtitleImpl(String url, @Nullable String languageCode,
                            @Nullable String name, @Nullable String codec) {
        mUrl = url;
        mLanguageCode = languageCode;
        mName = name;
        mCodec = codec;
    }

    @Override
    public String getUrl() {
        return mUrl;
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
}
