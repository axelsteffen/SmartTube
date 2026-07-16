package com.liskovsoft.smartyoutubetv2.common.misc;

import android.content.Context;

import com.liskovsoft.mediaserviceinterfaces.data.MediaGroup;
import com.liskovsoft.plexapi.prefs.PlexPrefs;
import com.liskovsoft.smartyoutubetv2.common.R;
import com.liskovsoft.smartyoutubetv2.common.app.models.data.BrowseSection;
import com.liskovsoft.smartyoutubetv2.common.app.models.errors.PlexDisabledError;
import com.liskovsoft.smartyoutubetv2.common.app.models.errors.PlexSignInError;

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

    /**
     * Inserts missing fork sections after Home ({@link MediaGroup#TYPE_HOME}).
     * Does not move a section that is already present (user may have reordered it).
     */
    public static void appendExtraSections(List<BrowseSection> sections, Context context) {
        for (BrowseSection section : getExtraSections(context)) {
            if (!containsSection(sections, section.getId())) {
                insertAfterHome(sections, section);
            }
        }
    }

    private static void insertAfterHome(List<BrowseSection> sections, BrowseSection section) {
        int homeIndex = indexOfSection(sections, MediaGroup.TYPE_HOME);
        if (homeIndex >= 0) {
            sections.add(homeIndex + 1, section);
        } else if (sections.isEmpty()) {
            sections.add(section);
        } else {
            sections.add(0, section);
        }
    }

    private static int indexOfSection(List<BrowseSection> sections, int id) {
        for (int i = 0; i < sections.size(); i++) {
            if (sections.get(i).getId() == id) {
                return i;
            }
        }
        return -1;
    }

    public static boolean isExtraSection(int sectionId) {
        return sectionId >= TYPE_PLEX;
    }

    public static List<BrowseSection> getExtraSections(Context context) {
        List<BrowseSection> result = new ArrayList<>();

        if (!MediaSourceRegistry.isPlexEnabled()) {
            result.add(createPlexDisabledSection(context));
            return result;
        }

        if (isPlexReady(context)) {
            result.add(createPlexBrowseSection(context));
        } else {
            result.add(createPlexSignInSection(context));
        }

        return result;
    }

    /** Auth token + selected server present — ready for library rows (Phase 3.2). */
    public static boolean isPlexReady(Context context) {
        PlexPrefs prefs = PlexPrefs.instance(context);
        return prefs.getAuthToken() != null && prefs.getSelectedServer() != null;
    }

    private static BrowseSection createPlexBrowseSection(Context context) {
        return new BrowseSection(
                TYPE_PLEX,
                context.getString(R.string.header_plex),
                BrowseSection.TYPE_ROW,
                R.drawable.icon_playlist,
                false
        );
    }

    private static BrowseSection createPlexSignInSection(Context context) {
        return new BrowseSection(
                TYPE_PLEX,
                context.getString(R.string.header_plex),
                BrowseSection.TYPE_ERROR,
                R.drawable.icon_playlist,
                false,
                new PlexSignInError(context)
        );
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
