package com.liskovsoft.smartyoutubetv2.common.app.presenters;

import com.liskovsoft.mediaserviceinterfaces.data.MediaGroup;
import com.liskovsoft.plexapi.PlexServiceManager;
import com.liskovsoft.plexapi.adapter.PlexMediaGroupAdapter;
import com.liskovsoft.plexserviceinterfaces.PlexLibraryService;
import com.liskovsoft.plexserviceinterfaces.data.PlexLibrary;
import com.liskovsoft.plexserviceinterfaces.data.PlexMediaItem;
import com.liskovsoft.sharedutils.mylogger.Log;
import com.liskovsoft.sharedutils.rx.RxHelper;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;

/**
 * Fork-only: loads Plex movie libraries as browse rows for the existing
 * {@link BrowsePresenter} pipeline (Phase 3.2). Not a view presenter.
 */
public final class PlexBrowsePresenter {
    private static final String TAG = PlexBrowsePresenter.class.getSimpleName();
    private static final String TYPE_MOVIE = "movie";

    private PlexBrowsePresenter() {
    }

    /**
     * Cold observable: lists movie sections, then fetches the first page of each
     * sequentially (TV-friendly). Emits on main after IO via {@link RxHelper}.
     */
    public static Observable<List<MediaGroup>> getLibraryRowsObserve() {
        return RxHelper.fromCallable(PlexBrowsePresenter::fetchLibraryRows);
    }

    private static List<MediaGroup> fetchLibraryRows() {
        PlexLibraryService libraryService = PlexServiceManager.instance().getLibraryService();

        List<PlexLibrary> libraries = libraryService.getLibrariesObserve().blockingFirst();
        List<MediaGroup> rows = new ArrayList<>();

        if (libraries == null || libraries.isEmpty()) {
            Log.d(TAG, "No Plex libraries");
            return rows;
        }

        for (PlexLibrary library : libraries) {
            if (!isMovieLibrary(library)) {
                continue;
            }

            List<PlexMediaItem> movies = libraryService.getMoviesObserve(library).blockingFirst();
            MediaGroup group = PlexMediaGroupAdapter.from(library, movies);
            if (group != null && !group.isEmpty()) {
                rows.add(group);
            }
        }

        Log.d(TAG, "Built " + rows.size() + " Plex movie row(s)");
        return rows;
    }

    private static boolean isMovieLibrary(PlexLibrary library) {
        return library != null
                && library.getType() != null
                && TYPE_MOVIE.equalsIgnoreCase(library.getType());
    }
}
