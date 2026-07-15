package com.liskovsoft.plexapi.service;

import com.liskovsoft.plexapi.adapter.PlexMediaSubtitle;
import com.liskovsoft.plexapi.media.PlexAudioTrackImpl;
import com.liskovsoft.plexapi.media.PlexStreamInfoImpl;
import com.liskovsoft.plexapi.media.PlexSubtitleImpl;
import com.liskovsoft.plexapi.network.PlexPmsApi;
import com.liskovsoft.plexapi.network.PlexRetrofitHelper;
import com.liskovsoft.plexapi.network.PlexUrlHelper;
import com.liskovsoft.plexapi.network.dto.MediaContainer;
import com.liskovsoft.plexapi.network.dto.MediaContainerResponse;
import com.liskovsoft.plexapi.network.dto.PlexMedia;
import com.liskovsoft.plexapi.network.dto.PlexMetadata;
import com.liskovsoft.plexapi.network.dto.PlexPart;
import com.liskovsoft.plexapi.network.dto.PlexStream;
import com.liskovsoft.plexapi.prefs.PlexPrefs;
import com.liskovsoft.plexserviceinterfaces.PlexMediaService;
import com.liskovsoft.plexserviceinterfaces.data.PlexAudioTrack;
import com.liskovsoft.plexserviceinterfaces.data.PlexMediaItem;
import com.liskovsoft.plexserviceinterfaces.data.PlexServer;
import com.liskovsoft.plexserviceinterfaces.data.PlexStreamInfo;
import com.liskovsoft.plexserviceinterfaces.data.PlexSubtitle;
import com.liskovsoft.sharedutils.mylogger.Log;
import com.liskovsoft.sharedutils.rx.RxHelper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import io.reactivex.Observable;
import retrofit2.Response;

