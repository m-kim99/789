package com.kyad.traystorage.app.common.util;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;

import com.kyad.traystorage.app.Constants;
import com.kyad.traystorage.data.model.ModelUser;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Global {

    private static final Global instance = new Global();
    public ModelUser gUser = null;
    public int gType = 0;

    public static Global getInstance() {
        return instance;
    }

    private String fcmToken = "";

    public String timeOptionString = "";
    public Constants.StartTimeCase startTimeCase;
    public Constants.TimeCase timeCase;

    public String selectedArea;

    public SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    public SimpleDateFormat dateYearMonthFormat = new SimpleDateFormat("MM-dd", Locale.getDefault());

    public static void clearLoginInfo(Context context) {
        PrefMgr prefMgr = new PrefMgr(context.getSharedPreferences(PrefMgr.traystorage_PREFS, MODE_PRIVATE));
        prefMgr.put(PrefMgr.USER_ID, "");
        prefMgr.put(PrefMgr.USER_PASSWD, "");
        prefMgr.put(PrefMgr.USER_TYPE, "");
    }

    public void setFcmToken(Context context, String token) {
        fcmToken = token;

        PrefMgr prefMgr = new PrefMgr(context.getSharedPreferences(PrefMgr.traystorage_PREFS, MODE_PRIVATE));
        prefMgr.put(PrefMgr.FCM_TOKEN, fcmToken);
    }

    public String getFcmToken(Context context) {
        if (fcmToken == null || fcmToken.isEmpty()) {
            PrefMgr prefMgr = new PrefMgr(context.getSharedPreferences(PrefMgr.traystorage_PREFS, MODE_PRIVATE));
            fcmToken = prefMgr.getString(PrefMgr.FCM_TOKEN, "");
        }
        return fcmToken;
    }

    public void setTodayPopupDone(Context context, int type) {
        PrefMgr prefMgr = new PrefMgr(context.getSharedPreferences(PrefMgr.traystorage_PREFS, MODE_PRIVATE));
        String today = Utils.getTimeStr(new Date(), "yyyy-MM-dd");
        prefMgr.put(PrefMgr.POPUP_TODAY + "_" + type, today);
    }

    public boolean isTodayPopupDone(Context context, int type) {
        PrefMgr prefMgr = new PrefMgr(context.getSharedPreferences(PrefMgr.traystorage_PREFS, MODE_PRIVATE));
        String today = Utils.getTimeStr(new Date(), "yyyy-MM-dd");
        return prefMgr.getString(PrefMgr.POPUP_TODAY + "_" + type, "").equals(today);
    }

    public void setSearchSize(Context context, String size) {
        PrefMgr prefMgr = new PrefMgr(context.getSharedPreferences(PrefMgr.traystorage_PREFS, MODE_PRIVATE));
        prefMgr.put(PrefMgr.SEARCH_SIZE, size);
    }

    public String getSearchSize(Context context) {
        PrefMgr prefMgr = new PrefMgr(context.getSharedPreferences(PrefMgr.traystorage_PREFS, MODE_PRIVATE));
        return prefMgr.getString(PrefMgr.SEARCH_SIZE, "");
    }

    public void setSearchSex(Context context, String sex) {
        PrefMgr prefMgr = new PrefMgr(context.getSharedPreferences(PrefMgr.traystorage_PREFS, MODE_PRIVATE));
        prefMgr.put(PrefMgr.SEARCH_SEX, sex);
    }

    public String getSearchSex(Context context) {
        PrefMgr prefMgr = new PrefMgr(context.getSharedPreferences(PrefMgr.traystorage_PREFS, MODE_PRIVATE));
        return prefMgr.getString(PrefMgr.SEARCH_SEX, "");
    }

    public void setSearchDoor(Context context, String door) {
        PrefMgr prefMgr = new PrefMgr(context.getSharedPreferences(PrefMgr.traystorage_PREFS, MODE_PRIVATE));
        prefMgr.put(PrefMgr.SEARCH_DOOR, door);
    }

    public String getSearchDoor(Context context) {
        PrefMgr prefMgr = new PrefMgr(context.getSharedPreferences(PrefMgr.traystorage_PREFS, MODE_PRIVATE));
        return prefMgr.getString(PrefMgr.SEARCH_DOOR, "");
    }

    public void setSportType(Context context, Integer type) {
        gType = type;
        PrefMgr prefMgr = new PrefMgr(context.getSharedPreferences(PrefMgr.traystorage_PREFS, MODE_PRIVATE));
        prefMgr.put(PrefMgr.SPORT_TYPE, type);
    }

    public void getSportType(Context context) {
        PrefMgr prefMgr = new PrefMgr(context.getSharedPreferences(PrefMgr.traystorage_PREFS, MODE_PRIVATE));
        gType = prefMgr.getInt(PrefMgr.SPORT_TYPE, 0);
    }


    public void setSignInfo(ModelUser user) {
        getInstance().gUser = user;
    }
}
