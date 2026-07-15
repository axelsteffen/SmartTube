package com.liskovsoft.smartyoutubetv2.common.misc;

import androidx.annotation.Nullable;

import com.liskovsoft.mediaserviceinterfaces.data.MediaItemFormatInfo;
import com.liskovsoft.plexapi.PlexServiceManager;
import com.liskovsoft.plexapi.adapter.PlexMediaItemAdapter;
import com.liskovsoft.plexapi.adapter.PlexMediaItemFormatInfo;
import com.liskovsoft.plexapi.library.PlexMediaItemImpl;
import com.liskovsoft.plexserviceinterfaces.PlexMediaService;
import com.liskovsoft.plexserviceinterfaces.data.PlexMediaItem;
import com.liskovsoft.plexserviceinterfaces.data.PlexStreamInfo;
import com.liskovsoft.sharedutils.mylogger.Log;
import com.liskovsoft.sharedutils.rx.RxHelper;
import com.liskovsoft.smartyoutubetv2.common.app.models.data.Video;

import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;

/**
 * Fork-only: resolves {@link MediaItemFormatInfo} for Plex {@link Video}s
 * so {@code VideoLoaderController} can open Direct Play / HLS via ExoPlayer.
 * Also syncs resume progress with PMS (Phase 4.1).
 */
public final class PlexPlaybackHelper {
    private static final String TAG = PlexPlaybackHelper.class.getSimpleName();

    @Nullable
    private static Disposable sProgressAction;

    private PlexPlaybackHelper() {
    }

    /**
     * @return observable of format info (IO thread + main observe), or {@code null}
     *         if video cannot be routed to Plex
     */
    @Nullable
    public static Observable<MediaItemFormatInfo> getFormatInfoObserve(@Nullable Video video) {
        PlexMediaItem item = resolvePlexItem(video);
        if (item == null) {
            return null;
        }

        // RxHelper schedules IO + main observe; service Observable is unscheduled so
        // blockingFirst() runs fetch on the IO worker (safe for unit tests too).
        return RxHelper.fromCallable(() -> {
            PlexStreamInfo stream = PlexServiceManager.instance()
                    .getMediaService()
                    .getStreamInfoObserve(item)
                    .blockingFirst();
            applyViewOffset(video, stream);
            MediaItemFormatInfo formatInfo = PlexMediaItemFormatInfo.from(item, stream);
            if (formatInfo == null) {
                throw new IllegalStateException(
                        "Plex format info missing for ratingKey=" + item.getRatingKey());
            }
            return formatInfo;
        });
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
