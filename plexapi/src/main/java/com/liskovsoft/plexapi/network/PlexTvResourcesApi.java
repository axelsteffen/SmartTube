package com.liskovsoft.plexapi.network;

import com.liskovsoft.plexapi.network.dto.PlexResource;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * plex.tv account resources (server discovery).
 *
 * @see plexapi/openapi-plex-pms-in-use.yaml
 */
public interface PlexTvResourcesApi {
    @GET("api/v2/resources")
    Call<List<PlexResource>> getResources(
            @Query("includeHttps") int includeHttps,
            @Query("includeRelay") int includeRelay);
}
