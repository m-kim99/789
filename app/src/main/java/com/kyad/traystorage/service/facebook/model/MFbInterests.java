package com.kyad.traystorage.service.facebook.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class MFbInterests {
    @SerializedName("books")
    public MFbItemWrapper books;
    @SerializedName("favorite_athletes")
    public List<MFbItem> favoriteAthletes;
    @SerializedName("favorite_teams")
    public List<MFbItem> favoriteTeams;
    @SerializedName("movies")
    public MFbItemWrapper movies;
    @SerializedName("music")
    public MFbItemWrapper music;
    @SerializedName("tagged_places")
    public MFbPlaceWrapper places;
    @SerializedName("sports")
    public MFbItemWrapper sports;
    @SerializedName("television")
    public MFbItemWrapper television;
}
