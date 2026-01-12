package com.kyad.traystorage.app.common.util;

//import static com.blankj.utilcode.util.StringUtils.getString;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import android.widget.Toast;

//import com.blankj.utilcode.util.ToastUtils;
import com.kyad.traystorage.R;

import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class Utils {

    private static final String TAG = Utils.class.getSimpleName();

    public static boolean isEmailValid(String email) {
        return (Patterns.EMAIL_ADDRESS.matcher(email).matches());
    }

    public static String getVersion(Context context) {
        try {
            PackageInfo pi = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return pi.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            return "";
        }
    }

    public static int getVersionCode(Context context) {
        try {
            PackageInfo pi = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return pi.versionCode;

        } catch (PackageManager.NameNotFoundException e) {
            return 0;
        }
    }

    public static String getTimeStr(String str) {
        if (str.equals("day")) {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
            return format.format(new Date());
        } else {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
            return format.format(new Date());
        }
    }

    public static String getTimeStr(Date date, String strFormat) {
        if (date == null)
            return "";
        SimpleDateFormat format = new SimpleDateFormat(strFormat);
        return format.format(date);
    }

    public static int getWeekStr(Date date) {
        Calendar curDay = Calendar.getInstance();
        curDay.set(date.getYear(), date.getMonth(), date.getDay());
        return curDay.get(Calendar.DAY_OF_WEEK);
    }

    public static Date convertStringToDate(String dateStr, String format) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(format);
        try {
            return simpleDateFormat.parse(dateStr);
        } catch (ParseException e) {
            return null;
        }
    }

    public static boolean isNullOrEmpty(String str) {
        return str == null || str.isEmpty();
    }

    public static String getComma(long number) {
        return NumberFormat.getNumberInstance(Locale.US).format(number);
    }

    public static void hideKeyboard(Activity activity) {
        View view = activity.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    public static Toast gToast = null;
    public static Toast showCustomToast(Context context, int stringid){
        return showCustomToast(context, stringid, Toast.LENGTH_SHORT);
    }
    public static Toast showCustomToast(Context context, int stringid, int len) {
        return showCustomToast(context, context.getResources().getText(stringid).toString(), len);
    }
    public static Toast showCustomToast(Context context, String msg, int len){
        if (gToast != null) {
            gToast.cancel();
            gToast = null;
        }
        LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.custom_toast, null);

        TextView txtView = (TextView)view.findViewById(R.id.txt_message);
        txtView.setText(msg);

        gToast = new Toast(context);
        gToast.setView(view);

        gToast.setDuration(len);
        gToast.show();
        return gToast;
    }
//
//    public static String getDayformat(String start_date, String start_time, String end_time, int duration) {
//        String[] weeks = {getString(R.string.Sun), getString(R.string.monday), getString(R.string.tuesday), getString(R.string.wednesday), getString(R.string.thursday), getString(R.string.friday), getString(R.string.saturday)};
//        String[] months = {getString(R.string.january), getString(R.string.february), getString(R.string.march), getString(R.string.april), getString(R.string.may), getString(R.string.june), getString(R.string.july), getString(R.string.august), getString(R.string.september), getString(R.string.october), getString(R.string.november), getString(R.string.december)};
//
//        Date date = Utils.convertStringToDate(start_date, "yyyy-MM-dd");
//
//        int hour = duration / 60;
//        return String.format(getString(R.string.history_day_format), weeks[Utils.getWeekStr(date)], months[date.getMonth()], date.getDate(), start_time, end_time, hour);
//    }

}
