package com.liskovsoft.plexapi.service;

import com.liskovsoft.plexapi.library.PlexLibraryImpl;
import com.liskovsoft.plexapi.network.PlexPmsApi;
import com.liskovsoft.plexapi.network.PlexRetrofitHelper;
import com.liskovsoft.plexapi.prefs.PlexPrefs;
import com.liskovsoft.plexapi.server.PlexServerImpl;
import com.liskovsoft.plexserviceinterfaces.data.PlexLibrary;
import com.liskovsoft.plexapi.library.PlexMediaItemImpl;
import com.liskovsoft.plexserviceinterfaces.data.PlexMediaItem;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@RunWith(RobolectricTestRunner.class)
public class PlexLibraryServiceImplTest {
    private MockWebServer mServer;
    private PlexPrefs mPrefs;
    private PlexLibraryServiceImpl mService;

    @Before
    public void setUp() throws Exception {
        PlexPrefs.unhold();
        PlexRetrofitHelper.reset();

        mPrefs = PlexPrefs.instance(RuntimeEnvironment.application);
        mPrefs.clearAuthToken();
        mPrefs.clearSelectedServer();

        mServer = new MockWebServer();
        mServer.start();

        String baseUrl = mServer.url("/").toString();
        mPrefs.setSelectedServer(new PlexServerImpl(
                "srv-1", "Home", baseUrl, "server-token", true, true));

        PlexPmsApi api = PlexRetrofitHelper.createPmsApi(baseUrl, PlexPmsApi.class);
        mService = new PlexLibraryServiceImpl(mPrefs, api, 50);
    }

    @After
    public void tearDown() throws Exception {
        mServer.shutdown();
        PlexPrefs.unhold();
        PlexRetrofitHelper.reset();
    }

    @Test
    public void getLibrariesObserve_mapsSections() throws Exception {
        mServer.enqueue(new MockResponse().setResponseCode(200).setBody("{"
                + "\"MediaContainer\":{"
                + "\"size\":2,"
                + "\"Directory\":["
                + "{\"key\":\"1\",\"type\":\"movie\",\"title\":\"Movies\"},"
                + "{\"key\":\"2\",\"type\":\"show\",\"title\":\"TV Shows\"}"
                + "]"
                + "}}"));

        List<PlexLibrary> libraries = mService.getLibrariesObserve().blockingFirst();

        assertEquals(2, libraries.size());
        assertEquals("1", libraries.get(0).getKey());
        assertEquals("Movies", libraries.get(0).getTitle());
        assertEquals("movie", libraries.get(0).getType());
        assertEquals("show", libraries.get(1).getType());

        RecordedRequest request = mServer.takeRequest(1, TimeUnit.SECONDS);
        assertNotNull(request);
        assertTrue(request.getPath().contains("library/sections"));
        assertEquals("server-token", request.getHeader("X-Plex-Token"));
    }

    @Test
    public void getMoviesPageObserve_mapsOffsetAndTotalSize() throws Exception {
        mServer.enqueue(new MockResponse().setResponseCode(200).setBody("{"
                + "\"MediaContainer\":{"
                + "\"offset\":50,"
                + "\"size\":1,"
                + "\"totalSize\":120,"
                + "\"Metadata\":[{"
                + "\"ratingKey\":\"1050\","
                + "\"key\":\"/library/metadata/1050\","
                + "\"type\":\"movie\","
                + "\"title\":\"Second Page\","
                + "\"year\":2002"
                + "}]"
                + "}}"));

        PlexLibrary library = new PlexLibraryImpl("1", "Movies", "movie");
        com.liskovsoft.plexserviceinterfaces.data.PlexMediaPage page =
                mService.getMoviesPageObserve(library, 50).blockingFirst();

        assertEquals(1, page.getItems().size());
        assertEquals("1050", page.getItems().get(0).getRatingKey());
        assertEquals(50, page.getOffset());
        assertEquals(120, page.getTotalSize());

        RecordedRequest request = mServer.takeRequest(1, TimeUnit.SECONDS);
        assertNotNull(request);
        assertEquals("50", request.getHeader("X-Plex-Container-Start"));
    }

