package com.liskovsoft.plexapi.network.dto;

import com.google.gson.annotations.SerializedName;

/**
 * Connection URI for a plex.tv {@link PlexResource}.
 */
public class PlexResourceConnection {
    @SerializedName("protocol")
    private String mProtocol;

    @SerializedName("address")
    private String mAddress;

    @SerializedName("port")
    private int mPort;

    @SerializedName("uri")
    private String mUri;

    @SerializedName("local")
    private boolean mLocal;

    public String getProtocol() {
        return mProtocol;
    }

    public String getAddress() {
        return mAddress;
    }

    public int getPort() {
        return mPort;
    }

    public String getUri() {
        return mUri;
    }

    public boolean isLocal() {
        return mLocal;
    }
}
