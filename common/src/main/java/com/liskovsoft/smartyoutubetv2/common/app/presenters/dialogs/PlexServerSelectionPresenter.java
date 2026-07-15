package com.liskovsoft.smartyoutubetv2.common.app.presenters.dialogs;

import android.annotation.SuppressLint;
import android.content.Context;

import com.liskovsoft.plexapi.PlexServiceManager;
import com.liskovsoft.plexserviceinterfaces.PlexServerService;
import com.liskovsoft.plexserviceinterfaces.data.PlexServer;
import com.liskovsoft.sharedutils.helpers.MessageHelpers;
import com.liskovsoft.sharedutils.mylogger.Log;
import com.liskovsoft.sharedutils.rx.RxHelper;
import com.liskovsoft.smartyoutubetv2.common.R;
import com.liskovsoft.smartyoutubetv2.common.app.models.playback.ui.OptionItem;
import com.liskovsoft.smartyoutubetv2.common.app.models.playback.ui.UiOptionItem;
import com.liskovsoft.smartyoutubetv2.common.app.presenters.AppDialogPresenter;
import com.liskovsoft.smartyoutubetv2.common.app.presenters.BrowsePresenter;
import com.liskovsoft.smartyoutubetv2.common.app.presenters.base.BasePresenter;
import com.liskovsoft.smartyoutubetv2.common.misc.SidebarSectionRegistry;
import com.liskovsoft.smartyoutubetv2.common.utils.LoadingManager;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.disposables.Disposable;

/**
 * Fork-only: pick a PMS after Plex PIN auth (Phase 3.5).
 */
public class PlexServerSelectionPresenter extends BasePresenter<Void> {
    private static final String TAG = PlexServerSelectionPresenter.class.getSimpleName();

    @SuppressLint("StaticFieldLeak")
    private static PlexServerSelectionPresenter sInstance;
    private Disposable mLoadAction;

    private PlexServerSelectionPresenter(Context context) {
        super(context);
    }

    public static PlexServerSelectionPresenter instance(Context context) {
        if (sInstance == null) {
            sInstance = new PlexServerSelectionPresenter(context);
        }
        sInstance.setContext(context);
        return sInstance;
    }

    public void unhold() {
        RxHelper.disposeActions(mLoadAction);
        sInstance = null;
    }

    /** @param force unused for now; kept for symmetry with account selection. */
    public void show(boolean force) {
        LoadingManager.showLoading(getContext(), true);
        PlexServerService serverService = PlexServiceManager.instance().getServerService();
        mLoadAction = RxHelper.fromCallable(() ->
                        serverService.getServersObserve().blockingFirst())
                .subscribe(
                        this::createAndShowDialog,
                        error -> {
                            LoadingManager.showLoading(getContext(), false);
                            Log.e(TAG, "Server discovery failed: %s", error.getMessage());
                            MessageHelpers.showMessage(getContext(),
                                    error.getMessage() != null
                                            ? error.getMessage()
                                            : getContext().getString(R.string.plex_no_servers));
                        }
                );
    }

    public void show() {
        show(false);
    }

    private void createAndShowDialog(List<PlexServer> servers) {
        LoadingManager.showLoading(getContext(), false);

        if (servers == null || servers.isEmpty()) {
            MessageHelpers.showMessage(getContext(), R.string.plex_no_servers);
            return;
        }

        AppDialogPresenter dialogPresenter = AppDialogPresenter.instance(getContext());
        List<OptionItem> optionItems = new ArrayList<>();
        PlexServer selected = PlexServiceManager.instance().getServerService().getSelectedServer();
        String selectedId = selected != null ? selected.getClientIdentifier() : null;

        for (PlexServer server : servers) {
            boolean checked = selectedId != null
                    && selectedId.equals(server.getClientIdentifier());
            optionItems.add(UiOptionItem.from(
                    formatServer(server),
                    option -> {
                        selectServer(server);
                        dialogPresenter.closeDialog();
                    },
                    checked
            ));
        }

        dialogPresenter.appendRadioCategory(
                getContext().getString(R.string.plex_select_server), optionItems);
        dialogPresenter.showDialog(
                getContext().getString(R.string.plex_select_server), this::unhold);
    }

    private void selectServer(PlexServer server) {
        PlexServiceManager.instance().getServerService().selectServer(server);
        Log.d(TAG, "Selected Plex server: " + server.getName());
        BrowsePresenter browsePresenter = BrowsePresenter.instance(getContext());
        if (browsePresenter != null) {
            browsePresenter.updateSections();
            browsePresenter.selectSection(SidebarSectionRegistry.TYPE_PLEX);
        }
        MessageHelpers.showMessage(getContext(), R.string.msg_done);
    }

    private static String formatServer(PlexServer server) {
        String name = server.getName() != null ? server.getName() : "Plex";
        StringBuilder sb = new StringBuilder(name);
        if (server.getBaseUrl() != null && !server.getBaseUrl().isEmpty()) {
            sb.append("\n").append(server.getBaseUrl());
        }
        if (!server.isOnline()) {
            sb.append(" (offline)");
        }
        return sb.toString();
    }
}
