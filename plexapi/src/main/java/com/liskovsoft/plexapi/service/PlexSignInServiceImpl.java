package com.liskovsoft.plexapi.service;

import com.liskovsoft.plexapi.auth.PlexAuthPinImpl;
import com.liskovsoft.plexapi.auth.PlexPinResponse;
import com.liskovsoft.plexapi.network.PlexRetrofitHelper;
import com.liskovsoft.plexapi.network.PlexTvApi;
import com.liskovsoft.plexapi.prefs.PlexPrefs;
import com.liskovsoft.plexserviceinterfaces.PlexSignInService;
import com.liskovsoft.plexserviceinterfaces.data.PlexAuthPin;
import com.liskovsoft.sharedutils.mylogger.Log;

import java.io.IOException;

import io.reactivex.Observable;
import retrofit2.Response;

/**
 * Plex PIN auth against plex.tv; token persisted in {@link PlexPrefs}.
 */
public class PlexSignInServiceImpl implements PlexSignInService {
    private static final String TAG = PlexSignInServiceImpl.class.getSimpleName();
    static final int DEFAULT_PIN_POLL_ATTEMPTS = 150;
    static final long DEFAULT_PIN_POLL_INTERVAL_MS = 2_000;

    private final PlexTvApi mApi;
    private final PlexPrefs mPrefs;
    private final int mPollAttempts;
    private final long mPollIntervalMs;

    public PlexSignInServiceImpl() {
        this(PlexRetrofitHelper.createPlexTvApi(PlexTvApi.class), null,
                DEFAULT_PIN_POLL_ATTEMPTS, DEFAULT_PIN_POLL_INTERVAL_MS);
    }

    /** Package-visible for tests. */
    PlexSignInServiceImpl(PlexTvApi api, PlexPrefs prefs, int pollAttempts, long pollIntervalMs) {
        mApi = api;
        mPrefs = prefs;
        mPollAttempts = pollAttempts;
        mPollIntervalMs = pollIntervalMs;
    }

    private PlexPrefs prefs() {
        return mPrefs != null ? mPrefs : PlexPrefs.instance();
    }

    @Override
    public boolean isSigned() {
        String token = getAuthToken();
        return token != null && !token.isEmpty();
    }

    @Override
    public String getAuthToken() {
        return prefs().getAuthToken();
    }

    @Override
    public void setAuthToken(String token) {
        prefs().setAuthToken(token);
    }

    @Override
    public void signOut() {
        prefs().clearAuthToken();
        prefs().clearSelectedServer();
    }

    @Override
    public Observable<PlexAuthPin> signInWithPinObserve() {
        return Observable.create(emitter -> {
            try {
                Response<PlexPinResponse> createResponse = mApi.createPin(true).execute();
                if (!createResponse.isSuccessful() || createResponse.body() == null) {
                    emitter.onError(new IOException("Failed to create Plex PIN: HTTP "
                            + createResponse.code()));
                    return;
                }

                PlexPinResponse created = createResponse.body();
                if (created.getCode() == null || created.getCode().isEmpty()) {
                    emitter.onError(new IllegalStateException("Plex PIN response missing code"));
                    return;
                }

                String pinId = String.valueOf(created.getId());
                PlexAuthPin pin = new PlexAuthPinImpl(pinId, created.getCode());
                Log.d(TAG, "PIN created: code=" + created.getCode() + " id=" + pinId);
                emitter.onNext(pin);

                String token = pollForAuthToken(pinId, emitter);
                if (emitter.isDisposed()) {
                    return;
                }

                if (token == null || token.isEmpty()) {
                    emitter.onError(new IllegalStateException(
                            "Plex PIN expired or was not claimed within the timeout"));
                    return;
                }

                setAuthToken(token);
                Log.d(TAG, "PIN claimed; auth token stored");
                emitter.onComplete();
            } catch (Throwable e) {
                if (!emitter.isDisposed()) {
                    emitter.onError(e);
                }
            }
        });
    }

    private String pollForAuthToken(String pinId, io.reactivex.ObservableEmitter<PlexAuthPin> emitter)
            throws InterruptedException, IOException {
        for (int i = 0; i < mPollAttempts; i++) {
            if (emitter.isDisposed()) {
                return null;
            }

            Thread.sleep(mPollIntervalMs);

            if (emitter.isDisposed()) {
                return null;
            }

            Response<PlexPinResponse> pollResponse = mApi.checkPin(pinId).execute();
            if (!pollResponse.isSuccessful() || pollResponse.body() == null) {
                Log.d(TAG, "PIN poll HTTP " + pollResponse.code() + " (attempt " + (i + 1) + ")");
                continue;
            }

            PlexPinResponse body = pollResponse.body();
            if (body.hasAuthToken()) {
                return body.getAuthToken();
            }
        }
        return null;
    }
}
