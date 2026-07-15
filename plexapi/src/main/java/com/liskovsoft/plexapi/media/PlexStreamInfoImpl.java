package com.liskovsoft.plexapi.media;

import com.liskovsoft.plexserviceinterfaces.data.PlexStreamInfo;
import com.liskovsoft.plexserviceinterfaces.data.PlexSubtitle;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Immutable {@link PlexStreamInfo} for Direct Play or transcoded streams.
 */
public final class PlexStreamInfoImpl implements PlexStreamInfo {
    private final String mUrl;
    private final String mContainer;
    private final boolean mTranscoded;
    private final long mViewOffsetMs;
    private final List<PlexSubtitle> mSubtitles;

    public PlexStreamInfoImpl(String url, String container, boolean transcoded) {
        this(url, container, transcoded, 0L, Collections.emptyList());
    }

    public PlexStreamInfoImpl(String url, String container, boolean transcoded, long viewOffsetMs) {
        this(url, container, transcoded, viewOffsetMs, Collections.emptyList());
    }

    public PlexStreamInfoImpl(
            String url,
            String container,
            boolean transcoded,
            long viewOffsetMs,
            List<PlexSubtitle> subtitles) {
        mUrl = url;
        mContainer = container;
        mTranscoded = transcoded;
        mViewOffsetMs = Math.max(0L, viewOffsetMs);
        if (subtitles == null || subtitles.isEmpty()) {
            mSubtitles = Collections.emptyList();
        } else {
            mSubtitles = Collections.unmodifiableList(new ArrayList<>(subtitles));
        }
    }

    @Override
    public String getUrl() {
        return mUrl;
    }

    @Override
    public String getContainer() {
        return mContainer;
    }

    @Override
    public boolean isTranscoded() {
        return mTranscoded;
    }

    @Override
    public long getViewOffsetMs() {
        return mViewOffsetMs;
    }

    @Override
    public List<PlexSubtitle> getSubtitles() {
        return mSubtitles;
    }

    /** Maps PMS container / protocol strings to MIME-like hints for ExoPlayer. */
    public static String mimeHint(String containerOrProtocol) {
        if (containerOrProtocol == null || containerOrProtocol.isEmpty()) {
            return "application/octet-stream";
        }
        String value = containerOrProtocol.toLowerCase();
        if ("hls".equals(value) || "m3u8".equals(value)) {
            return "application/x-mpegURL";
        }
        if ("dash".equals(value) || "mpd".equals(value)) {
            return "application/dash+xml";
        }
        if ("mp4".equals(value) || "m4v".equals(value)) {
            return "video/mp4";
        }
        if ("mkv".equals(value)) {
            return "video/x-matroska";
        }
        if ("webm".equals(value)) {
            return "video/webm";
        }
        if (value.contains("/")) {
            return containerOrProtocol;
        }
        return "video/" + value;
    }
}
