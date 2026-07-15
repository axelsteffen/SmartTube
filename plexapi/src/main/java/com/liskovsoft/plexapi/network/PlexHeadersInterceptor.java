package com.liskovsoft.plexapi.network;

import com.liskovsoft.plexapi.prefs.PlexPrefs;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Attaches Plex client identity headers (and auth token when available) to every request.
 * An existing {@code X-Plex-Token} on the request is left untouched (per-server token override).
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

        if (original.header(PlexHeaders.TOKEN) == null) {
            String token = resolveAuthToken();
            if (token != null) {
                builder.header(PlexHeaders.TOKEN, token);
            }
        }

        return chain.proceed(builder.build());
    }

    private static String resolveAuthToken() {
        try {
            return PlexPrefs.instance().getAuthToken();
        } catch (IllegalStateException e) {
            return null;
        }
    }
}
