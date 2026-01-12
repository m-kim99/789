package com.kyad.traystorage.service.facebook.model;

import com.google.gson.annotations.SerializedName;

public class MFacebookUser {
    private static final String FACEBOOK_GENDER_FEMALE = "female";
    private static final String FACEBOOK_GENDER_MALE = "male";
    @SerializedName("email")
    public String email;
    @SerializedName("gender")
    public String gender;
    @SerializedName("id")
    public long id;
    @SerializedName("birthday")
    public String birthday;
    @SerializedName("location")
    public String location;
    @SerializedName("link")
    public String link;
    @SerializedName("name")
    public String name;
    @SerializedName("verified")
    public boolean verified;

    public String getBirthdayYear() {
        if (birthday != null && birthday.length() >= 4) {
            return birthday.substring(birthday.length() - 4, birthday.length());
        }
        return birthday;
    }
}
