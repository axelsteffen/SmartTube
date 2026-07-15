package com.liskovsoft.plexapi.service;

import com.liskovsoft.plexserviceinterfaces.PlexMediaService;
import com.liskovsoft.plexserviceinterfaces.data.PlexMediaItem;
import com.liskovsoft.plexserviceinterfaces.data.PlexStreamInfo;

import io.reactivex.Observable;

/**
 * Stub: stream URL resolution lands in Phase 1.6.
 */
public class PlexMediaServiceImpl implements PlexMediaService {
    @Override
    public Observable<PlexStreamInfo> getStreamInfoObserve(PlexMediaItem item) {
        return Observable.error(new UnsupportedOperationException("Plex stream info not implemented yet (Phase 1.6)"));
    }
}
