package com.kyad.traystorage.service.facebook.model;

import com.google.gson.annotations.SerializedName;

public class MFbTaggedPlaceItem {
    @SerializedName("id")
    public String id;
    @SerializedName("place")
    public MFbItem place;
}
