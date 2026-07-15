package com.liskovsoft.plexapi.network;

import com.liskovsoft.plexapi.network.dto.MediaContainerResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Plex Media Server endpoints used by the Phase 1 PoC.
 * Base URL = selected server connection URI (trailing slash).
 *
 * @see plexapi/openapi-plex-pms-in-use.yaml
 */
public interface PlexPmsApi {
    /** Metadata type number for movies (PMS API Info → Types). */
    int TYPE_MOVIE = 1;
    /** Metadata type number for TV shows. */
    int TYPE_SHOW = 2;

    @GET("identity")
    Call<MediaContainerResponse> getIdentity();

    @GET("/")
    Call<MediaContainerResponse> getServerInfo();

    @GET("library/sections")
    Call<MediaContainerResponse> getLibrarySections(
            @Header(PlexHeaders.TOKEN) String token);

    @GET("library/sections/{sectionId}/all")
    Call<MediaContainerResponse> getSectionItems(
            @Path("sectionId") String sectionId,
            @Query("type") Integer type,
            @Header(PlexHeaders.CONTAINER_START) Integer containerStart,
            @Header(PlexHeaders.CONTAINER_SIZE) Integer containerSize,
            @Header(PlexHeaders.TOKEN) String token);

    @GET("library/metadata/{ids}")
    Call<MediaContainerResponse> getMetadata(
            @Path("ids") String ids,
            @Header(PlexHeaders.TOKEN) String token);

    @GET("library/metadata/{ratingKey}/children")
    Call<MediaContainerResponse> getMetadataChildren(
            @Path("ratingKey") String ratingKey,
            @Header(PlexHeaders.CONTAINER_START) Integer containerStart,
            @Header(PlexHeaders.CONTAINER_SIZE) Integer containerSize,
            @Header(PlexHeaders.TOKEN) String token);

    /**
     * Playback decision. Official template uses {@code /{transcodeType}/:/transcode/universal/decision};
     * for movies {@code transcodeType=video}.
     */
    @GET("video/:/transcode/universal/decision")
    Call<MediaContainerResponse> getPlaybackDecision(
            @Query("path") String path,
            @Query("directPlay") Integer directPlay,
            @Query("directStream") Integer directStream,
            @Query("protocol") String protocol,
            @Query("mediaIndex") Integer mediaIndex,
            @Query("partIndex") Integer partIndex,
            @Query("audioStreamID") Long audioStreamId,
            @Header(PlexHeaders.TOKEN) String token);

    /**
     * Report playback timeline / resume offset (Phase 4.1).
     * Called on play-state changes and periodically while playing.
     */
    @GET(":/timeline")
    Call<Void> reportTimeline(
            @Query("ratingKey") String ratingKey,
            @Query("key") String key,
            @Query("state") String state,
            @Query("time") long time,
            @Query("duration") long duration,
            @Header(PlexHeaders.TOKEN) String token,
            @Header(PlexHeaders.CLIENT_IDENTIFIER) String clientIdentifier);
}
