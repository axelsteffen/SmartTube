package com.liskovsoft.leanbackassistant.misc;

import com.liskovsoft.mediaserviceinterfaces.ServiceManager;
import com.liskovsoft.youtubeapi.service.YouTubeServiceManager;

/**
 * Holds the app {@link ServiceManager} so leanbackassistant can avoid depending on
 * {@code common}'s {@code MediaSourceRegistry} (circular Gradle dep).
 * <p>
 * Call {@link #init(ServiceManager)} from app startup ({@code SplashPresenter}).
 * Until then, {@link #get()} falls back to {@link YouTubeServiceManager}.
 */
public final class ServiceManagerProvider {
    private static ServiceManager sServiceManager;

    private ServiceManagerProvider() {
    }

    public static void init(ServiceManager serviceManager) {
        if (serviceManager != null) {
            sServiceManager = serviceManager;
        }
    }

    public static ServiceManager get() {
        return sServiceManager != null ? sServiceManager : YouTubeServiceManager.instance();
    }
}
