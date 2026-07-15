package com.liskovsoft.plexserviceinterfaces;

import com.liskovsoft.plexserviceinterfaces.data.PlexMediaItem;
import com.liskovsoft.plexserviceinterfaces.data.PlexStreamInfo;

import io.reactivex.Observable;

/**
 * Resolve playable stream URLs for a Plex media item.
 */
public interface PlexMediaService {
    Observable<PlexStreamInfo> getStreamInfoObserve(PlexMediaItem item);
}
