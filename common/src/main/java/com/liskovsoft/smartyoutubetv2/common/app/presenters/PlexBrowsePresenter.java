package com.liskovsoft.smartyoutubetv2.common.app.presenters;

import androidx.annotation.Nullable;

import com.liskovsoft.mediaserviceinterfaces.data.MediaGroup;
import com.liskovsoft.plexapi.PlexServiceManager;
import com.liskovsoft.plexapi.adapter.PlexMediaGroupAdapter;
import com.liskovsoft.plexserviceinterfaces.PlexLibraryService;
import com.liskovsoft.plexserviceinterfaces.data.PlexLibrary;
import com.liskovsoft.plexserviceinterfaces.data.PlexMediaItem;
import com.liskovsoft.sharedutils.mylogger.Log;
import com.liskovsoft.sharedutils.rx.RxHelper;
import com.liskovsoft.smartyoutubetv2.common.app.models.data.Video;
import com.liskovsoft.smartyoutubetv2.common.misc.PlexPlaybackHelper;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;

/**
 * Fork-only: loads Plex movie/show libraries as browse rows and resolves
 * show/season drill-down for the existing {@link BrowsePresenter} pipeline
 * (Phase 3.2 / 3.3). Not a view presenter.
 */
public final class PlexBrowsePresenter {
    private static final String TAG = PlexBrowsePresenter.class.getSimpleName();
    private static final String TYPE_MOVIE = "movie";
    private static final String TYPE_SHOW = "show";

    private PlexBrowsePresenter() {
    }

    /**
     * Cold observable: lists movie + show sections, then fetches the first page of each
     * sequentially (TV-friendly). Emits on main after IO via {@link RxHelper}.
     */
    public static Observable<List<MediaGroup>> getLibraryRowsObserve() {
        return RxHelper.fromCallable(PlexBrowsePresenter::fetchLibraryRows);
    }

    /**
     * Children of a Plex show or season for {@link ChannelUploadsPresenter} (Phase 3.3).
     */
    @Nullable
    public static Observable<MediaGroup> getChildrenGroupObserve(@Nullable Video video) {
        if (video == null || !video.isPlex() || !video.hasPlaylist()) {
            return null;
        }
        return RxHelper.fromCallable(() -> fetchChildrenGroup(video));
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
            List<PlexMediaItem> items = fetchFirstPage(libraryService, library);
            if (items == null || items.isEmpty()) {
                continue;
            }

            MediaGroup group = PlexMediaGroupAdapter.from(library, items);
            if (group != null && !group.isEmpty()) {
                rows.add(group);
            }
        }

        Log.d(TAG, "Built " + rows.size() + " Plex library row(s)");
        return rows;
    }

    @Nullable
    private static List<PlexMediaItem> fetchFirstPage(PlexLibraryService libraryService, PlexLibrary library) {
        if (isMovieLibrary(library)) {
            return libraryService.getMoviesObserve(library).blockingFirst();
        }
        if (isShowLibrary(library)) {
            return libraryService.getShowsObserve(library).blockingFirst();
        }
        return null;
    }

    @Nullable
    private static MediaGroup fetchChildrenGroup(Video video) {
        PlexMediaItem parent = PlexPlaybackHelper.resolvePlexMediaItem(video);
        if (parent == null) {
            return null;
        }

        List<PlexMediaItem> children = PlexServiceManager.instance()
                .getLibraryService()
                .getChildrenObserve(parent)
                .blockingFirst();

        return PlexMediaGroupAdapter.fromContainer(parent, children);
    }

    private static boolean isMovieLibrary(PlexLibrary library) {
        return library != null
                && library.getType() != null
                && TYPE_MOVIE.equalsIgnoreCase(library.getType());
    }

    private static boolean isShowLibrary(PlexLibrary library) {
        return library != null
                && library.getType() != null
                && TYPE_SHOW.equalsIgnoreCase(library.getType());
    }
}
