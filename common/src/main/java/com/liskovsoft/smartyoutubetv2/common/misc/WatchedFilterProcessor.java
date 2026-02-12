package com.liskovsoft.smartyoutubetv2.common.misc;

import android.content.Context;

import com.liskovsoft.smartyoutubetv2.common.app.models.data.Video;
import com.liskovsoft.smartyoutubetv2.common.app.models.data.VideoGroup;
import com.liskovsoft.smartyoutubetv2.common.app.models.playback.service.VideoStateService;
import com.liskovsoft.smartyoutubetv2.common.app.models.playback.service.VideoStateService.State;
import com.liskovsoft.smartyoutubetv2.common.prefs.PlayerTweaksData;

import java.util.ArrayList;
import java.util.List;

/**
 * Filters already watched videos from player suggestions.
 * Prevents videos from repeating in the "Up next" / suggestions list.
 */
public class WatchedFilterProcessor implements BrowseProcessor {
    private static final float WATCHED_THRESHOLD = 0.9f;
    private static final float WATCHED_PERCENT_THRESHOLD = 90f;

    private final Context mContext;

    public WatchedFilterProcessor(Context context) {
        mContext = context.getApplicationContext();
    }

    @Override
    public void process(VideoGroup videoGroup) {
        if (!PlayerTweaksData.instance(mContext).isHideWatchedFromSuggestionsEnabled()
                || videoGroup == null || videoGroup.isEmpty()) {
            return;
        }

        // Don't filter user's playback queue - they explicitly added those videos
        if (videoGroup.isQueue) {
            return;
        }

        VideoStateService stateService = VideoStateService.instance(mContext);
        if (stateService == null) {
            return;
        }

        List<Video> toRemove = new ArrayList<>();

        for (Video video : videoGroup.getVideos()) {
            if (!video.hasVideo() || video.isChapter) {
                continue;
            }

            if (isWatched(video, stateService)) {
                toRemove.add(video);
            }
        }

        for (Video video : toRemove) {
            videoGroup.remove(video);
        }
    }

    private boolean isWatched(Video video, VideoStateService stateService) {
        if (video == null || video.videoId == null) {
            return false;
        }

        // Quick check: video already has percentWatched from API/metadata
        if (video.percentWatched >= WATCHED_PERCENT_THRESHOLD) {
            return true;
        }

        State state = stateService.getByVideoId(video.videoId);
        if (state == null) {
            return false;
        }

        if (state.durationMs > 0) {
            return state.positionMs >= state.durationMs * WATCHED_THRESHOLD;
        }

        return state.video != null && state.video.percentWatched >= WATCHED_PERCENT_THRESHOLD;
    }

    @Override
    public void dispose() {
        // Nothing to dispose
    }
}
