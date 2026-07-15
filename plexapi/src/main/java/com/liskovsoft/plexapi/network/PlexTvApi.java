package com.liskovsoft.plexapi.network;

import com.liskovsoft.plexapi.auth.PlexPinResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * plex.tv PIN authentication endpoints (API v2).
 */
public interface PlexTvApi {
    @POST("api/v2/pins")
    Call<PlexPinResponse> createPin(@Query("strong") boolean strong);

    @GET("api/v2/pins/{id}")
    Call<PlexPinResponse> checkPin(@Path("id") String id);
}
