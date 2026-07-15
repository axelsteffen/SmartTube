package com.liskovsoft.plexapi.service;

import com.liskovsoft.plexapi.library.PlexLibraryImpl;
import com.liskovsoft.plexapi.library.PlexMediaItemImpl;
import com.liskovsoft.plexapi.library.PlexPage;
import com.liskovsoft.plexapi.network.PlexPmsApi;
import com.liskovsoft.plexapi.network.PlexRetrofitHelper;
import com.liskovsoft.plexapi.network.dto.MediaContainer;
import com.liskovsoft.plexapi.network.dto.MediaContainerResponse;
import com.liskovsoft.plexapi.network.dto.PlexDirectory;
import com.liskovsoft.plexapi.network.dto.PlexMetadata;
import com.liskovsoft.plexapi.prefs.PlexPrefs;
import com.liskovsoft.plexserviceinterfaces.PlexLibraryService;
import com.liskovsoft.plexserviceinterfaces.data.PlexLibrary;
import com.liskovsoft.plexserviceinterfaces.data.PlexMediaItem;
import com.liskovsoft.plexserviceinterfaces.data.PlexMediaPage;
import com.liskovsoft.plexserviceinterfaces.data.PlexServer;
import com.liskovsoft.sharedutils.mylogger.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import retrofit2.Response;

/**
 * Lists PMS library sections, section items, and metadata children (Phase 1.5 / 3.3 / 3.4).
 */
public class PlexLibraryServiceImpl implements PlexLibraryService {
    private static final String TAG = PlexLibraryServiceImpl.class.getSimpleName();
    /** First-page size for TV / lazy loading. */
    static final int DEFAULT_PAGE_SIZE = 50;

    private final PlexPrefs mPrefs;
    private final PlexPmsApi mApi;
    private final int mPageSize;

    public PlexLibraryServiceImpl() {
        this(null, null, DEFAULT_PAGE_SIZE);
    }

    /** Package-visible for tests. */
    PlexLibraryServiceImpl(PlexPrefs prefs, PlexPmsApi api, int pageSize) {
        mPrefs = prefs;
        mApi = api;
        mPageSize = pageSize > 0 ? pageSize : DEFAULT_PAGE_SIZE;
    }

    private PlexPrefs prefs() {
        return mPrefs != null ? mPrefs : PlexPrefs.instance();
    }

    @Override
    public Observable<List<PlexLibrary>> getLibrariesObserve() {
        return Observable.fromCallable(this::fetchLibraries);
    }

    @Override
    public Observable<List<PlexMediaItem>> getMoviesObserve(PlexLibrary library) {
        return Observable.fromCallable(() -> fetchSectionItems(library, PlexPmsApi.TYPE_MOVIE, "movie"));
    }

    @Override
    public Observable<List<PlexMediaItem>> getShowsObserve(PlexLibrary library) {
        return Observable.fromCallable(() -> fetchSectionItems(library, PlexPmsApi.TYPE_SHOW, "show"));
    }

    @Override
    public Observable<List<PlexMediaItem>> getChildrenObserve(PlexMediaItem parent) {
        return Observable.fromCallable(() -> fetchChildren(parent));
    }

    @Override
    public Observable<PlexMediaPage> getMoviesPageObserve(PlexLibrary library, int offset) {
        return Observable.fromCallable(() -> fetchSectionPage(library, PlexPmsApi.TYPE_MOVIE, "movie", offset));
    }

    @Override
    public Observable<PlexMediaPage> getShowsPageObserve(PlexLibrary library, int offset) {
        return Observable.fromCallable(() -> fetchSectionPage(library, PlexPmsApi.TYPE_SHOW, "show", offset));
    }

    @Override
    public Observable<PlexMediaPage> getChildrenPageObserve(PlexMediaItem parent, int offset) {
        return Observable.fromCallable(() -> fetchChildrenPage(parent, offset));
    }

    private List<PlexLibrary> fetchLibraries() throws IOException {
        PlexServer server = requireSelectedServer();
        PlexPmsApi api = pmsApi(server);
        String token = pmsToken(server);

        Response<MediaContainerResponse> response = api.getLibrarySections(token).execute();
        MediaContainer container = requireContainer(response, "list library sections");

        List<PlexLibrary> libraries = new ArrayList<>();
        for (PlexDirectory directory : container.getDirectories()) {
            PlexLibraryImpl library = PlexLibraryImpl.fromDirectory(directory);
            if (library != null) {
                libraries.add(library);
            }
        }

        Log.d(TAG, "Listed " + libraries.size() + " library section(s)");
        return libraries;
    }

    private List<PlexMediaItem> fetchSectionItems(PlexLibrary library, int type, String label)
            throws IOException {
        PlexPage page = fetchSectionPage(library, type, label, 0);
        return page.getItems();
    }

