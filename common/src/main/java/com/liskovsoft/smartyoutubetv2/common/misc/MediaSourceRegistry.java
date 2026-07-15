package com.liskovsoft.smartyoutubetv2.common.misc;

import com.liskovsoft.mediaserviceinterfaces.ServiceManager;
import com.liskovsoft.youtubeapi.service.YouTubeServiceManager;

/**
 * Central registry for media sources (YouTube, Plex).
 * Fork-only; use {@link #getServiceManager()} instead of direct YouTubeServiceManager access.
 */
public final class MediaSourceRegistry {
    public enum Source {
        YOUTUBE,
        PLEX
    }

    private static Source sActiveSource = Source.YOUTUBE;

    private MediaSourceRegistry() {
    }

    public static Source getActiveSource() {
        return sActiveSource;
    }

    public static void setActiveSource(Source source) {
        if (source != null) {
            sActiveSource = source;
        }
    }

    public static ServiceManager getServiceManager() {
        if (sActiveSource == Source.PLEX && isPlexEnabled()) {
            // PlexServiceManager.instance() — Phase 1
        }
        return YouTubeServiceManager.instance();
    }

    /**
     * Feature flag for Plex integration. Enabled from Phase 3.1 (Browse UI).
     */
    public static boolean isPlexEnabled() {
        return true;
    }
}
