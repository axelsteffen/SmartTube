package com.liskovsoft.smartyoutubetv2.common.misc;

import androidx.annotation.Nullable;

import com.liskovsoft.mediaserviceinterfaces.data.MediaItemFormatInfo;
import com.liskovsoft.plexapi.PlexServiceManager;
import com.liskovsoft.plexapi.adapter.PlexMediaItemAdapter;
import com.liskovsoft.plexapi.adapter.PlexMediaItemFormatInfo;
import com.liskovsoft.plexapi.library.PlexMediaItemImpl;
import com.liskovsoft.plexserviceinterfaces.data.PlexMediaItem;
import com.liskovsoft.plexserviceinterfaces.data.PlexStreamInfo;
import com.liskovsoft.sharedutils.rx.RxHelper;
import com.liskovsoft.smartyoutubetv2.common.app.models.data.Video;

import io.reactivex.Observable;

/**
 * Fork-only: resolves {@link MediaItemFormatInfo} for Plex {@link Video}s
 * so {@code VideoLoaderController} can open Direct Play / HLS via ExoPlayer.
 */
public final class PlexPlaybackHelper {
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
            MediaItemFormatInfo formatInfo = PlexMediaItemFormatInfo.from(item, stream);
            if (formatInfo == null) {
                throw new IllegalStateException(
                        "Plex format info missing for ratingKey=" + item.getRatingKey());
            }
            return formatInfo;
        });
    }

    @Nullable
    static PlexMediaItem resolvePlexItem(@Nullable Video video) {
        if (video == null || !video.isPlex()) {
            return null;
        }

        if (video.mediaItem instanceof PlexMediaItemAdapter) {
            return ((PlexMediaItemAdapter) video.mediaItem).getPlexItem();
        }

        String ratingKey = video.videoId;
        if (ratingKey == null || ratingKey.isEmpty()) {
            return null;
        }

        // Lightweight stub when mediaItem was not retained (e.g. queue restore)
        return new PlexMediaItemImpl(
                ratingKey,
                null,
                video.title,
                "movie",
                0L,
                video.cardImageUrl,
                0);
    }
}
