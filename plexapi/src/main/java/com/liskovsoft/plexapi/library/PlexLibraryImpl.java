package com.liskovsoft.plexapi.library;

import com.liskovsoft.plexapi.network.dto.PlexDirectory;
import com.liskovsoft.plexserviceinterfaces.data.PlexLibrary;

/**
 * Immutable {@link PlexLibrary} from a PMS section {@link PlexDirectory}.
 */
public final class PlexLibraryImpl implements PlexLibrary {
    private final String mKey;
    private final String mTitle;
    private final String mType;

    public PlexLibraryImpl(String key, String title, String type) {
        mKey = key;
        mTitle = title;
        mType = type;
    }

    public static PlexLibraryImpl fromDirectory(PlexDirectory directory) {
        if (directory == null || directory.getKey() == null || directory.getKey().isEmpty()) {
            return null;
        }
        return new PlexLibraryImpl(directory.getKey(), directory.getTitle(), directory.getType());
    }

    @Override
    public String getKey() {
        return mKey;
    }

    @Override
    public String getTitle() {
        return mTitle;
    }

    @Override
    public String getType() {
        return mType;
    }
}
