package com.liskovsoft.smartyoutubetv2.common.misc;

import android.content.Context;

import androidx.annotation.Nullable;

import com.liskovsoft.mediaserviceinterfaces.data.MediaItemFormatInfo;
import com.liskovsoft.plexapi.PlexServiceManager;
import com.liskovsoft.plexapi.adapter.PlexMediaItemAdapter;
import com.liskovsoft.plexapi.adapter.PlexMediaItemFormatInfo;
import com.liskovsoft.plexapi.library.PlexMediaItemImpl;
import com.liskovsoft.plexserviceinterfaces.PlexMediaService;
import com.liskovsoft.plexserviceinterfaces.data.PlexAudioTrack;
import com.liskovsoft.plexserviceinterfaces.data.PlexMediaItem;
import com.liskovsoft.plexserviceinterfaces.data.PlexStreamInfo;
import com.liskovsoft.sharedutils.mylogger.Log;
import com.liskovsoft.sharedutils.rx.RxHelper;
import com.liskovsoft.smartyoutubetv2.common.app.models.data.Video;
import com.liskovsoft.smartyoutubetv2.common.prefs.PlayerData;

import java.util.Collections;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;

/**
 * Fork-only: resolves {@link MediaItemFormatInfo} for Plex {@link Video}s
 * so {@code VideoLoaderController} can open Direct Play / HLS via ExoPlayer.
 * Also syncs resume progress (4.1), audio track session state (4.3),
 * and forced transcode fallback (4.5).
 */
public final class PlexPlaybackHelper {
    private static final String TAG = PlexPlaybackHelper.class.getSimpleName();

    @Nullable
    private static Disposable sProgressAction;

    /** Transient override for next format resolve (HLS audio switch). */
    @Nullable
    private static Long sOverrideAudioStreamId;

    /** Transient: next resolve skips Direct Play and forces HLS transcode (4.5). */
    private static boolean sForceTranscode;

    /** Rating key for which a force-transcode fallback was already requested (no loops). */
    @Nullable
    private static String sTranscodeFallbackRatingKey;

    @Nullable
    private static PlaybackSession sSession;

    private PlexPlaybackHelper() {
    }

    /** Snapshot of the last resolved Plex stream for the active playback. */
    public static final class PlaybackSession {
        public final String ratingKey;
        public final boolean transcoded;
        public final List<PlexAudioTrack> audioTracks;
        public final long selectedAudioStreamId;
        @Nullable
        public final String preferredAudioLanguage;

        PlaybackSession(
                String ratingKey,
                boolean transcoded,
                List<PlexAudioTrack> audioTracks,
                long selectedAudioStreamId,
                @Nullable String preferredAudioLanguage) {
            this.ratingKey = ratingKey;
            this.transcoded = transcoded;
            this.audioTracks = audioTracks != null
                    ? audioTracks
                    : Collections.emptyList();
            this.selectedAudioStreamId = selectedAudioStreamId;
            this.preferredAudioLanguage = preferredAudioLanguage;
        }
    }

    /**
     * @return observable of format info (IO thread + main observe), or {@code null}
     *         if video cannot be routed to Plex
     */
    @Nullable
    public static Observable<MediaItemFormatInfo> getFormatInfoObserve(@Nullable Video video) {
        return getFormatInfoObserve(video, null);
    }

    /**
     * @param context used to read preferred audio language from {@link PlayerData}
     */
    @Nullable
    public static Observable<MediaItemFormatInfo> getFormatInfoObserve(
            @Nullable Video video, @Nullable Context context) {
        PlexMediaItem item = resolvePlexItem(video);
        if (item == null) {
            return null;
        }

        Long overrideAudioId = consumeOverrideAudioStreamId();
        boolean forceTranscode = consumeForceTranscode();
        String preferredLanguage = null;
        if (context != null) {
            preferredLanguage = PlayerData.instance(context).getAudioLanguage();
            if (preferredLanguage != null && preferredLanguage.isEmpty()) {
                preferredLanguage = null;
            }
        }
        final String preferredLangFinal = preferredLanguage;
        final Long overrideFinal = overrideAudioId;
        final boolean forceTranscodeFinal = forceTranscode;

        return RxHelper.fromCallable(() -> {
            PlexStreamInfo stream = PlexServiceManager.instance()
                    .getMediaService()
                    .getStreamInfoObserve(
                            item, overrideFinal, preferredLangFinal, forceTranscodeFinal)
                    .blockingFirst();
            applyViewOffset(video, stream);
            rememberSession(item.getRatingKey(), stream, preferredLangFinal);
            MediaItemFormatInfo formatInfo = PlexMediaItemFormatInfo.from(item, stream);
            if (formatInfo == null) {
                throw new IllegalStateException(
                        "Plex format info missing for ratingKey=" + item.getRatingKey());
            }
            return formatInfo;
        });
    }

    /** Sets audio stream id for the next format resolve (mid-playback HLS switch). */
    public static void setOverrideAudioStreamId(long audioStreamId) {
        sOverrideAudioStreamId = audioStreamId > 0L ? audioStreamId : null;
    }

    @Nullable
    private static Long consumeOverrideAudioStreamId() {
        Long id = sOverrideAudioStreamId;
        sOverrideAudioStreamId = null;
        return id;
    }

    /**
     * True when Direct Play failed once and we have not yet forced transcode for this item.
     */
    public static boolean canAttemptTranscodeFallback(@Nullable Video video) {
        if (video == null || !video.isPlex()) {
            return false;
        }
        String ratingKey = video.videoId;
        if (ratingKey == null || ratingKey.isEmpty()) {
            return false;
        }
        if (ratingKey.equals(sTranscodeFallbackRatingKey)) {
            return false;
        }
        if (sSession != null
                && ratingKey.equals(sSession.ratingKey)
                && sSession.transcoded) {
            return false;
        }
        return true;
    }

