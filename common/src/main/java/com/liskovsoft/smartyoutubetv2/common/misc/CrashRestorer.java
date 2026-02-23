package com.liskovsoft.smartyoutubetv2.common.misc;

import android.content.Context;
import android.os.Bundle;

import com.liskovsoft.smartyoutubetv2.common.app.models.data.Video;
import com.liskovsoft.smartyoutubetv2.common.app.models.playback.service.VideoStateService;
import com.liskovsoft.smartyoutubetv2.common.app.models.playback.service.VideoStateService.State;
import com.liskovsoft.smartyoutubetv2.common.app.presenters.PlaybackPresenter;
import com.liskovsoft.smartyoutubetv2.common.app.views.ViewManager;

public class CrashRestorer {
    private static final String SELECTED_VIDEO = "SelectedVideo";
    private static final String IS_PLAYER_IN_FOREGROUND = "IsPlayerInForeground";
    private Video mSelectedVideo;
    private boolean mIsPlayerInForeground;
    private final Context mContext;

    public CrashRestorer(Context context, Bundle savedState) {
        mContext = context.getApplicationContext();
        init(savedState);
    }

    private void init(Bundle savedState) {
        if (savedState == null) {
            return;
        }

        mSelectedVideo = Video.fromString(savedState.getString(SELECTED_VIDEO));
        mIsPlayerInForeground = savedState.getBoolean(IS_PLAYER_IN_FOREGROUND, false);
    }

    public void persist(Bundle outState, Video currentVideo) {
        if (currentVideo == null) { // multiple crashes without user interaction
            currentVideo = mSelectedVideo;
        }

        if (currentVideo != null) {
            outState.putString(SELECTED_VIDEO, currentVideo.toString());
        }
        outState.putBoolean(IS_PLAYER_IN_FOREGROUND, ViewManager.instance(mContext).isPlayerInForeground());
    }

    public void restore() {
        if (PlaybackPresenter.instance(mContext).getPlayer() == null && mIsPlayerInForeground) {
            VideoStateService stateService = VideoStateService.instance(mContext);
            boolean isVideoStateSynced = mSelectedVideo == null || stateService.getByVideoId(mSelectedVideo.videoId) != null;
            State lastState = stateService.getLastState();
            PlaybackPresenter.instance(mContext).openVideo(lastState != null && isVideoStateSynced ? lastState.video : mSelectedVideo);
        }

        // Restore can be called only once
        mIsPlayerInForeground = false;
    }

    public Video getSelectedVideo() {
        return mSelectedVideo;
    }
}
