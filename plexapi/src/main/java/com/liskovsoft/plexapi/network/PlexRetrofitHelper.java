package com.liskovsoft.plexapi.network;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Retrofit factory for plex.tv and per-server PMS calls.
 */
public final class PlexRetrofitHelper {
    public static final String DEFAULT_PLEX_TV_BASE_URL = "https://plex.tv/";

    private static String sPlexTvBaseUrl = DEFAULT_PLEX_TV_BASE_URL;
    private static OkHttpClient sClient;

    private PlexRetrofitHelper() {
    }

    /** Override plex.tv base URL (MockWebServer). Resets cached HTTP client. */
    public static synchronized void setPlexTvBaseUrl(String baseUrl) {
        sPlexTvBaseUrl = baseUrl != null ? baseUrl : DEFAULT_PLEX_TV_BASE_URL;
        sClient = null;
    }

    public static synchronized void reset() {
        sPlexTvBaseUrl = DEFAULT_PLEX_TV_BASE_URL;
        sClient = null;
    }

    public static <T> T createPlexTvApi(Class<T> clazz) {
        return buildRetrofit(sPlexTvBaseUrl).create(clazz);
    }

    /**
     * PMS Retrofit for a server connection URI.
     *
     * @param baseUrl connection {@code uri} from plex.tv resources (trailing slash optional)
     */
    public static <T> T createPmsApi(String baseUrl, Class<T> clazz) {
        return buildRetrofit(PlexUrlHelper.normalizeBaseUrl(baseUrl)).create(clazz);
    }

    private static Retrofit buildRetrofit(String baseUrl) {
        return new Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(getClient())
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    private static synchronized OkHttpClient getClient() {
        if (sClient == null) {
            sClient = new OkHttpClient.Builder()
                    .addInterceptor(new PlexHeadersInterceptor())
                    .connectTimeout(15, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .writeTimeout(15, TimeUnit.SECONDS)
                    .build();
        }
        return sClient;
    }
}
