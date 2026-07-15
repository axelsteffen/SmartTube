package com.liskovsoft.plexapi.network.dto;

import com.google.gson.annotations.SerializedName;

import java.util.Collections;
import java.util.List;

/**
 * plex.tv account resource (typically a Plex Media Server).
 */
public class PlexResource {
    @SerializedName("name")
    private String mName;

    @SerializedName("product")
    private String mProduct;

    @SerializedName("productVersion")
    private String mProductVersion;

    @SerializedName("platform")
    private String mPlatform;

    @SerializedName("clientIdentifier")
    private String mClientIdentifier;

    @SerializedName("owned")
    private boolean mOwned;

    @SerializedName("presence")
    private boolean mPresence;

    @SerializedName("provides")
    private String mProvides;

    @SerializedName("publicAddress")
    private String mPublicAddress;

    @SerializedName("accessToken")
    private String mAccessToken;

    @SerializedName("connections")
    private List<PlexResourceConnection> mConnections;

    public String getName() {
        return mName;
    }

    public String getProduct() {
        return mProduct;
    }

    public String getProductVersion() {
        return mProductVersion;
    }

    public String getPlatform() {
        return mPlatform;
    }

    public String getClientIdentifier() {
        return mClientIdentifier;
    }

    public boolean isOwned() {
        return mOwned;
    }

    public boolean isPresence() {
        return mPresence;
    }

    public String getProvides() {
        return mProvides;
    }

    public String getPublicAddress() {
        return mPublicAddress;
    }

    public String getAccessToken() {
        return mAccessToken;
    }

    public List<PlexResourceConnection> getConnections() {
        return mConnections != null ? mConnections : Collections.emptyList();
    }

    /** True when this resource looks like a Plex Media Server. */
    public boolean isServer() {
        if (mProvides != null && mProvides.contains("server")) {
            return true;
        }
        return mProduct != null && mProduct.toLowerCase().contains("media server");
    }
}
