package com.liskovsoft.smartyoutubetv2.common.app.presenters;

import androidx.annotation.Nullable;

import com.liskovsoft.mediaserviceinterfaces.data.MediaGroup;
import com.liskovsoft.plexapi.PlexServiceManager;
import com.liskovsoft.plexapi.adapter.PlexMediaGroupAdapter;
import com.liskovsoft.plexapi.library.PlexLibraryImpl;
import com.liskovsoft.plexapi.library.PlexPage;
import com.liskovsoft.plexapi.network.PlexPmsApi;
import com.liskovsoft.plexserviceinterfaces.PlexLibraryService;
import com.liskovsoft.plexserviceinterfaces.data.PlexHubGroup;
import com.liskovsoft.plexserviceinterfaces.data.PlexLibrary;
import com.liskovsoft.plexserviceinterfaces.data.PlexMediaItem;
import com.liskovsoft.plexserviceinterfaces.data.PlexMediaPage;
import com.liskovsoft.sharedutils.mylogger.Log;
import com.liskovsoft.sharedutils.prefs.GlobalPreferences;
import com.liskovsoft.sharedutils.rx.RxHelper;
import com.liskovsoft.smartyoutubetv2.common.R;
import com.liskovsoft.smartyoutubetv2.common.app.models.data.Video;
import com.liskovsoft.smartyoutubetv2.common.misc.PlexPlaybackHelper;

import android.content.Context;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import io.reactivex.Observable;

/**
 * Fork-only: loads Plex home-style browse rows (Continue / Watchlist / Recently Added /
 * Grid+Hub recommended) for the existing {@link BrowsePresenter} pipeline.
 */
public final class PlexBrowsePresenter {
    private static final String TAG = PlexBrowsePresenter.class.getSimpleName();
    private static final String TYPE_MOVIE = "movie";
    private static final String TYPE_SHOW = "show";
    private static final int MERGE_PAGE_CAP = 50;

    private PlexBrowsePresenter() {
    }

    public static boolean isPlexGroup(@Nullable MediaGroup group) {
        return group instanceof PlexMediaGroupAdapter;
    }

    /**
     * Cold observable: emits Home-style rows progressively (TV-friendly).
     * Movies block then TV block; empty shelves are skipped.
     */
    public static Observable<List<MediaGroup>> getLibraryRowsObserve() {
        return RxHelper.createLong(emitter -> {
            try {
                Context context = resolveContext();
                PlexLibraryService libraryService = PlexServiceManager.instance().getLibraryService();
                List<PlexLibrary> libraries = libraryService.getLibrariesObserve().blockingFirst();

                if (libraries == null || libraries.isEmpty()) {
                    Log.d(TAG, "No Plex libraries");
                    emitter.onNext(Collections.emptyList());
                    emitter.onComplete();
                    return;
                }

                List<PlexLibrary> movieLibraries = new ArrayList<>();
                List<PlexLibrary> showLibraries = new ArrayList<>();
                for (PlexLibrary library : libraries) {
                    if (isMovieLibrary(library)) {
                        movieLibraries.add(library);
                    } else if (isShowLibrary(library)) {
                        showLibraries.add(library);
                    }
                }

                int emitted = 0;
                if (!movieLibraries.isEmpty()) {
                    emitted += emitMovieRows(emitter, libraryService, movieLibraries, context);
                }
                if (!showLibraries.isEmpty()) {
                    emitted += emitShowRows(emitter, libraryService, showLibraries, context);
                }

                if (emitted == 0 && !emitter.isDisposed()) {
                    emitter.onNext(Collections.emptyList());
                }
                if (!emitter.isDisposed()) {
                    Log.d(TAG, "Finished Plex home rows, emitted=" + emitted);
                    emitter.onComplete();
                }
            } catch (Throwable e) {
                if (!emitter.isDisposed()) {
                    emitter.onError(e);
                }
            }
        });
    }

    private static int emitMovieRows(io.reactivex.ObservableEmitter<List<MediaGroup>> emitter,
                                     PlexLibraryService libraryService,
                                     List<PlexLibrary> movieLibraries,
                                     @Nullable Context context) {
        int emitted = 0;
        emitted += emitIfPresent(emitter, buildMergedShelf(
                libraryService, movieLibraries, PlexMediaGroupAdapter.Kind.ON_DECK,
                string(context, R.string.plex_row_continue_movies)));
        emitted += emitIfPresent(emitter, buildWatchlist(
                libraryService, string(context, R.string.plex_row_watchlist)));
        emitted += emitIfPresent(emitter, buildMergedShelf(
                libraryService, movieLibraries, PlexMediaGroupAdapter.Kind.RECENTLY_ADDED,
                string(context, R.string.plex_row_recently_added_movies)));
        emitted += emitIfPresent(emitter, buildRecommendedRow(
                libraryService, movieLibraries.get(0),
                string(context, R.string.plex_row_movies)));
        return emitted;
    }

