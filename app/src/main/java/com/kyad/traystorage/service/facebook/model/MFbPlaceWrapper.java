package com.kyad.traystorage.service.facebook.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class MFbPlaceWrapper {
    @SerializedName("data")
    public List<MFbTaggedPlaceItem> data;
}
