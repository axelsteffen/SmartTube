package com.liskovsoft.plexserviceinterfaces;

import com.liskovsoft.plexserviceinterfaces.data.PlexLibrary;
import com.liskovsoft.plexserviceinterfaces.data.PlexMediaItem;
import com.liskovsoft.plexserviceinterfaces.data.PlexMediaPage;

import java.util.List;

import io.reactivex.Observable;

/**
 * Browse Plex libraries and fetch media lists (lazy / on-demand).
 */
public interface PlexLibraryService {
    Observable<List<PlexLibrary>> getLibrariesObserve();

    Observable<List<PlexMediaItem>> getMoviesObserve(PlexLibrary library);

    Observable<List<PlexMediaItem>> getShowsObserve(PlexLibrary library);

    /** Show → seasons, season → episodes (Phase 3.3). */
    Observable<List<PlexMediaItem>> getChildrenObserve(PlexMediaItem parent);

    /** Paginated section browse (Phase 3.4). {@code offset} is PMS container start. */
    Observable<PlexMediaPage> getMoviesPageObserve(PlexLibrary library, int offset);

    Observable<PlexMediaPage> getShowsPageObserve(PlexLibrary library, int offset);

    Observable<PlexMediaPage> getChildrenPageObserve(PlexMediaItem parent, int offset);
}