    @Test
    public void getMoviesObserve_mapsFirstPageAndAbsoluteThumb() throws Exception {
        mServer.enqueue(new MockResponse().setResponseCode(200).setBody("{"
                + "\"MediaContainer\":{"
                + "\"Metadata\":[{"
                + "\"ratingKey\":\"1049\","
                + "\"key\":\"/library/metadata/1049\","
                + "\"type\":\"movie\","
                + "\"title\":\"Zoolander\","
                + "\"year\":2001,"
                + "\"duration\":5129000,"
                + "\"thumb\":\"/library/metadata/1049/thumb/1\""
                + "}]"
                + "}}"));

        PlexLibrary library = new PlexLibraryImpl("1", "Movies", "movie");
        List<PlexMediaItem> movies = mService.getMoviesObserve(library).blockingFirst();

        assertEquals(1, movies.size());
        PlexMediaItem movie = movies.get(0);
        assertEquals("1049", movie.getRatingKey());
        assertEquals("Zoolander", movie.getTitle());
        assertEquals(2001, movie.getYear());
        assertEquals(5129000L, movie.getDurationMs());
        assertTrue(movie.getThumbUrl().startsWith(mServer.url("/").toString()));
        assertTrue(movie.getThumbUrl().contains("library/metadata/1049/thumb/1"));
        assertTrue(movie.getThumbUrl().contains("X-Plex-Token=server-token"));

        RecordedRequest request = mServer.takeRequest(1, TimeUnit.SECONDS);
        assertNotNull(request);
        assertTrue(request.getPath().contains("library/sections/1/all"));
        assertTrue(request.getPath().contains("type=1"));
        assertEquals("0", request.getHeader("X-Plex-Container-Start"));
        assertEquals("50", request.getHeader("X-Plex-Container-Size"));
        assertEquals("server-token", request.getHeader("X-Plex-Token"));
    }

    @Test
    public void getShowsObserve_mapsFirstPage() throws Exception {
        mServer.enqueue(new MockResponse().setResponseCode(200).setBody("{"
                + "\"MediaContainer\":{"
                + "\"Metadata\":[{"
                + "\"ratingKey\":\"2001\","
                + "\"key\":\"/library/metadata/2001\","
                + "\"type\":\"show\","
                + "\"title\":\"Breaking Bad\","
                + "\"year\":2008"
                + "}]"
                + "}}"));

        PlexLibrary library = new PlexLibraryImpl("2", "TV Shows", "show");
        List<PlexMediaItem> shows = mService.getShowsObserve(library).blockingFirst();

        assertEquals(1, shows.size());
        assertEquals("2001", shows.get(0).getRatingKey());
        assertEquals("Breaking Bad", shows.get(0).getTitle());
        assertEquals("show", shows.get(0).getType());

        RecordedRequest request = mServer.takeRequest(1, TimeUnit.SECONDS);
        assertNotNull(request);
        assertTrue(request.getPath().contains("library/sections/2/all"));
        assertTrue(request.getPath().contains("type=2"));
    }

    @Test
    public void getChildrenObserve_mapsSeasonsOrEpisodes() throws Exception {
        mServer.enqueue(new MockResponse().setResponseCode(200).setBody("{"
                + "\"MediaContainer\":{"
                + "\"Metadata\":[{"
                + "\"ratingKey\":\"3001\","
                + "\"key\":\"/library/metadata/3001\","
                + "\"type\":\"season\","
                + "\"title\":\"Season 1\","
                + "\"year\":2008"
                + "}]"
                + "}}"));

        PlexMediaItem show = new PlexMediaItemImpl(
                "2001", "/library/metadata/2001", "Breaking Bad", "show", 0, null, 2008);
        List<PlexMediaItem> children = mService.getChildrenObserve(show).blockingFirst();

        assertEquals(1, children.size());
        assertEquals("3001", children.get(0).getRatingKey());
        assertEquals("season", children.get(0).getType());

        RecordedRequest request = mServer.takeRequest(1, TimeUnit.SECONDS);
        assertNotNull(request);
        assertTrue(request.getPath().contains("library/metadata/2001/children"));
    }

    @Test
    public void getLibrariesObserve_requiresSelectedServer() {
        mPrefs.clearSelectedServer();
        try {
            mService.getLibrariesObserve().blockingFirst();
            fail("expected IllegalStateException");
        } catch (RuntimeException e) {
            assertTrue(e.getCause() instanceof IllegalStateException
                    || e instanceof IllegalStateException);
        }
    }

    @Test
    public void sectionIdFromKey_stripsPath() {
        assertEquals("1", PlexLibraryServiceImpl.sectionIdFromKey("1"));
        assertEquals("3", PlexLibraryServiceImpl.sectionIdFromKey("/library/sections/3"));
    }

    @Test
    public void pmsToken_fallsBackToAccountToken() throws Exception {
        mPrefs.setSelectedServer(new PlexServerImpl(
                "srv-1", "Home", mServer.url("/").toString(), null, true, true));
        mPrefs.setAuthToken("account-token");

        PlexPmsApi api = PlexRetrofitHelper.createPmsApi(
                mServer.url("/").toString(), PlexPmsApi.class);
        PlexLibraryServiceImpl service = new PlexLibraryServiceImpl(mPrefs, api, 10);

        mServer.enqueue(new MockResponse().setResponseCode(200).setBody(
                "{\"MediaContainer\":{\"Directory\":[]}}"));

        service.getLibrariesObserve().blockingFirst();

        RecordedRequest request = mServer.takeRequest(1, TimeUnit.SECONDS);
        assertNotNull(request);
        assertEquals("account-token", request.getHeader("X-Plex-Token"));
    }
}
