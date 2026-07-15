package com.liskovsoft.plexapi.network;

/**
 * Builds absolute PMS URLs from relative {@code key} / {@code Part.key} paths.
 */
public final class PlexUrlHelper {
    private PlexUrlHelper() {
    }

    /**
     * Joins server base URL with a PMS-relative key and appends the auth token query.
     *
     * @param baseUrl e.g. {@code https://host:32400/}
     * @param relativeKey e.g. {@code /library/parts/827/file.mkv} or {@code /library/metadata/1/thumb/…}
     * @param token {@code X-Plex-Token} value (query form accepted by PMS)
     */
    public static String absoluteUrl(String baseUrl, String relativeKey, String token) {
        if (relativeKey == null || relativeKey.isEmpty()) {
            return null;
        }
        if (relativeKey.startsWith("http://") || relativeKey.startsWith("https://")) {
            return appendToken(relativeKey, token);
        }

        String base = normalizeBaseUrl(baseUrl);
        String path = relativeKey.startsWith("/") ? relativeKey.substring(1) : relativeKey;
        return appendToken(base + path, token);
    }

    public static String normalizeBaseUrl(String baseUrl) {
        if (baseUrl == null || baseUrl.isEmpty()) {
            throw new IllegalArgumentException("baseUrl required");
        }
        return baseUrl.endsWith("/") ? baseUrl : baseUrl + "/";
    }

    private static String appendToken(String url, String token) {
        if (token == null || token.isEmpty()) {
            return url;
        }
        String sep = url.contains("?") ? "&" : "?";
        return url + sep + "X-Plex-Token=" + token;
    }
}
