package com.liskovsoft.smartyoutubetv2.common.app.models.errors;

import android.content.Context;

import com.liskovsoft.plexapi.PlexServiceManager;
import com.liskovsoft.smartyoutubetv2.common.R;
import com.liskovsoft.smartyoutubetv2.common.app.presenters.PlexSignInPresenter;
import com.liskovsoft.smartyoutubetv2.common.app.presenters.dialogs.PlexServerSelectionPresenter;

/**
 * Shown when the Plex sidebar section is enabled but the user is not signed in
 * or has no server selected. Action starts the PIN / server flow (Phase 3.5).
 */
public class PlexSignInError implements ErrorFragmentData {
    private final Context mContext;

    public PlexSignInError(Context context) {
        mContext = context;
    }

    @Override
    public void onAction() {
        if (PlexServiceManager.instance().getSignInService().isSigned()) {
            PlexServerSelectionPresenter.instance(mContext).show(true);
        } else {
            PlexSignInPresenter.instance(mContext).start();
        }
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