/**
 * Resolves playable stream URLs, external subtitles, audio track preference,
 * progress sync, and forced transcode fallback (Phase 1.6 / 4.1–4.5).
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
        return getStreamInfoObserve(item, null, null, false);
    }

    @Override
    public Observable<PlexStreamInfo> getStreamInfoObserve(
            PlexMediaItem item, Long audioStreamId, String preferredLanguage) {
        return getStreamInfoObserve(item, audioStreamId, preferredLanguage, false);
    }

    @Override
    public Observable<PlexStreamInfo> getStreamInfoObserve(
            PlexMediaItem item,
            Long audioStreamId,
            String preferredLanguage,
            boolean forceTranscode) {
        return Observable.fromCallable(
                () -> fetchStreamInfo(item, audioStreamId, preferredLanguage, forceTranscode));
    }

    @Override
    public Observable<Void> updateProgressObserve(
            PlexMediaItem item, long positionMs, long durationMs, String state) {
        return RxHelper.fromRunnable(() -> {
            try {
                reportTimeline(item, positionMs, durationMs, state);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private PlexStreamInfo fetchStreamInfo(
            PlexMediaItem item,
            Long audioStreamId,
            String preferredLanguage,
            boolean forceTranscode) throws IOException {
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

        long viewOffsetMs = firstViewOffset(metaContainer);
        List<PlexSubtitle> subtitles = collectExternalSubtitles(metaContainer, baseUrl, token);
        List<PlexAudioTrack> audioTracks = collectAudioTracks(metaContainer);
        long selectedAudioId = pickAudioStreamId(audioTracks, audioStreamId, preferredLanguage);

        if (!forceTranscode) {
            PlexPart directPart = firstPartWithKey(metaContainer);
            if (directPart != null) {
                String url = PlexUrlHelper.absoluteUrl(baseUrl, directPart.getKey(), token);
                String container = directPart.getContainer();
                if (container == null || container.isEmpty()) {
                    container = firstMediaContainer(metaContainer);
                }
                Log.d(TAG, "Direct Play for ratingKey=" + item.getRatingKey()
                        + " subtitles=" + subtitles.size()
                        + " audioTracks=" + audioTracks.size()
                        + " preferredAudio=" + selectedAudioId);
                return new PlexStreamInfoImpl(
                        url, PlexStreamInfoImpl.mimeHint(container), false, viewOffsetMs,
                        subtitles, audioTracks, selectedAudioId);
            }
            Log.d(TAG, "No Part.key — trying decision for ratingKey=" + item.getRatingKey()
                    + " audioStreamID=" + selectedAudioId);
        } else {
            Log.d(TAG, "Force transcode for ratingKey=" + item.getRatingKey()
                    + " audioStreamID=" + selectedAudioId);
        }

        return resolveViaDecision(
                api, item.getRatingKey(), baseUrl, token, viewOffsetMs,
                subtitles, audioTracks, selectedAudioId, forceTranscode);
    }

    private void reportTimeline(PlexMediaItem item, long positionMs, long durationMs, String state)
            throws IOException {
        if (item == null || item.getRatingKey() == null || item.getRatingKey().isEmpty()) {
            throw new IllegalArgumentException("item with ratingKey required");
        }
        String timelineState = state != null && !state.isEmpty() ? state : STATE_STOPPED;
        long time = Math.max(0L, positionMs);
        long duration = Math.max(0L, durationMs);
        if (duration <= 0L && item.getDurationMs() > 0L) {
            duration = item.getDurationMs();
        }

        String key = item.getKey();
        if (key == null || key.isEmpty()) {
            key = "/library/metadata/" + item.getRatingKey();
        }

        PlexServer server = requireSelectedServer();
        PlexPmsApi api = pmsApi(server);
        String token = pmsToken(server);
        String clientId = prefs().getClientIdentifier();

        Response<Void> response = api.reportTimeline(
                item.getRatingKey(),
                key,
                timelineState,
                time,
                duration,
                token,
                clientId).execute();

        if (!response.isSuccessful()) {
            throw new IOException("Failed to report timeline for " + item.getRatingKey()
                    + ": HTTP " + response.code());
        }
        Log.d(TAG, "Timeline " + timelineState + " ratingKey=" + item.getRatingKey()
                + " time=" + time + " duration=" + duration);
    }

    private PlexStreamInfo resolveViaDecision(
            PlexPmsApi api,
            String ratingKey,
            String baseUrl,
            String token,
            long viewOffsetMs,
            List<PlexSubtitle> subtitles,
            List<PlexAudioTrack> audioTracks,
            long selectedAudioId,
            boolean forceTranscode) throws IOException {
        String path = "/library/metadata/" + ratingKey;
        Long audioQuery = selectedAudioId > 0L ? selectedAudioId : null;
        int directPlay = forceTranscode ? 0 : 1;
        int directStream = forceTranscode ? 0 : 1;
        Response<MediaContainerResponse> decisionResponse = api.getPlaybackDecision(
                path, directPlay, directStream, "hls", 0, 0, audioQuery, token).execute();
        MediaContainer decisionContainer =
                requireContainer(decisionResponse, "playback decision for " + ratingKey);

        PlexPart part = firstPartWithKey(decisionContainer);
        if (part == null) {
            throw new IOException("Playback decision returned no Part.key for " + ratingKey);
        }

        boolean transcoded = forceTranscode || isTranscodeDecision(part.getDecision());
        String url = PlexUrlHelper.absoluteUrl(baseUrl, part.getKey(), token);
        String hintSource = part.getProtocol();
        if (hintSource == null || hintSource.isEmpty()) {
            hintSource = part.getContainer();
        }
        if (transcoded && (hintSource == null || hintSource.isEmpty())) {
            hintSource = "hls";
        }

        Log.d(TAG, "Decision stream for ratingKey=" + ratingKey
                + " transcoded=" + transcoded + " force=" + forceTranscode
                + " decision=" + part.getDecision()
                + " subtitles=" + subtitles.size()
                + " audioStreamID=" + selectedAudioId);
        return new PlexStreamInfoImpl(
                url, PlexStreamInfoImpl.mimeHint(hintSource), transcoded, viewOffsetMs,
                subtitles, audioTracks, selectedAudioId);
    }

    /**
     * Collects external subtitle streams ({@code streamType=3} with a {@code key}).
     * Embedded tracks without a key are left to the container extractor.
     */
    static List<PlexSubtitle> collectExternalSubtitles(
            MediaContainer container, String baseUrl, String token) {
        if (container == null) {
            return Collections.emptyList();
        }
        List<PlexSubtitle> result = new ArrayList<>();
        for (PlexMetadata metadata : container.getMetadata()) {
            for (PlexMedia media : metadata.getMedia()) {
                for (PlexPart part : media.getParts()) {
                    for (PlexStream stream : part.getStreams()) {
                        if (stream.getStreamType() != PlexStream.TYPE_SUBTITLE) {
                            continue;
                        }
                        String key = stream.getKey();
                        if (key == null || key.isEmpty()) {
                            continue;
                        }
                        if (PlexMediaSubtitle.mimeFromCodec(stream.getCodec()) == null) {
                            continue;
                        }
                        String url = PlexUrlHelper.absoluteUrl(baseUrl, key, token);
                        if (url == null || url.isEmpty()) {
                            continue;
                        }
                        String languageCode = firstNonEmpty(
                                stream.getLanguageCode(),
                                stream.getLanguageTag(),
                                stream.getLanguage());
                        String name = firstNonEmpty(
                                stream.getDisplayTitle(),
                                stream.getTitle(),
                                languageCode,
                                stream.getCodec());
                        result.add(new PlexSubtitleImpl(
                                url, languageCode, name, stream.getCodec()));
                    }
                }
            }
        }
        return result.isEmpty() ? Collections.emptyList() : result;
    }

    /** Collects audio streams ({@code streamType=2}). */
    static List<PlexAudioTrack> collectAudioTracks(MediaContainer container) {
        if (container == null) {
            return Collections.emptyList();
        }
        List<PlexAudioTrack> result = new ArrayList<>();
        for (PlexMetadata metadata : container.getMetadata()) {
            for (PlexMedia media : metadata.getMedia()) {
                for (PlexPart part : media.getParts()) {
                    for (PlexStream stream : part.getStreams()) {
                        if (stream.getStreamType() != PlexStream.TYPE_AUDIO) {
                            continue;
                        }
                        if (stream.getId() <= 0L) {
                            continue;
                        }
                        String languageCode = firstNonEmpty(
                                stream.getLanguageCode(),
                                stream.getLanguageTag(),
                                stream.getLanguage());
                        String name = firstNonEmpty(
                                stream.getDisplayTitle(),
                                stream.getTitle(),
                                languageCode,
                                stream.getCodec());
                        result.add(new PlexAudioTrackImpl(
                                stream.getId(),
                                languageCode,
                                name,
                                stream.getCodec(),
                                stream.getChannels(),
                                stream.isSelected()));
                    }
                }
            }
        }
        return result.isEmpty() ? Collections.emptyList() : result;
    }

    /**
     * Picks audio stream id: explicit override → preferred language → PMS selected → first.
     */
    static long pickAudioStreamId(
            List<PlexAudioTrack> tracks, Long overrideId, String preferredLanguage) {
        if (tracks == null || tracks.isEmpty()) {
            return 0L;
        }
        if (overrideId != null && overrideId > 0L) {
            for (PlexAudioTrack track : tracks) {
                if (track.getId() == overrideId) {
                    return overrideId;
                }
            }
        }
        if (preferredLanguage != null && !preferredLanguage.isEmpty()) {
            String pref = preferredLanguage.toLowerCase(Locale.US);
            for (PlexAudioTrack track : tracks) {
                if (languageMatches(track.getLanguageCode(), pref)) {
                    return track.getId();
                }
            }
        }
        for (PlexAudioTrack track : tracks) {
            if (track.isSelected()) {
                return track.getId();
            }
        }
        return tracks.get(0).getId();
    }

    static boolean languageMatches(String languageCode, String preferredLower) {
        if (languageCode == null || languageCode.isEmpty() || preferredLower == null) {
            return false;
        }
        String code = languageCode.toLowerCase(Locale.US);
        return code.equals(preferredLower)
                || code.startsWith(preferredLower + "-")
                || preferredLower.startsWith(code + "-");
    }

    private static String firstNonEmpty(String... values) {
        if (values == null) {
            return null;
        }
        for (String value : values) {
            if (value != null && !value.isEmpty()) {
                return value;
            }
        }
        return null;
    }

    private static long firstViewOffset(MediaContainer container) {
        for (PlexMetadata metadata : container.getMetadata()) {
            if (metadata.getViewOffset() > 0L) {
                return metadata.getViewOffset();
            }
        }
        return 0L;
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
