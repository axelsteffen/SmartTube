package com.liskovsoft.plexapi.prefs;

import android.annotation.SuppressLint;
import android.content.Context;

import androidx.annotation.Nullable;

import com.liskovsoft.plexapi.server.PlexServerImpl;
import com.liskovsoft.plexserviceinterfaces.data.PlexServer;
import com.liskovsoft.sharedutils.prefs.GlobalPreferences;
import com.liskovsoft.sharedutils.prefs.SharedPreferencesBase;

import java.util.UUID;

/**
 * Persistent Plex auth token, selected server, and stable client identifier.
 */
public final class PlexPrefs extends SharedPreferencesBase {
    private static final String SHARED_PREFERENCES_NAME = PlexPrefs.class.getName();
    private static final String AUTH_TOKEN = "plex_auth_token";
    private static final String CLIENT_IDENTIFIER = "plex_client_identifier";
    private static final String SELECTED_SERVER_ID = "plex_selected_server_id";
    private static final String SELECTED_SERVER_NAME = "plex_selected_server_name";
    private static final String SELECTED_SERVER_BASE_URL = "plex_selected_server_base_url";
    private static final String SELECTED_SERVER_ACCESS_TOKEN = "plex_selected_server_access_token";
    private static final String SELECTED_SERVER_OWNED = "plex_selected_server_owned";

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

    public void setSelectedServer(@Nullable PlexServer server) {
        if (server == null) {
            clearSelectedServer();
            return;
        }
        putString(SELECTED_SERVER_ID, nullToEmpty(server.getClientIdentifier()));
        putString(SELECTED_SERVER_NAME, nullToEmpty(server.getName()));
        putString(SELECTED_SERVER_BASE_URL, nullToEmpty(server.getBaseUrl()));
        putString(SELECTED_SERVER_ACCESS_TOKEN, nullToEmpty(server.getAccessToken()));
        putBoolean(SELECTED_SERVER_OWNED, server.isOwned());
    }

    @Nullable
    public PlexServer getSelectedServer() {
        String baseUrl = getString(SELECTED_SERVER_BASE_URL, null);
        if (baseUrl == null || baseUrl.isEmpty()) {
            return null;
        }
        String id = getString(SELECTED_SERVER_ID, null);
        String name = getString(SELECTED_SERVER_NAME, null);
        String accessToken = getString(SELECTED_SERVER_ACCESS_TOKEN, null);
        boolean owned = getBoolean(SELECTED_SERVER_OWNED, false);
        // Presence is ephemeral; treat persisted selection as online until rediscovered.
        return new PlexServerImpl(id, name, baseUrl, emptyToNull(accessToken), owned, true);
    }

    public void clearSelectedServer() {
        putString(SELECTED_SERVER_ID, "");
        putString(SELECTED_SERVER_NAME, "");
        putString(SELECTED_SERVER_BASE_URL, "");
        putString(SELECTED_SERVER_ACCESS_TOKEN, "");
        putBoolean(SELECTED_SERVER_OWNED, false);
    }

    private static String nullToEmpty(@Nullable String value) {
        return value != null ? value : "";
    }

    @Nullable
    private static String emptyToNull(@Nullable String value) {
        return value != null && !value.isEmpty() ? value : null;
    }
}