    private static int emitShowRows(io.reactivex.ObservableEmitter<List<MediaGroup>> emitter,
                                    PlexLibraryService libraryService,
                                    List<PlexLibrary> showLibraries,
                                    @Nullable Context context) {
        int emitted = 0;
        emitted += emitIfPresent(emitter, buildMergedShelf(
                libraryService, showLibraries, PlexMediaGroupAdapter.Kind.ON_DECK,
                string(context, R.string.plex_row_continue_shows)));
        emitted += emitIfPresent(emitter, buildMergedShelf(
                libraryService, showLibraries, PlexMediaGroupAdapter.Kind.RECENTLY_ADDED,
                string(context, R.string.plex_row_recently_added_shows)));
        emitted += emitIfPresent(emitter, buildRecommendedRow(
                libraryService, showLibraries.get(0),
                string(context, R.string.plex_row_shows)));
        return emitted;
    }

    private static int emitIfPresent(io.reactivex.ObservableEmitter<List<MediaGroup>> emitter,
                                     @Nullable MediaGroup group) {
        if (emitter.isDisposed() || group == null || group.isEmpty()) {
            return 0;
        }
        emitter.onNext(Collections.singletonList(group));
        return 1;
    }

    @Nullable
    private static MediaGroup buildMergedShelf(PlexLibraryService libraryService,
                                               List<PlexLibrary> libraries,
                                               PlexMediaGroupAdapter.Kind kind,
                                               String title) {
        try {
            List<PlexMediaItem> merged = new ArrayList<>();
            Set<String> seen = new HashSet<>();
            PlexLibrary paginationLibrary = null;
            PlexPage paginationPage = null;

            for (PlexLibrary library : libraries) {
                if (merged.size() >= MERGE_PAGE_CAP) {
                    break;
                }
                PlexPage page;
                if (kind == PlexMediaGroupAdapter.Kind.ON_DECK) {
                    page = toPlexPage(libraryService.getOnDeckPageObserve(library, 0).blockingFirst());
                } else {
                    page = toPlexPage(libraryService.getRecentlyAddedPageObserve(library, 0).blockingFirst());
                }
                if (page == null || page.getItems().isEmpty()) {
                    continue;
                }
                if (paginationLibrary == null) {
                    paginationLibrary = library;
                    paginationPage = page;
                }
                for (PlexMediaItem item : page.getItems()) {
                    if (item.getRatingKey() == null || !seen.add(item.getRatingKey())) {
                        continue;
                    }
                    merged.add(item);
                    if (merged.size() >= MERGE_PAGE_CAP) {
                        break;
                    }
                }
            }

            if (merged.isEmpty()) {
                return null;
            }

            // Paginate only when a single library contributed (merged multi-lib has no stable offset).
            PlexPage pageForKey = libraries.size() == 1 ? paginationPage : null;
            PlexLibrary libForKey = libraries.size() == 1 ? paginationLibrary : null;
            return PlexMediaGroupAdapter.fromSimple(title, kind, libForKey, merged, pageForKey);
        } catch (Throwable e) {
            Log.e(TAG, "Failed shelf " + kind + ": "
                    + (e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName()));
            return null;
        }
    }

    @Nullable
    private static MediaGroup buildWatchlist(PlexLibraryService libraryService, String title) {
        try {
            PlexPage page = toPlexPage(libraryService
                    .getWatchlistPageObserve(PlexPmsApi.TYPE_MOVIE, 0)
                    .blockingFirst());
            if (page == null || page.getItems().isEmpty()) {
                return null;
            }
            return PlexMediaGroupAdapter.fromSimple(
                    title,
                    PlexMediaGroupAdapter.Kind.WATCHLIST,
                    null,
                    PlexPmsApi.TYPE_MOVIE,
                    page.getItems(),
                    page);
        } catch (Throwable e) {
            Log.e(TAG, "Failed watchlist: "
                    + (e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName()));
            return null;
        }
    }

