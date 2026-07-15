package com.liskovsoft.smartyoutubetv2.common.app.presenters.settings;

import android.annotation.SuppressLint;
import android.content.Context;

import com.liskovsoft.plexapi.PlexServiceManager;
import com.liskovsoft.plexserviceinterfaces.PlexSignInService;
import com.liskovsoft.plexserviceinterfaces.data.PlexServer;
import com.liskovsoft.sharedutils.helpers.MessageHelpers;
import com.liskovsoft.smartyoutubetv2.common.R;
import com.liskovsoft.smartyoutubetv2.common.app.models.playback.ui.UiOptionItem;
import com.liskovsoft.smartyoutubetv2.common.app.presenters.AppDialogPresenter;
import com.liskovsoft.smartyoutubetv2.common.app.presenters.BrowsePresenter;
import com.liskovsoft.smartyoutubetv2.common.app.presenters.PlexSignInPresenter;
import com.liskovsoft.smartyoutubetv2.common.app.presenters.base.BasePresenter;
import com.liskovsoft.smartyoutubetv2.common.app.presenters.dialogs.PlexServerSelectionPresenter;
import com.liskovsoft.smartyoutubetv2.common.misc.MediaSourceRegistry;
import com.liskovsoft.smartyoutubetv2.common.utils.AppDialogUtil;

/**
 * Fork-only: Plex sign-in, server picker, and sign-out (Phase 3.5).
 */
public class PlexSettingsPresenter extends BasePresenter<Void> {
    @SuppressLint("StaticFieldLeak")
    private static PlexSettingsPresenter sInstance;

    private PlexSettingsPresenter(Context context) {
        super(context);
    }

    public static PlexSettingsPresenter instance(Context context) {
        if (sInstance == null) {
            sInstance = new PlexSettingsPresenter(context);
        }
        sInstance.setContext(context);
        return sInstance;
    }

    public void unhold() {
        sInstance = null;
    }

    public void show() {
        if (!MediaSourceRegistry.isPlexEnabled()) {
            MessageHelpers.showMessage(getContext(), R.string.plex_not_available);
            return;
        }

        AppDialogPresenter settingsPresenter = AppDialogPresenter.instance(getContext());
        PlexSignInService signInService = PlexServiceManager.instance().getSignInService();
        PlexServer selected = PlexServiceManager.instance().getServerService().getSelectedServer();

        if (!signInService.isSigned()) {
            settingsPresenter.appendSingleButton(UiOptionItem.from(
                    getContext().getString(R.string.action_signin),
                    option -> {
                        settingsPresenter.closeDialog();
                        PlexSignInPresenter.instance(getContext()).start();
                    }));
        } else {
            settingsPresenter.appendSingleButton(UiOptionItem.from(
                    getContext().getString(R.string.plex_select_server),
                    option -> {
                        settingsPresenter.closeDialog();
                        PlexServerSelectionPresenter.instance(getContext()).show(true);
                    }));

            settingsPresenter.appendSingleButton(UiOptionItem.from(
                    getContext().getString(R.string.dialog_remove_account),
                    option -> AppDialogUtil.showConfirmationDialog(
                            getContext(),
                            getContext().getString(R.string.dialog_remove_account),
                            () -> {
                                signOut();
                                settingsPresenter.closeDialog();
                                MessageHelpers.showMessage(getContext(), R.string.msg_done);
                            })));
        }

        String title = getContext().getString(R.string.header_plex);
        if (signInService.isSigned() && selected != null && selected.getName() != null) {
            title = getContext().getString(R.string.plex_current_server, selected.getName());
        }
        settingsPresenter.showDialog(title, this::unhold);
    }

    private void signOut() {
        PlexServiceManager.instance().getSignInService().signOut();
        BrowsePresenter browsePresenter = BrowsePresenter.instance(getContext());
        if (browsePresenter != null) {
            browsePresenter.updateSections();
        }
    }
}
