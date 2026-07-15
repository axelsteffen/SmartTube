package com.liskovsoft.plexapi.service;

import com.liskovsoft.plexapi.media.PlexStreamInfoImpl;
import com.liskovsoft.plexapi.network.PlexPmsApi;
import com.liskovsoft.plexapi.network.PlexRetrofitHelper;
import com.liskovsoft.plexapi.network.PlexUrlHelper;
import com.liskovsoft.plexapi.network.dto.MediaContainer;
import com.liskovsoft.plexapi.network.dto.MediaContainerResponse;
import com.liskovsoft.plexapi.network.dto.PlexMedia;
import com.liskovsoft.plexapi.network.dto.PlexMetadata;
import com.liskovsoft.plexapi.network.dto.PlexPart;
import com.liskovsoft.plexapi.prefs.PlexPrefs;
import com.liskovsoft.plexserviceinterfaces.PlexMediaService;
import com.liskovsoft.plexserviceinterfaces.data.PlexMediaItem;
import com.liskovsoft.plexserviceinterfaces.data.PlexServer;
import com.liskovsoft.plexserviceinterfaces.data.PlexStreamInfo;
import com.liskovsoft.sharedutils.mylogger.Log;

import java.io.IOException;

import io.reactivex.Observable;
import retrofit2.Response;

/**
 * Resolves playable stream URLs for a Plex media item (Phase 1.6).
 * Prefers Direct Play from metadata {@code Part.key}; falls back to HLS decision.
 */
public class PlexMediaServiceImpl implements PlexMediaService {
    private static final String TAG = PlexMediaServiceImpl.class.getSimpleName();

    private final PlexPrefs mPrefs;
    private final PlexPmsApi mApi;

    public PlexMediaServiceImpl() {
        this(null, null);
    }

    /** Package-visible for tests. */
    PlexMediaServiceImpl(PlexPrefs prefs, PlexPmsApi api) {
        mPrefs = prefs;
        mApi = api;
    }

    private PlexPrefs prefs() {
        return mPrefs != null ? mPrefs : PlexPrefs.instance();
    }

    @Override
    public Observable<PlexStreamInfo> getStreamInfoObserve(PlexMediaItem item) {
        return Observable.fromCallable(() -> fetchStreamInfo(item));
    }

    private PlexStreamInfo fetchStreamInfo(PlexMediaItem item) throws IOException {
        if (item == null || item.getRatingKey() == null || item.getRatingKey().isEmpty()) {
            throw new IllegalArgumentException("item with ratingKey required");
        }

        PlexServer server = requireSelectedServer();
        PlexPmsApi api = pmsApi(server);
        String token = pmsToken(server);
        String baseUrl = server.getBaseUrl();

        Response<MediaContainerResponse> metaResponse =
                api.getMetadata(item.getRatingKey(), token).execute();
        MediaContainer metaContainer = requireContainer(metaResponse, "fetch metadata " + item.getRatingKey());

        PlexPart directPart = firstPartWithKey(metaContainer);
        if (directPart != null) {
            String url = PlexUrlHelper.absoluteUrl(baseUrl, directPart.getKey(), token);
            String container = directPart.getContainer();
            if (container == null || container.isEmpty()) {
                container = firstMediaContainer(metaContainer);
            }
            Log.d(TAG, "Direct Play for ratingKey=" + item.getRatingKey());
            return new PlexStreamInfoImpl(url, PlexStreamInfoImpl.mimeHint(container), false);
        }

        Log.d(TAG, "No Part.key — trying decision for ratingKey=" + item.getRatingKey());
        return resolveViaDecision(api, item.getRatingKey(), baseUrl, token);
    }

    private PlexStreamInfo resolveViaDecision(PlexPmsApi api, String ratingKey,
                                              String baseUrl, String token) throws IOException {
        String path = "/library/metadata/" + ratingKey;
        Response<MediaContainerResponse> decisionResponse = api.getPlaybackDecision(
                path, 1, 1, "hls", 0, 0, token).execute();
        MediaContainer decisionContainer =
                requireContainer(decisionResponse, "playback decision for " + ratingKey);

        PlexPart part = firstPartWithKey(decisionContainer);
        if (part == null) {
            throw new IOException("Playback decision returned no Part.key for " + ratingKey);
        }

        boolean transcoded = isTranscodeDecision(part.getDecision());
        String url = PlexUrlHelper.absoluteUrl(baseUrl, part.getKey(), token);
        String hintSource = part.getProtocol();
        if (hintSource == null || hintSource.isEmpty()) {
            hintSource = part.getContainer();
        }
        if (transcoded && (hintSource == null || hintSource.isEmpty())) {
            hintSource = "hls";
        }

        Log.d(TAG, "Decision stream for ratingKey=" + ratingKey
                + " transcoded=" + transcoded + " decision=" + part.getDecision());
        return new PlexStreamInfoImpl(url, PlexStreamInfoImpl.mimeHint(hintSource), transcoded);
    }

    private static PlexPart firstPartWithKey(MediaContainer container) {
        for (PlexMetadata metadata : container.getMetadata()) {
            for (PlexMedia media : metadata.getMedia()) {
                for (PlexPart part : media.getParts()) {
                    if (part.getKey() != null && !part.getKey().isEmpty()) {
                        return part;
                    }
                }
            }
        }
        return null;
    }

    private static String firstMediaContainer(MediaContainer container) {
        for (PlexMetadata metadata : container.getMetadata()) {
            for (PlexMedia media : metadata.getMedia()) {
                if (media.getContainer() != null && !media.getContainer().isEmpty()) {
                    return media.getContainer();
                }
            }
        }
        return null;
    }

    static boolean isTranscodeDecision(String decision) {
        if (decision == null || decision.isEmpty()) {
            return true;
        }
        return !"directplay".equalsIgnoreCase(decision)
                && !"directstream".equalsIgnoreCase(decision);
    }

    private PlexServer requireSelectedServer() {
        PlexServer server = prefs().getSelectedServer();
        if (server == null || server.getBaseUrl() == null || server.getBaseUrl().isEmpty()) {
            throw new IllegalStateException("No Plex server selected (Phase 1.4)");
        }
        return server;
    }

    private PlexPmsApi pmsApi(PlexServer server) {
        if (mApi != null) {
            return mApi;
        }
        return PlexRetrofitHelper.createPmsApi(server.getBaseUrl(), PlexPmsApi.class);
    }

    private String pmsToken(PlexServer server) {
        String serverToken = server.getAccessToken();
        if (serverToken != null && !serverToken.isEmpty()) {
            return serverToken;
        }
        String accountToken = prefs().getAuthToken();
        if (accountToken != null && !accountToken.isEmpty()) {
            return accountToken;
        }
        throw new IllegalStateException("No Plex auth token available for PMS calls");
    }

    private static MediaContainer requireContainer(Response<MediaContainerResponse> response,
                                                   String action) throws IOException {
        if (!response.isSuccessful()) {
            throw new IOException("Failed to " + action + ": HTTP " + response.code());
        }
        MediaContainerResponse body = response.body();
        if (body == null || body.getMediaContainer() == null) {
            throw new IOException("Failed to " + action + ": empty MediaContainer");
        }
        return body.getMediaContainer();
    }
}