    @Nullable
    private static MediaGroup buildRecommendedRow(PlexLibraryService libraryService,
                                                  PlexLibrary library,
                                                  String rowTitle) {
        try {
            List<PlexMediaItem> recommended = collectRecommendedItems(libraryService, library);
            return PlexMediaGroupAdapter.fromRecommended(library, rowTitle, recommended, null);
        } catch (Throwable e) {
            Log.e(TAG, "Failed recommended row " + library.getTitle() + ": "
                    + (e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName()));
            try {
                return PlexMediaGroupAdapter.fromRecommended(
                        library, rowTitle, Collections.emptyList(), null);
            } catch (Throwable ignored) {
                return null;
            }
        }
    }

    private static List<PlexMediaItem> collectRecommendedItems(PlexLibraryService libraryService,
                                                               PlexLibrary library) {
        List<PlexMediaItem> recommended = new ArrayList<>();
        Set<String> seen = new HashSet<>();
        try {
            List<PlexHubGroup> hubs = libraryService.getSectionHubsObserve(library).blockingFirst();
            if (hubs == null) {
                return recommended;
            }
            for (PlexHubGroup hub : hubs) {
                if (!isRecommendationHub(hub)) {
                    continue;
                }
                for (PlexMediaItem item : hub.getItems()) {
                    if (item.getRatingKey() == null || !seen.add(item.getRatingKey())) {
                        continue;
                    }
                    recommended.add(item);
                    if (recommended.size() >= MERGE_PAGE_CAP) {
                        return recommended;
                    }
                }
            }
        } catch (Throwable e) {
            Log.e(TAG, "Hub fetch failed for " + library.getTitle() + ": "
                    + (e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName()));
        }
        return recommended;
    }

    static boolean isRecommendationHub(@Nullable PlexHubGroup hub) {
        if (hub == null) {
            return false;
        }
        String id = hub.getHubIdentifier() != null
                ? hub.getHubIdentifier().toLowerCase(Locale.US) : "";
        String title = hub.getTitle() != null
                ? hub.getTitle().toLowerCase(Locale.US) : "";

        if (id.contains("continue") || id.contains("ondeck") || id.contains("on.deck")
                || id.contains("recentlyadded") || id.contains("recently.added")
                || id.contains("recentlyreleased") || id.contains("inprogress")) {
            return false;
        }
        if (title.contains("continue") || title.contains("on deck")
                || title.contains("recently added") || title.contains("in progress")) {
            return false;
        }

        return id.contains("recommend") || id.contains("promoted")
                || id.contains("discover") || id.contains("home.movies")
                || id.contains("home.tv") || id.contains("home.video")
                || title.contains("recommend") || title.contains("promoted")
                || title.contains("suggested") || title.contains("for you")
                || title.contains("empfohlen");
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

        if (group.isWatchlistGroup()) {
            page = toPlexPage(libraryService
                    .getWatchlistPageObserve(group.getWatchlistType(), offset)
                    .blockingFirst());
        } else if (group.isOnDeckGroup() && group.getPlexLibrary() != null) {
            page = toPlexPage(libraryService
                    .getOnDeckPageObserve(group.getPlexLibrary(), offset)
                    .blockingFirst());
        } else if (group.isRecentlyAddedGroup() && group.getPlexLibrary() != null) {
            page = toPlexPage(libraryService
                    .getRecentlyAddedPageObserve(group.getPlexLibrary(), offset)
                    .blockingFirst());
        } else if (group.getKind() == PlexMediaGroupAdapter.Kind.LIBRARY
                || group.getKind() == PlexMediaGroupAdapter.Kind.LIBRARY_GRID) {
            if (group.getPlexLibrary() == null) {
                return null;
            }
            page = fetchLibraryPage(libraryService, group.getPlexLibrary(), offset);
        } else if (group.isContainerGroup() && group.getPlexContainer() != null) {
            page = toPlexPage(libraryService
                    .getChildrenPageObserve(group.getPlexContainer(), offset)
                    .blockingFirst());
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

    @Nullable
    private static Context resolveContext() {
        try {
            return GlobalPreferences.context();
        } catch (Throwable e) {
            return null;
        }
    }

    private static String string(@Nullable Context context, int resId) {
        if (context != null) {
            return context.getString(resId);
        }
        if (resId == R.string.plex_row_continue_movies
                || resId == R.string.plex_row_continue_shows) {
            return "Continue Watching";
        }
        if (resId == R.string.plex_row_watchlist) {
            return "Watchlist";
        }
        if (resId == R.string.plex_row_recently_added_movies
                || resId == R.string.plex_row_recently_added_shows) {
            return "Recently Added";
        }
        if (resId == R.string.plex_row_movies) {
            return "Movies";
        }
        if (resId == R.string.plex_row_shows) {
            return "TV Shows";
        }
        return "Plex";
    }
}
