package com.liskovsoft.plexserviceinterfaces;

import com.liskovsoft.plexserviceinterfaces.data.PlexServer;

import java.util.List;

import io.reactivex.Observable;

/**
 * Discover and select a Plex Media Server.
 */
public interface PlexServerService {
    Observable<List<PlexServer>> getServersObserve();

    PlexServer getSelectedServer();

    void selectServer(PlexServer server);
}
