package com.liskovsoft.plexapi.adapter;

import androidx.annotation.Nullable;

import com.liskovsoft.mediaserviceinterfaces.data.MediaFormat;
import com.liskovsoft.mediaserviceinterfaces.data.MediaItemFormatInfo;
import com.liskovsoft.mediaserviceinterfaces.data.MediaItemStoryboard;
import com.liskovsoft.mediaserviceinterfaces.data.MediaSubtitle;
import com.liskovsoft.plexserviceinterfaces.data.PlexMediaItem;
import com.liskovsoft.plexserviceinterfaces.data.PlexStreamInfo;
import com.liskovsoft.plexserviceinterfaces.data.PlexSubtitle;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.reactivex.Observable;

/**
 * Fork-only adapter: maps {@link PlexStreamInfo} (+ item metadata) to MSC
 * {@link MediaItemFormatInfo} so existing ExoPlayer loaders can open the stream.
 * <p>
 * Direct Play → UrlFormats ({@code openUrlList}).<br>
 * Transcoded HLS → {@code getHlsManifestUrl()} (VOD routing hook in step 2.5).
 */
public final class PlexMediaItemFormatInfo implements MediaItemFormatInfo {
    private static final String HLS_MIME = "application/x-mpegURL";

    private final String mVideoId;
    private final String mTitle;
    private final String mLengthSeconds;
    private final String mStreamUrl;
    private final String mContainer;
    private final boolean mTranscoded;
    private final List<MediaFormat> mUrlFormats;
    private final List<MediaSubtitle> mSubtitles;
    private String mClickTrackingParams;

    private PlexMediaItemFormatInfo(
            String videoId,
            String title,
            String lengthSeconds,
            String streamUrl,
            String container,
            boolean transcoded,
            List<MediaFormat> urlFormats,
            List<MediaSubtitle> subtitles) {
        mVideoId = videoId;
        mTitle = title;
        mLengthSeconds = lengthSeconds;
        mStreamUrl = streamUrl;
        mContainer = container;
        mTranscoded = transcoded;
        mUrlFormats = urlFormats;
        mSubtitles = subtitles;
    }

    /**
     * @return null if item/stream missing or stream URL empty
     */
    @Nullable
    public static PlexMediaItemFormatInfo from(
            @Nullable PlexMediaItem item, @Nullable PlexStreamInfo stream) {
        if (item == null || stream == null) {
            return null;
        }
        String url = stream.getUrl();
        if (url == null || url.isEmpty()) {
            return null;
        }
        String ratingKey = item.getRatingKey();
        if (ratingKey == null || ratingKey.isEmpty()) {
            return null;
        }

        boolean transcoded = stream.isTranscoded() || isHlsMime(stream.getContainer());
        List<MediaFormat> urlFormats = transcoded
                ? null
                : PlexMediaFormat.singletonList(url, stream.getContainer());

        long durationMs = item.getDurationMs();
        String lengthSeconds = durationMs > 0
                ? String.valueOf(durationMs / 1000L)
                : null;

        return new PlexMediaItemFormatInfo(
                ratingKey,
                item.getTitle(),
                lengthSeconds,
                url,
                stream.getContainer(),
                transcoded,
                urlFormats,
                mapSubtitles(stream.getSubtitles()));
    }

    private static List<MediaSubtitle> mapSubtitles(@Nullable List<PlexSubtitle> plexSubtitles) {
        if (plexSubtitles == null || plexSubtitles.isEmpty()) {
            return null;
        }
        List<MediaSubtitle> mapped = new ArrayList<>(plexSubtitles.size());
        for (PlexSubtitle plexSubtitle : plexSubtitles) {
            MediaSubtitle subtitle = PlexMediaSubtitle.from(plexSubtitle);
            if (subtitle != null) {
                mapped.add(subtitle);
            }
        }
        return mapped.isEmpty() ? null : mapped;
    }

    private static boolean isHlsMime(@Nullable String container) {
        if (container == null) {
            return false;
        }
        String value = container.toLowerCase();
        return HLS_MIME.equalsIgnoreCase(container)
                || "hls".equals(value)
                || value.contains("mpegurl");
    }

    /** Underlying stream URL (Direct Play or HLS). */
    public String getStreamUrl() {
        return mStreamUrl;
    }

    public boolean isTranscoded() {
        return mTranscoded;
    }

