package com.liskovsoft.plexapi.prefs;

import android.annotation.SuppressLint;
import android.content.Context;

import androidx.annotation.Nullable;

import com.liskovsoft.sharedutils.prefs.GlobalPreferences;
import com.liskovsoft.sharedutils.prefs.SharedPreferencesBase;

import java.util.UUID;

/**
 * Persistent Plex auth token and stable client identifier.
 */
public final class PlexPrefs extends SharedPreferencesBase {
    private static final String SHARED_PREFERENCES_NAME = PlexPrefs.class.getName();
    private static final String AUTH_TOKEN = "plex_auth_token";
    private static final String CLIENT_IDENTIFIER = "plex_client_identifier";

    @SuppressLint("StaticFieldLeak")
    private static PlexPrefs sInstance;

    private PlexPrefs(Context context) {
        super(context, SHARED_PREFERENCES_NAME);
    }

    public static synchronized PlexPrefs instance() {
        if (sInstance == null) {
            Context context = GlobalPreferences.context();
            if (context == null) {
                throw new IllegalStateException("PlexPrefs requires GlobalPreferences (or instance(Context)) first");
            }
            sInstance = new PlexPrefs(context);
        }
        return sInstance;
    }

    public static synchronized PlexPrefs instance(Context context) {
        if (sInstance == null) {
            sInstance = new PlexPrefs(context.getApplicationContext());
        }
        return sInstance;
    }

    /** Clears singleton — for unit tests only. */
    public static synchronized void unhold() {
        sInstance = null;
    }

    @Nullable
    public String getAuthToken() {
        String token = getString(AUTH_TOKEN, null);
        return token != null && !token.isEmpty() ? token : null;
    }

    public void setAuthToken(@Nullable String token) {
        putString(AUTH_TOKEN, token != null ? token : "");
    }

    public void clearAuthToken() {
        setAuthToken(null);
    }

    /**
     * Stable UUID used as {@code X-Plex-Client-Identifier}. Created once and persisted.
     */
    public String getClientIdentifier() {
        String id = getString(CLIENT_IDENTIFIER, null);
        if (id == null || id.isEmpty()) {
            id = UUID.randomUUID().toString();
            putString(CLIENT_IDENTIFIER, id);
        }
        return id;
    }
}
