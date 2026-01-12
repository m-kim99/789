package com.kyad.traystorage.app.common.util;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public final class PrefMgr {

    public static final String traystorage_PREFS = "traystorage_prefs";

    public static final String FCM_TOKEN = "fcm_token";

    public static final String USER_ID = "user_id";
    public static final String USER_PASSWD = "user_passwd";
    public static final String USER_TYPE = "user_type";
    public static final String ACCESS_TOKEN = "access_token";
    public static final String FIRST_START = "first_start";
    public static final String HAS_PERMISSION = "has_permission";

    public static final String POPUP_TODAY = "popup_today";

    public static final String SEARCH_SIZE = "search_size";
    public static final String SEARCH_SEX = "search_SEX";
    public static final String SEARCH_DOOR = "search_door";

    public static final String SPORT_TYPE = "sport_type";



    private final SharedPreferences prefs;

    public PrefMgr(SharedPreferences prefs) {
        super();
        this.prefs = prefs;
    }

    public void put(String key, Object value) {
        Editor editor = prefs.edit();
        if (value.getClass().equals(String.class)) {
            editor.putString(key, (String) value);
        } else if (value.getClass().equals(Boolean.class)) {
            editor.putBoolean(key, (Boolean) value);
        } else if (value.getClass().equals(Integer.class)) {
            editor.putInt(key, (Integer) value);
        } else if (value.getClass().equals(Float.class) || value.getClass().equals(Double.class)) {
            editor.putFloat(key, ((Number) value).floatValue());
        }

        editor.commit();
    }

    public int getInt(String key, int defaultValue) {
        return prefs.getInt(key, defaultValue);
    }

    public String getString(String key, String defaultValue) {
        return prefs.getString(key, defaultValue);
    }

    public float getFloat(String key, float defaultValue) {
        return prefs.getFloat(key, defaultValue);
    }

    public boolean getBoolean(String key, Boolean defaultValue) {
        return prefs.getBoolean(key, defaultValue);
    }

    public SharedPreferences getPrefs() {
        return prefs;
    }

}

