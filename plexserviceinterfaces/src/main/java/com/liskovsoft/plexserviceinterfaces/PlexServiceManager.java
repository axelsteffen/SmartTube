package com.liskovsoft.plexserviceinterfaces;

/**
 * Fork-only facade for Plex services (analogous to mediaserviceinterfaces.ServiceManager).
 */
public interface PlexServiceManager {
    PlexSignInService getSignInService();
    PlexServerService getServerService();
    PlexLibraryService getLibraryService();
    PlexMediaService getMediaService();
}
