package com.liskovsoft.plexapi.library;

import com.liskovsoft.plexserviceinterfaces.data.PlexMediaItem;
import com.liskovsoft.plexserviceinterfaces.data.PlexMediaPage;

import java.util.Collections;
import java.util.List;

/** One page of Plex metadata items plus container paging fields. */
public final class PlexPage implements PlexMediaPage {
    private final List<PlexMediaItem> mItems;
    private final int mOffset;
    private final int mTotalSize;

    public PlexPage(List<PlexMediaItem> items, int offset, int totalSize) {
        mItems = items != null ? items : Collections.emptyList();
        mOffset = Math.max(offset, 0);
        mTotalSize = Math.max(totalSize, 0);
    }

    public List<PlexMediaItem> getItems() {
        return mItems;
    }

    @Override
    public int getOffset() {
        return mOffset;
    }

    @Override
    public int getTotalSize() {
        return mTotalSize;
    }

    public int getNextOffset() {
        int next = mOffset + mItems.size();
        return next < mTotalSize ? next : -1;
    }
}
