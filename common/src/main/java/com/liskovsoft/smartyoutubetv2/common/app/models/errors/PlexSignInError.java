package com.liskovsoft.smartyoutubetv2.common.app.models.errors;

import android.content.Context;

import com.liskovsoft.sharedutils.helpers.MessageHelpers;
import com.liskovsoft.smartyoutubetv2.common.R;

/**
 * Shown when the Plex sidebar section is enabled but the user is not signed in
 * or has no server selected. Full PIN / settings UI lands in Phase 3.5.
 */
public class PlexSignInError implements ErrorFragmentData {
    private final Context mContext;

    public PlexSignInError(Context context) {
        mContext = context;
    }

    @Override
    public void onAction() {
        MessageHelpers.showMessage(mContext, R.string.plex_signin_coming_soon);
    }

    @Override
    public String getMessage() {
        return mContext.getString(R.string.plex_signin_to_browse);
    }

    @Override
    public String getActionText() {
        return mContext.getString(R.string.action_signin);
    }
}
