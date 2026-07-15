package com.liskovsoft.plexapi.service;

import com.liskovsoft.plexapi.library.PlexMediaItemImpl;
import com.liskovsoft.plexapi.media.PlexStreamInfoImpl;
import com.liskovsoft.plexapi.network.PlexPmsApi;
import com.liskovsoft.plexapi.network.PlexRetrofitHelper;
import com.liskovsoft.plexapi.prefs.PlexPrefs;
import com.liskovsoft.plexapi.server.PlexServerImpl;
import com.liskovsoft.plexserviceinterfaces.data.PlexMediaItem;
import com.liskovsoft.plexserviceinterfaces.data.PlexStreamInfo;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import java.util.concurrent.TimeUnit;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@RunWith(RobolectricTestRunner.class)
public class PlexMediaServiceImplTest {
    private MockWebServer mServer;
    private PlexPrefs mPrefs;
    private PlexMediaServiceImpl mService;
    private String mBaseUrl;

    @Before
    public void setUp() throws Exception {
        PlexPrefs.unhold();
        PlexRetrofitHelper.reset();

        mPrefs = PlexPrefs.instance(RuntimeEnvironment.application);
        mPrefs.clearAuthToken();
        mPrefs.clearSelectedServer();

        mServer = new MockWebServer();
        mServer.start();

        mBaseUrl = mServer.url("/").toString();
        mPrefs.setSelectedServer(new PlexServerImpl(
                "srv-1", "Home", mBaseUrl, "server-token", true, true));

        PlexPmsApi api = PlexRetrofitHelper.createPmsApi(mBaseUrl, PlexPmsApi.class);
        mService = new PlexMediaServiceImpl(mPrefs, api);
    }

    @After
    public void tearDown() throws Exception {
        mServer.shutdown();
        PlexPrefs.unhold();
        PlexRetrofitHelper.reset();
    }

    @Test
    public void getStreamInfoObserve_directPlayFromMetadata() throws Exception {
        mServer.enqueue(new MockResponse().setResponseCode(200).setBody("{"
                + "\"MediaContainer\":{"
                + "\"Metadata\":[{"
                + "\"ratingKey\":\"1049\","
                + "\"key\":\"/library/metadata/1049\","
                + "\"Media\":[{"
                + "\"container\":\"mkv\","
                + "\"Part\":[{"
                + "\"id\":827,"
                + "\"key\":\"/library/parts/827/file.mkv\","
                + "\"container\":\"mkv\""
                + "}]"
                + "}]"
                + "}]"
                + "}}"));

        PlexMediaItem item = movie("1049");
        PlexStreamInfo stream = mService.getStreamInfoObserve(item).blockingFirst();

        assertNotNull(stream);
        assertFalse(stream.isTranscoded());
        assertEquals("video/x-matroska", stream.getContainer());
        assertTrue(stream.getUrl().startsWith(mBaseUrl));
        assertTrue(stream.getUrl().contains("library/parts/827/file.mkv"));
        assertTrue(stream.getUrl().contains("X-Plex-Token=server-token"));

        RecordedRequest request = mServer.takeRequest(1, TimeUnit.SECONDS);
        assertNotNull(request);
        assertTrue(request.getPath().contains("library/metadata/1049"));
        assertEquals("server-token", request.getHeader("X-Plex-Token"));
        assertEquals(1, mServer.getRequestCount()); // metadata only, no decision
    }

    @Test
    public void getStreamInfoObserve_fallsBackToDecisionWhenNoPartKey() throws Exception {
        mServer.enqueue(new MockResponse().setResponseCode(200).setBody("{"
                + "\"MediaContainer\":{"
                + "\"Metadata\":[{"
                + "\"ratingKey\":\"1049\","
                + "\"Media\":[{\"container\":\"mkv\",\"Part\":[{}]}]"
                + "}]"
                + "}}"));
        mServer.enqueue(new MockResponse().setResponseCode(200).setBody("{"
                + "\"MediaContainer\":{"
                + "\"Metadata\":[{"
                + "\"Media\":[{"
                + "\"Part\":[{"
                + "\"key\":\"/video/:/transcode/universal/start.m3u8?path=%2Flibrary%2Fmetadata%2F1049\","
                + "\"decision\":\"transcode\","
                + "\"protocol\":\"hls\""
                + "}]"
                + "}]"
                + "}]"
                + "}}"));

        PlexStreamInfo stream = mService.getStreamInfoObserve(movie("1049")).blockingFirst();

        assertTrue(stream.isTranscoded());
        assertEquals("application/x-mpegURL", stream.getContainer());
        assertTrue(stream.getUrl().contains("transcode/universal/start.m3u8"));
        assertTrue(stream.getUrl().contains("X-Plex-Token=server-token"));

        RecordedRequest meta = mServer.takeRequest(1, TimeUnit.SECONDS);
        assertNotNull(meta);
        assertTrue(meta.getPath().contains("library/metadata/1049"));

        RecordedRequest decision = mServer.takeRequest(1, TimeUnit.SECONDS);
        assertNotNull(decision);
        assertTrue(decision.getPath().contains("video/:/transcode/universal/decision"));
        assertTrue(decision.getPath().contains("path=%2Flibrary%2Fmetadata%2F1049")
                || decision.getPath().contains("path=/library/metadata/1049"));
        assertTrue(decision.getPath().contains("protocol=hls"));
        assertEquals("server-token", decision.getHeader("X-Plex-Token"));
    }

    @Test
    public void getStreamInfoObserve_requiresRatingKey() {
        try {
            mService.getStreamInfoObserve(new PlexMediaItemImpl(
                    null, null, "x", "movie", 0, null, 0)).blockingFirst();
            fail("expected IllegalArgumentException");
        } catch (RuntimeException e) {
            assertTrue(e.getCause() instanceof IllegalArgumentException
                    || e instanceof IllegalArgumentException);
        }
    }

    @Test
    public void isTranscodeDecision_directPlayFalse() {
        assertFalse(PlexMediaServiceImpl.isTranscodeDecision("directplay"));
        assertFalse(PlexMediaServiceImpl.isTranscodeDecision("directstream"));
        assertTrue(PlexMediaServiceImpl.isTranscodeDecision("transcode"));
        assertTrue(PlexMediaServiceImpl.isTranscodeDecision(null));
    }

    @Test
    public void mimeHint_mapsCommonContainers() {
        assertEquals("video/mp4", PlexStreamInfoImpl.mimeHint("mp4"));
        assertEquals("application/x-mpegURL", PlexStreamInfoImpl.mimeHint("hls"));
        assertEquals("video/x-matroska", PlexStreamInfoImpl.mimeHint("mkv"));
    }

    private static PlexMediaItem movie(String ratingKey) {
        return new PlexMediaItemImpl(
                ratingKey, "/library/metadata/" + ratingKey, "Zoolander", "movie",
                5129000L, null, 2001);
    }
}
