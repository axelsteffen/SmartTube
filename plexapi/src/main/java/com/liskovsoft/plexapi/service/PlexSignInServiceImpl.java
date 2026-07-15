package com.liskovsoft.plexapi.service;

import com.liskovsoft.plexserviceinterfaces.PlexSignInService;
import com.liskovsoft.plexserviceinterfaces.data.PlexAuthPin;

import io.reactivex.Observable;

/**
 * Stub: real PIN/token auth lands in Phase 1.3.
 */
public class PlexSignInServiceImpl implements PlexSignInService {
    private String mAuthToken;

    @Override
    public boolean isSigned() {
        return mAuthToken != null && !mAuthToken.isEmpty();
    }

    @Override
    public String getAuthToken() {
        return mAuthToken;
    }

    @Override
    public void setAuthToken(String token) {
        mAuthToken = token;
    }

    @Override
    public void signOut() {
        mAuthToken = null;
    }

    @Override
    public Observable<PlexAuthPin> signInWithPinObserve() {
        return Observable.error(new UnsupportedOperationException("Plex PIN auth not implemented yet (Phase 1.3)"));
    }
}
