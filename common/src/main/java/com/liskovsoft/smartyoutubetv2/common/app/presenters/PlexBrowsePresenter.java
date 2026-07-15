package com.liskovsoft.smartyoutubetv2.common.app.presenters;

import androidx.annotation.Nullable;

import com.liskovsoft.mediaserviceinterfaces.data.MediaGroup;
import com.liskovsoft.plexapi.PlexServiceManager;
import com.liskovsoft.plexapi.adapter.PlexMediaGroupAdapter;
import com.liskovsoft.plexapi.library.PlexLibraryImpl;
import com.liskovsoft.plexapi.library.PlexPage;
import com.liskovsoft.plexserviceinterfaces.PlexLibraryService;
import com.liskovsoft.plexserviceinterfaces.data.PlexLibrary;
import com.liskovsoft.plexserviceinterfaces.data.PlexMediaItem;
import com.liskovsoft.plexserviceinterfaces.data.PlexMediaPage;
import com.liskovsoft.sharedutils.mylogger.Log;
import com.liskovsoft.sharedutils.rx.RxHelper;
import com.liskovsoft.smartyoutubetv2.common.app.models.data.Video;
import com.liskovsoft.smartyoutubetv2.common.misc.PlexPlaybackHelper;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;

/**
 * Fork-only: loads Plex movie/show libraries as browse rows, resolves
 * show/season drill-down, library grids, and row/grid pagination
 * for the existing {@link BrowsePresenter} pipeline (Phase 3.2–3.4).
 */
public final class PlexBrowsePresenter {
    private static final String TAG = PlexBrowsePresenter.class.getSimpleName();
    private static final String TYPE_MOVIE = "movie";
    private static final String TYPE_SHOW = "show";

    private PlexBrowsePresenter() {
    }

    public static boolean isPlexGroup(@Nullable MediaGroup group) {
        return group instanceof PlexMediaGroupAdapter;
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

    /**
     * Full paginated library grid from a browse stub ({@link Video#getReloadPageKey()}).
     */
    @Nullable
    public static Observable<MediaGroup> getLibraryGridObserve(@Nullable Video video) {
        if (video == null || !video.isPlex() || !video.hasReloadPageKey()) {
            return null;
        }
        return RxHelper.fromCallable(() -> fetchLibraryGrid(video));
    }

    /**
     * Next page for a Plex row or grid ({@link BrowsePresenter} / {@link ChannelUploadsPresenter}).
     */
    @Nullable
    public static Observable<MediaGroup> continueGroupObserve(@Nullable MediaGroup group) {
        if (!(group instanceof PlexMediaGroupAdapter)) {
            return null;
        }
        return RxHelper.fromCallable(() -> fetchContinueGroup((PlexMediaGroupAdapter) group));
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
            PlexPage page = fetchLibraryPage(libraryService, library, 0);
            if (page == null || page.getItems().isEmpty()) {
                continue;
            }

            MediaGroup group = PlexMediaGroupAdapter.from(library, page.getItems(), page);
            if (group != null && !group.isEmpty()) {
                rows.add(group);
            }
        }

        Log.d(TAG, "Built " + rows.size() + " Plex library row(s)");
        return rows;
    }

    @Nullable
    private static MediaGroup fetchChildrenGroup(Video video) {
        PlexMediaItem parent = PlexPlaybackHelper.resolvePlexMediaItem(video);
        if (parent == null) {
            return null;
        }

        PlexPage page = toPlexPage(PlexServiceManager.instance()
                .getLibraryService()
                .getChildrenPageObserve(parent, 0)
                .blockingFirst());

        return PlexMediaGroupAdapter.fromContainer(parent, page.getItems(), page);
    }

    @Nullable
    private static MediaGroup fetchLibraryGrid(Video video) {
        String libraryKey = video.getReloadPageKey();
        if (libraryKey == null || libraryKey.isEmpty()) {
            return null;
        }

        String libraryType = video.playlistParams != null ? video.playlistParams : TYPE_MOVIE;
        String title = video.title != null ? video.title : libraryKey;
        PlexLibrary library = new PlexLibraryImpl(libraryKey, title, libraryType);

        PlexPage page = fetchLibraryPage(PlexServiceManager.instance().getLibraryService(), library, 0);
        if (page == null || page.getItems().isEmpty()) {
            return null;
        }

        return PlexMediaGroupAdapter.fromLibraryGrid(library, page.getItems(), page);
    }

    @Nullable
    private static MediaGroup fetchContinueGroup(PlexMediaGroupAdapter group) {
        String nextPageKey = group.getNextPageKey();
        if (nextPageKey == null || nextPageKey.isEmpty()) {
            return null;
        }

        int offset = parseOffset(nextPageKey);
        if (offset < 0) {
            return null;
        }

        PlexLibraryService libraryService = PlexServiceManager.instance().getLibraryService();
        PlexPage page;

        if (group.isLibraryGroup() && group.getPlexLibrary() != null) {
            page = fetchLibraryPage(libraryService, group.getPlexLibrary(), offset);
        } else if (group.isContainerGroup() && group.getPlexContainer() != null) {
            page = toPlexPage(libraryService.getChildrenPageObserve(group.getPlexContainer(), offset).blockingFirst());
        } else {
            return null;
        }

        if (page == null || page.getItems().isEmpty()) {
            return null;
        }

        return PlexMediaGroupAdapter.continueFrom(group, page.getItems(), page);
    }

    @Nullable
    private static PlexPage fetchLibraryPage(PlexLibraryService libraryService,
                                             PlexLibrary library,
                                             int offset) {
        if (libraryService == null || library == null) {
            return null;
        }

        PlexMediaPage page;
        if (isMovieLibrary(library)) {
            page = libraryService.getMoviesPageObserve(library, offset).blockingFirst();
        } else if (isShowLibrary(library)) {
            page = libraryService.getShowsPageObserve(library, offset).blockingFirst();
        } else {
            return null;
        }

        return toPlexPage(page);
    }

    @Nullable
    private static PlexPage toPlexPage(@Nullable PlexMediaPage page) {
        if (page == null) {
            return null;
        }
        return new PlexPage(page.getItems(), page.getOffset(), page.getTotalSize());
    }

    private static int parseOffset(String nextPageKey) {
        try {
            return Integer.parseInt(nextPageKey);
        } catch (NumberFormatException e) {
            Log.e(TAG, "Invalid Plex nextPageKey: " + nextPageKey);
            return -1;
        }
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
