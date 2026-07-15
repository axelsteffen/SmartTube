package com.liskovsoft.plexapi.service;

import com.liskovsoft.plexapi.network.PlexHeaders;
import com.liskovsoft.plexapi.network.PlexRetrofitHelper;
import com.liskovsoft.plexapi.network.PlexTvApi;
import com.liskovsoft.plexapi.prefs.PlexPrefs;
import com.liskovsoft.plexserviceinterfaces.data.PlexAuthPin;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

@RunWith(RobolectricTestRunner.class)
public class PlexSignInServiceImplTest {
    private MockWebServer mServer;
    private PlexPrefs mPrefs;
    private PlexSignInServiceImpl mService;

    @Before
    public void setUp() throws Exception {
        PlexPrefs.unhold();
        PlexRetrofitHelper.reset();

        mPrefs = PlexPrefs.instance(RuntimeEnvironment.application);
        mPrefs.clearAuthToken();

        mServer = new MockWebServer();
        mServer.start();
        PlexRetrofitHelper.setPlexTvBaseUrl(mServer.url("/").toString());

        PlexTvApi api = PlexRetrofitHelper.createPlexTvApi(PlexTvApi.class);
        mService = new PlexSignInServiceImpl(api, mPrefs, 5, 10);
    }

    @After
    public void tearDown() throws Exception {
        mServer.shutdown();
        PlexPrefs.unhold();
        PlexRetrofitHelper.reset();
    }

    @Test
    public void setAuthToken_persistsAcrossInstances() {
        mService.setAuthToken("test-token-abc");
        assertTrue(mService.isSigned());
        assertEquals("test-token-abc", mService.getAuthToken());

        PlexPrefs.unhold();
        PlexPrefs reloaded = PlexPrefs.instance(RuntimeEnvironment.application);
        assertEquals("test-token-abc", reloaded.getAuthToken());
    }

    @Test
    public void signOut_clearsToken() {
        mService.setAuthToken("to-clear");
        mService.signOut();
        assertFalse(mService.isSigned());
        assertNull(mService.getAuthToken());
    }

    @Test
    public void clientIdentifier_isStable() {
        String first = mPrefs.getClientIdentifier();
        String second = mPrefs.getClientIdentifier();
        assertNotNull(first);
        assertEquals(first, second);

        PlexPrefs.unhold();
        assertEquals(first, PlexPrefs.instance(RuntimeEnvironment.application).getClientIdentifier());
    }

    @Test
    public void signInWithPin_emitsCode_thenStoresToken() throws Exception {
        mServer.enqueue(new MockResponse()
                .setResponseCode(201)
                .setBody("{\"id\":42,\"code\":\"ABCD\",\"authToken\":null,\"expiresIn\":900}"));
        mServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{\"id\":42,\"code\":\"ABCD\",\"authToken\":null,\"expiresIn\":900}"));
        mServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{\"id\":42,\"code\":\"ABCD\",\"authToken\":\"plex-auth-token-1\",\"expiresIn\":900}"));

        AtomicReference<PlexAuthPin> pinRef = new AtomicReference<>();
        mService.signInWithPinObserve()
                .blockingSubscribe(pinRef::set, throwable -> {
                    throw new RuntimeException(throwable);
                });

        PlexAuthPin pin = pinRef.get();
        assertNotNull(pin);
        assertEquals("42", pin.getId());
        assertEquals("ABCD", pin.getCode());
        assertEquals("https://plex.tv/link", pin.getAuthUrl());
        assertEquals("plex-auth-token-1", mService.getAuthToken());
        assertTrue(mService.isSigned());

        RecordedRequest createReq = mServer.takeRequest(1, TimeUnit.SECONDS);
        assertNotNull(createReq);
        assertEquals("POST", createReq.getMethod());
        assertTrue(createReq.getPath().contains("api/v2/pins"));
        assertEquals(PlexHeaders.PRODUCT_VALUE, createReq.getHeader(PlexHeaders.PRODUCT));
        assertNotNull(createReq.getHeader(PlexHeaders.CLIENT_IDENTIFIER));
    }
}
