package com.liskovsoft.plexapi.network.dto;

import com.google.gson.annotations.SerializedName;

import java.util.Collections;
import java.util.List;

/**
 * PMS {@code MediaContainer} body fields (inside {@link MediaContainerResponse}).
 */
public class MediaContainer {
    @SerializedName("size")
    private Integer mSize;

    @SerializedName("totalSize")
    private Integer mTotalSize;

    @SerializedName("offset")
    private Integer mOffset;

    @SerializedName("machineIdentifier")
    private String mMachineIdentifier;

    @SerializedName("friendlyName")
    private String mFriendlyName;

    @SerializedName("version")
    private String mVersion;

    @SerializedName("claimed")
    private Boolean mClaimed;

    @SerializedName("identifier")
    private String mIdentifier;

    @SerializedName("librarySectionID")
    private Integer mLibrarySectionId;

    @SerializedName("librarySectionTitle")
    private String mLibrarySectionTitle;

    @SerializedName("directPlayDecisionCode")
    private Integer mDirectPlayDecisionCode;

    @SerializedName("transcodeDecisionCode")
    private Integer mTranscodeDecisionCode;

    @SerializedName("Directory")
    private List<PlexDirectory> mDirectories;

    @SerializedName("Metadata")
    private List<PlexMetadata> mMetadata;

    public Integer getSize() {
        return mSize;
    }

    public Integer getTotalSize() {
        return mTotalSize;
    }

    public Integer getOffset() {
        return mOffset;
    }

    public String getMachineIdentifier() {
        return mMachineIdentifier;
    }

    public String getFriendlyName() {
        return mFriendlyName;
    }

    public String getVersion() {
        return mVersion;
    }

    public Boolean getClaimed() {
        return mClaimed;
    }

    public String getIdentifier() {
        return mIdentifier;
    }

    public Integer getLibrarySectionId() {
        return mLibrarySectionId;
    }

    public String getLibrarySectionTitle() {
        return mLibrarySectionTitle;
    }

    public Integer getDirectPlayDecisionCode() {
        return mDirectPlayDecisionCode;
    }

    public Integer getTranscodeDecisionCode() {
        return mTranscodeDecisionCode;
    }

    public List<PlexDirectory> getDirectories() {
        return mDirectories != null ? mDirectories : Collections.emptyList();
    }

    public List<PlexMetadata> getMetadata() {
        return mMetadata != null ? mMetadata : Collections.emptyList();
    }
}
