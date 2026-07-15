package com.liskovsoft.plexapi.network;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Attaches Plex client identity headers to every request.
 */
public final class PlexHeadersInterceptor implements Interceptor {
    @Override
    public Response intercept(Chain chain) throws IOException {
        Request original = chain.request();
        Request.Builder builder = original.newBuilder()
                .header(PlexHeaders.ACCEPT, PlexHeaders.ACCEPT_JSON)
                .header(PlexHeaders.CLIENT_IDENTIFIER, PlexHeaders.clientIdentifier())
                .header(PlexHeaders.PRODUCT, PlexHeaders.PRODUCT_VALUE)
                .header(PlexHeaders.VERSION, PlexHeaders.VERSION_VALUE)
                .header(PlexHeaders.DEVICE, PlexHeaders.DEVICE_VALUE)
                .header(PlexHeaders.PLATFORM, PlexHeaders.PLATFORM_VALUE);

        return chain.proceed(builder.build());
    }
}
