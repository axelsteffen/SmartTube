package com.liskovsoft.plexapi.adapter;

import androidx.annotation.Nullable;

import com.liskovsoft.mediaserviceinterfaces.data.MediaSubtitle;
import com.liskovsoft.plexserviceinterfaces.data.PlexSubtitle;

/**
 * Maps {@link PlexSubtitle} to MSC {@link MediaSubtitle} for Exo sideloading.
 */
public final class PlexMediaSubtitle implements MediaSubtitle {
    private final String mBaseUrl;
    private final String mLanguageCode;
    private final String mName;
    private final String mMimeType;
    private final String mCodecs;

    private PlexMediaSubtitle(
            String baseUrl,
            @Nullable String languageCode,
            @Nullable String name,
            String mimeType,
            @Nullable String codecs) {
        mBaseUrl = baseUrl;
        mLanguageCode = languageCode;
        mName = name;
        mMimeType = mimeType;
        mCodecs = codecs;
    }

    /**
     * @return mapped subtitle, or {@code null} if URL missing / codec unsupported
     */
    @Nullable
    public static MediaSubtitle from(@Nullable PlexSubtitle subtitle) {
        if (subtitle == null) {
            return null;
        }
        String url = subtitle.getUrl();
        if (url == null || url.isEmpty()) {
            return null;
        }
        String mime = mimeFromCodec(subtitle.getCodec());
        if (mime == null) {
            return null;
        }
        String language = firstNonEmpty(
                subtitle.getLanguageCode(),
                subtitle.getName());
        String name = firstNonEmpty(
                subtitle.getName(),
                subtitle.getLanguageCode(),
                subtitle.getCodec());
        return new PlexMediaSubtitle(url, language, name, mime, subtitle.getCodec());
    }

    /**
     * Maps PMS codec to Exo text sample MIME. Returns {@code null} for unsupported
     * (e.g. image-based PGS).
     */
    @Nullable
    public static String mimeFromCodec(@Nullable String codec) {
        if (codec == null || codec.isEmpty()) {
            return null;
        }
        String value = codec.toLowerCase();
        switch (value) {
            case "srt":
            case "subrip":
                return "application/x-subrip";
            case "ass":
            case "ssa":
                return "text/x-ssa";
            case "vtt":
            case "webvtt":
                return "text/vtt";
            case "ttml":
            case "dfxp":
                return "application/ttml+xml";
            default:
                return null;
        }
    }

    @Nullable
    private static String firstNonEmpty(String... values) {
        if (values == null) {
            return null;
        }
        for (String value : values) {
            if (value != null && !value.isEmpty()) {
                return value;
            }
        }
        return null;
    }

    @Override
    public String getBaseUrl() {
        return mBaseUrl;
    }

    @Override
    public boolean isTranslatable() {
        return false;
    }

    @Override
    public String getLanguageCode() {
        return mLanguageCode;
    }

    @Override
    public String getVssId() {
        return null;
    }

    @Override
    public String getName() {
        return mName;
    }

    @Override
    public String getMimeType() {
        return mMimeType;
    }

    @Override
    public String getCodecs() {
        return mCodecs;
    }

    @Override
    public String getType() {
        return null;
    }
}
