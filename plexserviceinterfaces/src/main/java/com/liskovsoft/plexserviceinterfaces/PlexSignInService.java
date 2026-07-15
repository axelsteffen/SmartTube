package com.liskovsoft.plexserviceinterfaces;

import com.liskovsoft.plexserviceinterfaces.data.PlexAuthPin;

import io.reactivex.Observable;

/**
 * Plex authentication via PIN flow or stored auth token.
 */
public interface PlexSignInService {
    boolean isSigned();

    String getAuthToken();

    void setAuthToken(String token);

    void signOut();

    /**
     * Starts PIN auth: emits pin/code for the user to enter at plex.tv/link, then completes
     * when the PIN is claimed and a token is available.
     */
    Observable<PlexAuthPin> signInWithPinObserve();
}
