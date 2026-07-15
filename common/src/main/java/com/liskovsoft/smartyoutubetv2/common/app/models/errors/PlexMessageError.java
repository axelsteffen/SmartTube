package com.liskovsoft.smartyoutubetv2.common.app.models.errors;

import androidx.annotation.Nullable;

/**
 * Lightweight Plex browse error with a user-facing message (Phase 4.6).
 * No action button — section refresh is handled by {@code BrowsePresenter}.
 */
public class PlexMessageError implements ErrorFragmentData {
    private final String mMessage;

    public PlexMessageError(@Nullable String message) {
        mMessage = message != null ? message : "";
    }

    @Override
    public void onAction() {
        // no-op
    }

    @Override
    public String getMessage() {
        return mMessage;
    }

    @Override
    public String getActionText() {
        return null;
    }
}
