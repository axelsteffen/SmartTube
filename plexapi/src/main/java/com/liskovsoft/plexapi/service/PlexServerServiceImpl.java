package com.liskovsoft.plexapi.service;

import com.liskovsoft.plexserviceinterfaces.PlexServerService;
import com.liskovsoft.plexserviceinterfaces.data.PlexServer;

import java.util.List;

import io.reactivex.Observable;

/**
 * Stub: server discovery lands in Phase 1.4.
 */
public class PlexServerServiceImpl implements PlexServerService {
    private PlexServer mSelectedServer;

    @Override
    public Observable<List<PlexServer>> getServersObserve() {
        return Observable.error(new UnsupportedOperationException("Plex server discovery not implemented yet (Phase 1.4)"));
    }

    @Override
    public PlexServer getSelectedServer() {
        return mSelectedServer;
    }

    @Override
    public void selectServer(PlexServer server) {
        mSelectedServer = server;
    }
}