    @Override
    public List<MediaFormat> getAdaptiveFormats() {
        return null;
    }

    @Override
    public List<MediaFormat> getUrlFormats() {
        return mUrlFormats;
    }

    @Override
    public List<MediaSubtitle> getSubtitles() {
        return mSubtitles;
    }

    @Override
    public String getHlsManifestUrl() {
        return mTranscoded ? mStreamUrl : null;
    }

    @Override
    public String getDashManifestUrl() {
        return null;
    }

    @Override
    public String getLengthSeconds() {
        return mLengthSeconds;
    }

    @Override
    public String getTitle() {
        return mTitle;
    }

    @Override
    public String getAuthor() {
        return null;
    }

    @Override
    public String getViewCount() {
        return null;
    }

    @Override
    public String getDescription() {
        return null;
    }

    @Override
    public String getVideoId() {
        return mVideoId;
    }

    @Override
    public String getChannelId() {
        return null;
    }

    @Override
    public String getCategory() {
        return null;
    }

    @Override
    public boolean isLive() {
        return false;
    }

    @Override
    public boolean isLiveContent() {
        return false;
    }

    @Override
    public boolean containsMedia() {
        return containsHlsUrl() || containsUrlFormats();
    }

    @Override
    public boolean containsSabrFormats() {
        return false;
    }

    @Override
    public boolean containsDashFormats() {
        return false;
    }

    @Override
    public boolean containsHlsUrl() {
        return mTranscoded && mStreamUrl != null;
    }

    @Override
    public boolean containsDashUrl() {
        return false;
    }

    @Override
    public boolean containsUrlFormats() {
        return mUrlFormats != null && !mUrlFormats.isEmpty();
    }

    @Override
    public boolean hasExtendedHlsFormats() {
        return false;
    }

    @Override
    public float getVolumeLevel() {
        return 1.0f;
    }

    @Override
    public InputStream createMpdStream() {
        return null;
    }

    @Override
    public Observable<InputStream> createMpdStreamObservable() {
        return Observable.empty();
    }

    @Override
    public List<String> createUrlList() {
        if (!containsUrlFormats()) {
            return Collections.emptyList();
        }
        return Collections.singletonList(mStreamUrl);
    }

    @Override
    public MediaItemStoryboard createStoryboard() {
        return null;
    }

    @Override
    public boolean isUnplayable() {
        return !containsMedia();
    }

    @Override
    public boolean isUnknownError() {
        return false;
    }

    @Override
    public String getPlayabilityReason() {
        return null;
    }

    @Override
    public boolean isStreamSeekable() {
        return true;
    }

    @Override
    public String getStartTimestamp() {
        return null;
    }

    @Override
    public String getUploadDate() {
        return null;
    }

    @Override
    public long getStartTimeMs() {
        return 0;
    }

    @Override
    public int getStartSegmentNum() {
        return 0;
    }

    @Override
    public int getSegmentDurationUs() {
        return 0;
    }

    @Override
    public String getPaidContentText() {
        return null;
    }

    @Override
    public String getVideoPlaybackUstreamerConfig() {
        return null;
    }

    @Override
    public String getServerAbrStreamingUrl() {
        return null;
    }

    @Override
    public String getPoToken() {
        return null;
    }

    @Override
    public String getVisitorCookie() {
        return null;
    }

    @Override
    public ClientInfo getClientInfo() {
        return null;
    }

    @Override
    public boolean isSynced() {
        return true;
    }

    @Override
    public boolean isAuth() {
        return true;
    }

    @Override
    public String getEventId() {
        return null;
    }

    @Override
    public String getVisitorMonitoringData() {
        return null;
    }

    @Override
    public String getOfParam() {
        return null;
    }

    @Override
    public String getClickTrackingParams() {
        return mClickTrackingParams;
    }

    @Override
    public void setClickTrackingParams(String clickTrackingParams) {
        mClickTrackingParams = clickTrackingParams;
    }

    @Override
    public boolean isCacheActual() {
        return true;
    }

    @Override
    public void sync(MediaItemFormatInfo formatInfo) {
        // no-op: Plex format info is built once from a resolved stream
    }

    /** MIME/container hint from PMS (debug / future Exo overrides). */
    @Nullable
    public String getContainer() {
        return mContainer;
    }
}
