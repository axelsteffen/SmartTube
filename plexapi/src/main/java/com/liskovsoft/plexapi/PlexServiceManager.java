package com.liskovsoft.plexapi;

import android.content.Context;

import com.liskovsoft.plexapi.prefs.PlexPrefs;
import com.liskovsoft.plexapi.service.PlexLibraryServiceImpl;
import com.liskovsoft.plexapi.service.PlexMediaServiceImpl;
import com.liskovsoft.plexapi.service.PlexServerServiceImpl;
import com.liskovsoft.plexapi.service.PlexSignInServiceImpl;
import com.liskovsoft.plexserviceinterfaces.PlexLibraryService;
import com.liskovsoft.plexserviceinterfaces.PlexMediaService;
import com.liskovsoft.plexserviceinterfaces.PlexServerService;
import com.liskovsoft.plexserviceinterfaces.PlexSignInService;
import com.liskovsoft.sharedutils.mylogger.Log;

/**
 * Fork-only entry point for Plex services.
 * Sign-in, server discovery, library listing, and stream resolve are live.
 */
public final class PlexServiceManager implements com.liskovsoft.plexserviceinterfaces.PlexServiceManager {
    private static final String TAG = PlexServiceManager.class.getSimpleName();
    private static PlexServiceManager sInstance;

    private final PlexSignInService mSignInService;
    private final PlexServerService mServerService;
    private final PlexLibraryService mLibraryService;
    private final PlexMediaService mMediaService;

    private PlexServiceManager() {
        Log.d(TAG, "Starting...");
        mSignInService = new PlexSignInServiceImpl();
        mServerService = new PlexServerServiceImpl();
        mLibraryService = new PlexLibraryServiceImpl();
        mMediaService = new PlexMediaServiceImpl();
    }

    /**
     * Optional early init so {@link PlexPrefs} has a Context before the first plex.tv call
     * (also covered once {@code GlobalPreferences} is ready).
     */
    public static void init(Context context) {
        if (context != null) {
            PlexPrefs.instance(context);
        }
    }

    public static com.liskovsoft.plexserviceinterfaces.PlexServiceManager instance() {
        if (sInstance == null) {
            sInstance = new PlexServiceManager();
        }
        return sInstance;
    }

    @Override
    public PlexSignInService getSignInService() {
        return mSignInService;
    }

    @Override
    public PlexServerService getServerService() {
        return mServerService;
    }

    @Override
    public PlexLibraryService getLibraryService() {
        return mLibraryService;
    }

    @Override
    public PlexMediaService getMediaService() {
        return mMediaService;
    }
}
