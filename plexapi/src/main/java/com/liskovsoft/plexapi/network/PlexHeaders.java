package com.liskovsoft.plexapi.network;

import com.liskovsoft.plexapi.prefs.PlexPrefs;

/**
 * Standard X-Plex-* headers required by plex.tv and PMS.
 */
public final class PlexHeaders {
    public static final String CLIENT_IDENTIFIER = "X-Plex-Client-Identifier";
    public static final String PRODUCT = "X-Plex-Product";
    public static final String VERSION = "X-Plex-Version";
    public static final String DEVICE = "X-Plex-Device";
    public static final String PLATFORM = "X-Plex-Platform";
    public static final String TOKEN = "X-Plex-Token";
    public static final String ACCEPT = "Accept";
    public static final String CONTAINER_START = "X-Plex-Container-Start";
    public static final String CONTAINER_SIZE = "X-Plex-Container-Size";

    public static final String PRODUCT_VALUE = "SmartTube";
    public static final String VERSION_VALUE = "0.1.0";
    public static final String DEVICE_VALUE = "Android TV";
    public static final String PLATFORM_VALUE = "Android";
    public static final String ACCEPT_JSON = "application/json";

    private PlexHeaders() {
    }

    public static String clientIdentifier() {
        return PlexPrefs.instance().getClientIdentifier();
    }
}
