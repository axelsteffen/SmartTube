package com.liskovsoft.plexapi.service;

import com.liskovsoft.plexapi.network.PlexRetrofitHelper;
import com.liskovsoft.plexapi.network.PlexTvResourcesApi;
import com.liskovsoft.plexapi.prefs.PlexPrefs;
import com.liskovsoft.plexapi.server.PlexServerImpl;
import com.liskovsoft.plexserviceinterfaces.data.PlexServer;

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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

@RunWith(RobolectricTestRunner.class)
public class PlexServerServiceImplTest {
    private MockWebServer mServer;
    private PlexPrefs mPrefs;
    private PlexServerServiceImpl mService;

    @Before
    public void setUp() throws Exception {
        PlexPrefs.unhold();
        PlexRetrofitHelper.reset();

        mPrefs = PlexPrefs.instance(RuntimeEnvironment.application);
        mPrefs.clearAuthToken();
        mPrefs.clearSelectedServer();
        mPrefs.setAuthToken("account-token");

        mServer = new MockWebServer();
        mServer.start();
        PlexRetrofitHelper.setPlexTvBaseUrl(mServer.url("/").toString());

        PlexTvResourcesApi api = PlexRetrofitHelper.createPlexTvApi(PlexTvResourcesApi.class);
        mService = new PlexServerServiceImpl(api, mPrefs);
    }

    @After
    public void tearDown() throws Exception {
        mServer.shutdown();
        PlexPrefs.unhold();
        PlexRetrofitHelper.reset();
    }

    @Test
    public void getServersObserve_filtersServersAndPicksLocalHttps() throws Exception {
        mServer.enqueue(new MockResponse().setResponseCode(200).setBody("["
                + "{"
                + "\"name\":\"Phone\",\"product\":\"Plex for Android\",\"provides\":\"client\","
                + "\"clientIdentifier\":\"phone\",\"owned\":true,\"presence\":true,"
                + "\"connections\":[{\"uri\":\"http://1.2.3.4:32400\",\"local\":true}]"
                + "},"
                + "{"
                + "\"name\":\"NAS\",\"product\":\"Plex Media Server\",\"provides\":\"server\","
                + "\"clientIdentifier\":\"nas-1\",\"owned\":true,\"presence\":true,"
                + "\"accessToken\":\"srv-token\","
                + "\"connections\":["
                + "{\"protocol\":\"http\",\"uri\":\"http://nas.example:32400\",\"local\":false},"
                + "{\"protocol\":\"https\",\"uri\":\"https://192.168.1.5:32400\",\"local\":true}"
                + "]"
                + "}"
                + "]"));

        List<PlexServer> servers = mService.getServersObserve().blockingFirst();

        assertEquals(1, servers.size());
        PlexServer server = servers.get(0);
        assertEquals("nas-1", server.getClientIdentifier());
        assertEquals("NAS", server.getName());
        assertEquals("https://192.168.1.5:32400/", server.getBaseUrl());
        assertEquals("srv-token", server.getAccessToken());
        assertTrue(server.isOwned());
        assertTrue(server.isOnline());

        RecordedRequest request = mServer.takeRequest(1, TimeUnit.SECONDS);
        assertNotNull(request);
        assertEquals("GET", request.getMethod());
        assertTrue(request.getPath().contains("api/v2/resources"));
        assertTrue(request.getPath().contains("includeHttps=1"));
        assertTrue(request.getPath().contains("includeRelay=1"));
        assertEquals("account-token", request.getHeader("X-Plex-Token"));
    }

    @Test
    public void selectServer_persistsAcrossInstances() {
        PlexServer server = new PlexServerImpl(
                "id-1", "Living Room", "https://10.0.0.8:32400", "tok-1", true, true);

        mService.selectServer(server);

        assertEquals("id-1", mService.getSelectedServer().getClientIdentifier());
        assertEquals("https://10.0.0.8:32400/", mService.getSelectedServer().getBaseUrl());

        PlexTvResourcesApi api = PlexRetrofitHelper.createPlexTvApi(PlexTvResourcesApi.class);
        PlexServerServiceImpl reloaded = new PlexServerServiceImpl(api, mPrefs);
        PlexServer selected = reloaded.getSelectedServer();
        assertNotNull(selected);
        assertEquals("id-1", selected.getClientIdentifier());
        assertEquals("Living Room", selected.getName());
        assertEquals("https://10.0.0.8:32400/", selected.getBaseUrl());
        assertEquals("tok-1", selected.getAccessToken());
        assertTrue(selected.isOwned());
    }

    @Test
    public void selectServer_null_clearsSelection() {
        mService.selectServer(new PlexServerImpl(
                "id-1", "A", "https://10.0.0.8:32400", "tok", true, true));
        mService.selectServer(null);
        assertNull(mService.getSelectedServer());
    }

    @Test
    public void signOut_clearsSelectedServer() {
        mService.selectServer(new PlexServerImpl(
                "id-1", "A", "https://10.0.0.8:32400", "tok", true, true));

        PlexSignInServiceImpl signIn = new PlexSignInServiceImpl(
                PlexRetrofitHelper.createPlexTvApi(com.liskovsoft.plexapi.network.PlexTvApi.class),
                mPrefs, 1, 1);
        signIn.signOut();

        assertNull(mPrefs.getSelectedServer());
        assertNull(mPrefs.getAuthToken());
    }
}
