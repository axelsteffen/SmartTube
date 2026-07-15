package com.liskovsoft.plexapi.server;

import com.liskovsoft.plexapi.network.PlexUrlHelper;
import com.liskovsoft.plexapi.network.dto.PlexResource;
import com.liskovsoft.plexapi.network.dto.PlexResourceConnection;
import com.liskovsoft.plexserviceinterfaces.data.PlexServer;

import java.util.List;

/**
 * Immutable {@link PlexServer} mapped from a plex.tv resource.
 */
public final class PlexServerImpl implements PlexServer {
    private final String mClientIdentifier;
    private final String mName;
    private final String mBaseUrl;
    private final String mAccessToken;
    private final boolean mOwned;
    private final boolean mOnline;

    public PlexServerImpl(String clientIdentifier, String name, String baseUrl,
                          String accessToken, boolean owned, boolean online) {
        mClientIdentifier = clientIdentifier;
        mName = name;
        mBaseUrl = baseUrl != null ? PlexUrlHelper.normalizeBaseUrl(baseUrl) : null;
        mAccessToken = accessToken;
        mOwned = owned;
        mOnline = online;
    }

    /**
     * Maps a PMS resource, or {@code null} if it is not a server / has no usable connection.
     */
    public static PlexServerImpl fromResource(PlexResource resource) {
        if (resource == null || !resource.isServer()) {
            return null;
        }
        String baseUrl = pickBaseUrl(resource.getConnections());
        if (baseUrl == null) {
            return null;
        }
        return new PlexServerImpl(
                resource.getClientIdentifier(),
                resource.getName(),
                baseUrl,
                resource.getAccessToken(),
                resource.isOwned(),
                resource.isPresence());
    }

    /**
     * Prefer local HTTPS, then local, then remote HTTPS, then first URI.
     */
    static String pickBaseUrl(List<PlexResourceConnection> connections) {
        if (connections == null || connections.isEmpty()) {
            return null;
        }

        PlexResourceConnection best = null;
        int bestScore = Integer.MIN_VALUE;

        for (PlexResourceConnection connection : connections) {
            if (connection == null) {
                continue;
            }
            String uri = connection.getUri();
            if (uri == null || uri.isEmpty()) {
                continue;
            }

            int score = 0;
            if (connection.isLocal()) {
                score += 2;
            }
            if (isHttps(connection)) {
                score += 1;
            }

            if (score > bestScore) {
                bestScore = score;
                best = connection;
            }
        }

        return best != null ? best.getUri() : null;
    }

    private static boolean isHttps(PlexResourceConnection connection) {
        String protocol = connection.getProtocol();
        if (protocol != null && protocol.equalsIgnoreCase("https")) {
            return true;
        }
        String uri = connection.getUri();
        return uri != null && uri.regionMatches(true, 0, "https://", 0, 8);
    }

    @Override
    public String getClientIdentifier() {
        return mClientIdentifier;
    }

    @Override
    public String getName() {
        return mName;
    }

    @Override
    public String getBaseUrl() {
        return mBaseUrl;
    }

    @Override
    public String getAccessToken() {
        return mAccessToken;
    }

    @Override
    public boolean isOwned() {
        return mOwned;
    }

    @Override
    public boolean isOnline() {
        return mOnline;
    }
}
