package com.liskovsoft.smartyoutubetv2.common.app.models.errors;

import android.content.Context;

import com.liskovsoft.smartyoutubetv2.common.R;

public class PlexDisabledError implements ErrorFragmentData {
    private final Context mContext;

    public PlexDisabledError(Context context) {
        mContext = context;
    }

    @Override
    public void onAction() {
    }

    @Override
    public String getMessage() {
        return mContext.getString(R.string.plex_not_available);
    }

    @Override
    public String getActionText() {
        return null;
    }
}