    /**
     * Requests forced HLS transcode on the next format resolve (Phase 4.5).
     * Marks the rating key so fallback is attempted at most once per item.
     */
    public static void requestForceTranscode(@Nullable Video video) {
        sForceTranscode = true;
        if (video != null && video.videoId != null && !video.videoId.isEmpty()) {
            sTranscodeFallbackRatingKey = video.videoId;
        }
    }

    private static boolean consumeForceTranscode() {
        boolean force = sForceTranscode;
        sForceTranscode = false;
        return force;
    }

    private static void rememberSession(
            String ratingKey, PlexStreamInfo stream, @Nullable String preferredLanguage) {
        if (stream == null) {
            sSession = null;
            return;
        }
        if (sSession != null
                && ratingKey != null
                && !ratingKey.equals(sSession.ratingKey)) {
            sTranscodeFallbackRatingKey = null;
        }
        String preferred = preferredLanguage;
        if ((preferred == null || preferred.isEmpty())
                && stream.getSelectedAudioStreamId() > 0L) {
            for (PlexAudioTrack track : stream.getAudioTracks()) {
                if (track.getId() == stream.getSelectedAudioStreamId()) {
                    preferred = track.getLanguageCode();
                    break;
                }
            }
        }
        sSession = new PlaybackSession(
                ratingKey,
                stream.isTranscoded(),
                stream.getAudioTracks(),
                stream.getSelectedAudioStreamId(),
                preferred);
    }

    public static void clearSession() {
        sSession = null;
        sOverrideAudioStreamId = null;
        sForceTranscode = false;
        sTranscodeFallbackRatingKey = null;
    }

    @Nullable
    public static PlaybackSession getSession() {
        return sSession;
    }

    /** True when transcoded HLS has multiple Plex audio tracks (needs decision reload). */
    public static boolean needsAudioStreamReload(@Nullable Video video) {
        if (video == null || !video.isPlex() || sSession == null) {
            return false;
        }
        if (video.videoId != null && !video.videoId.equals(sSession.ratingKey)) {
            return false;
        }
        return sSession.transcoded && sSession.audioTracks.size() > 1;
    }

    public static List<PlexAudioTrack> getAudioTracks() {
        return sSession != null ? sSession.audioTracks : Collections.emptyList();
    }

    public static long getSelectedAudioStreamId() {
        return sSession != null ? sSession.selectedAudioStreamId : 0L;
    }

    @Nullable
    public static String getPreferredAudioLanguage() {
        return sSession != null ? sSession.preferredAudioLanguage : null;
    }

    /**
     * Reports progress to PMS. Fire-and-forget; overlaps are coalesced.
     */
    public static void updateProgress(
            @Nullable Video video, long positionMs, long durationMs, @Nullable String state) {
        PlexMediaItem item = resolvePlexMediaItem(video);
        if (item == null) {
            return;
        }
        if (RxHelper.isAnyActionRunning(sProgressAction)) {
            return;
        }

        String timelineState = state != null ? state : PlexMediaService.STATE_STOPPED;
        RxHelper.disposeActions(sProgressAction);
        sProgressAction = RxHelper.execute(
                PlexServiceManager.instance()
                        .getMediaService()
                        .updateProgressObserve(item, positionMs, durationMs, timelineState),
                error -> Log.e(TAG, "Plex progress sync failed: " + error.getMessage()));
    }

    /** Applies PMS viewOffset onto {@link Video} when local resume data is missing. */
    static void applyViewOffset(@Nullable Video video, @Nullable PlexStreamInfo stream) {
        if (video == null || stream == null) {
            return;
        }
        long viewOffsetMs = stream.getViewOffsetMs();
        if (viewOffsetMs <= 0L) {
            return;
        }
        // Prefer existing local/card resume; do not overwrite a known position.
        if (video.getPositionMs() > 0L || video.startTimeSeconds > 0) {
            return;
        }
        video.startTimeSeconds = (int) (viewOffsetMs / 1000L);
        long durationMs = video.getDurationMs();
        if (durationMs > 0L) {
            video.percentWatched = Math.min(100f, (viewOffsetMs * 100f) / durationMs);
        }
    }

    @Nullable
    static PlexMediaItem resolvePlexItem(@Nullable Video video) {
        return resolvePlexMediaItem(video);
    }

    /** Resolves underlying Plex item from a {@link Video} (playback + browse drill-down). */
    @Nullable
    public static PlexMediaItem resolvePlexMediaItem(@Nullable Video video) {
        if (video == null || !video.isPlex()) {
            return null;
        }

        if (video.mediaItem instanceof PlexMediaItemAdapter) {
            return ((PlexMediaItemAdapter) video.mediaItem).getPlexItem();
        }

        String ratingKey = video.videoId;
        if (ratingKey == null || ratingKey.isEmpty()) {
            ratingKey = video.playlistId;
        }
        if (ratingKey == null || ratingKey.isEmpty()) {
            return null;
        }

        // Lightweight stub when mediaItem was not retained (e.g. queue restore)
        return new PlexMediaItemImpl(
                ratingKey,
                null,
                video.title,
                video.hasPlaylist() ? "show" : "movie",
                video.getDurationMs() > 0 ? video.getDurationMs() : 0L,
                video.cardImageUrl,
                0);
    }
}
