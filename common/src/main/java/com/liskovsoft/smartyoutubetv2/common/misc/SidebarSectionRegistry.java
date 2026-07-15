package com.liskovsoft.smartyoutubetv2.common.misc;

import android.content.Context;

import com.liskovsoft.smartyoutubetv2.common.R;
import com.liskovsoft.smartyoutubetv2.common.app.models.data.BrowseSection;
import com.liskovsoft.smartyoutubetv2.common.app.models.errors.PlexDisabledError;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Fork-only extension point for sidebar sections beyond upstream YouTube categories.
 * Reserved section ids start at {@link #TYPE_PLEX} (100).
 */
public final class SidebarSectionRegistry {
    public static final int TYPE_PLEX = 100;

    private SidebarSectionRegistry() {
    }

    public static void registerSectionMappings(Map<Integer, BrowseSection> mappings, Context context) {
        for (BrowseSection section : getExtraSections(context)) {
            mappings.put(section.getId(), section);
        }
    }

    public static void appendExtraSections(List<BrowseSection> sections, Context context) {
        for (BrowseSection section : getExtraSections(context)) {
            if (!containsSection(sections, section.getId())) {
                sections.add(section);
            }
        }
    }

    public static boolean isExtraSection(int sectionId) {
        return sectionId >= TYPE_PLEX;
    }

    public static List<BrowseSection> getExtraSections(Context context) {
        List<BrowseSection> result = new ArrayList<>();

        if (!MediaSourceRegistry.isPlexEnabled()) {
            result.add(createPlexDisabledSection(context));
        }

        return result;
    }

    private static BrowseSection createPlexDisabledSection(Context context) {
        return new BrowseSection(
                TYPE_PLEX,
                context.getString(R.string.header_plex),
                BrowseSection.TYPE_ERROR,
                R.drawable.icon_playlist,
                false,
                new PlexDisabledError(context)
        );
    }

    private static boolean containsSection(List<BrowseSection> sections, int id) {
        for (BrowseSection section : sections) {
            if (section.getId() == id) {
                return true;
            }
        }

        return false;
    }
}
