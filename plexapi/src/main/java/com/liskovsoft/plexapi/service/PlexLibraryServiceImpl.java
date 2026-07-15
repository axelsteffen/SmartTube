package com.liskovsoft.plexapi.service;

import com.liskovsoft.plexserviceinterfaces.PlexLibraryService;
import com.liskovsoft.plexserviceinterfaces.data.PlexLibrary;
import com.liskovsoft.plexserviceinterfaces.data.PlexMediaItem;

import java.util.List;

import io.reactivex.Observable;

/**
 * Stub: library listing lands in Phase 1.5.
 */
public class PlexLibraryServiceImpl implements PlexLibraryService {
    @Override
    public Observable<List<PlexLibrary>> getLibrariesObserve() {
        return Observable.error(new UnsupportedOperationException("Plex libraries not implemented yet (Phase 1.5)"));
    }

    @Override
    public Observable<List<PlexMediaItem>> getMoviesObserve(PlexLibrary library) {
        return Observable.error(new UnsupportedOperationException("Plex movie list not implemented yet (Phase 1.5)"));
    }
}