    private PlexPage fetchSectionPage(PlexLibrary library, int type, String label, int offset)
            throws IOException {
        if (library == null || library.getKey() == null || library.getKey().isEmpty()) {
            throw new IllegalArgumentException("library with key required");
        }

        PlexServer server = requireSelectedServer();
        PlexPmsApi api = pmsApi(server);
        String token = pmsToken(server);
        String sectionId = sectionIdFromKey(library.getKey());

        Response<MediaContainerResponse> response = api.getSectionItems(
                sectionId, type, offset, mPageSize, token).execute();
        MediaContainer container = requireContainer(response, "list " + label + " for section " + sectionId);

        List<PlexMediaItem> items = mapMetadata(container, server, token);
        int totalSize = resolveTotalSize(container, offset, items.size());
        Log.d(TAG, "Listed page offset=" + offset + " size=" + items.size()
                + " total=" + totalSize + " " + label + "(s) from section " + sectionId);
        return new PlexPage(items, offset, totalSize);
    }

    private List<PlexMediaItem> fetchChildren(PlexMediaItem parent) throws IOException {
        PlexPage page = fetchChildrenPage(parent, 0);
        return page.getItems();
    }

    private PlexPage fetchChildrenPage(PlexMediaItem parent, int offset) throws IOException {
        if (parent == null || parent.getRatingKey() == null || parent.getRatingKey().isEmpty()) {
            throw new IllegalArgumentException("parent with ratingKey required");
        }

        PlexServer server = requireSelectedServer();
        PlexPmsApi api = pmsApi(server);
        String token = pmsToken(server);

        Response<MediaContainerResponse> response = api.getMetadataChildren(
                parent.getRatingKey(), offset, mPageSize, token).execute();
        MediaContainer container = requireContainer(response,
                "list children for ratingKey " + parent.getRatingKey());

        List<PlexMediaItem> items = mapMetadata(container, server, token);
        int totalSize = resolveTotalSize(container, offset, items.size());
        Log.d(TAG, "Listed page offset=" + offset + " size=" + items.size()
                + " total=" + totalSize + " child item(s) for " + parent.getTitle());
        return new PlexPage(items, offset, totalSize);
    }

    private static int resolveTotalSize(MediaContainer container, int offset, int pageCount) {
        Integer totalSize = container.getTotalSize();
        if (totalSize != null && totalSize >= 0) {
            return totalSize;
        }
        Integer size = container.getSize();
        if (size != null && size >= 0) {
            return offset + size;
        }
        return offset + pageCount;
    }

    private List<PlexMediaItem> mapMetadata(MediaContainer container, PlexServer server, String token) {
        List<PlexMediaItem> items = new ArrayList<>();
        for (PlexMetadata metadata : container.getMetadata()) {
            PlexMediaItemImpl item = PlexMediaItemImpl.fromMetadata(
                    metadata, server.getBaseUrl(), token);
            if (item != null) {
                items.add(item);
            }
        }
        return items;
    }

    private PlexServer requireSelectedServer() {
        PlexServer server = prefs().getSelectedServer();
        if (server == null || server.getBaseUrl() == null || server.getBaseUrl().isEmpty()) {
            throw new IllegalStateException("No Plex server selected (Phase 1.4)");
        }
        return server;
    }

    private PlexPmsApi pmsApi(PlexServer server) {
        if (mApi != null) {
            return mApi;
        }
        return PlexRetrofitHelper.createPmsApi(server.getBaseUrl(), PlexPmsApi.class);
    }

    private String pmsToken(PlexServer server) {
        String serverToken = server.getAccessToken();
        if (serverToken != null && !serverToken.isEmpty()) {
            return serverToken;
        }
        String accountToken = prefs().getAuthToken();
        if (accountToken != null && !accountToken.isEmpty()) {
            return accountToken;
        }
        throw new IllegalStateException("No Plex auth token available for PMS calls");
    }

    private static MediaContainer requireContainer(Response<MediaContainerResponse> response,
                                                   String action) throws IOException {
        if (!response.isSuccessful()) {
            throw new IOException("Failed to " + action + ": HTTP " + response.code());
        }
        MediaContainerResponse body = response.body();
        if (body == null || body.getMediaContainer() == null) {
            throw new IOException("Failed to " + action + ": empty MediaContainer");
        }
        return body.getMediaContainer();
    }

    /** Section Directory.key is usually the numeric id; tolerate path-style keys. */
    static String sectionIdFromKey(String key) {
        if (key == null || key.isEmpty()) {
            return key;
        }
        String trimmed = key.endsWith("/") ? key.substring(0, key.length() - 1) : key;
        int slash = trimmed.lastIndexOf('/');
        return slash >= 0 ? trimmed.substring(slash + 1) : trimmed;
    }
}
