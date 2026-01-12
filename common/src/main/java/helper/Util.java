package helper;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.widget.Toast;

import androidx.annotation.ColorRes;

import java.text.NumberFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class Util {
//	public static void showToast(Context p_context, int strId) {
//		String msg = p_context.getString(strId);
//		showToast(p_context, msg);
//	}
//
//	public static void showToast(Context p_context, String msg) {
//		Utils.showCustomToast(p_context, msg, Toast.LENGTH_SHORT);
//	}

	public static void gotoSetting(Activity activity, int request) {
		Intent intent = new Intent();
		intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
		Uri uri = Uri.fromParts("package", activity.getPackageName(), null);
		intent.setData(uri);
		activity.startActivityForResult(intent, request);
	}

	public static void gotoGPS(Activity activity, int request) {
		Intent intent = new Intent();
		intent.setAction(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
		activity.startActivityForResult(intent, request);
	}

	public static void gotoWifi(Context context) {
		Intent intent = new Intent(Settings.ACTION_WIRELESS_SETTINGS);
		context.startActivity(intent);
	}

	public static int getAge(Date date) {
		Calendar dob = Calendar.getInstance();
		Calendar today = Calendar.getInstance();

		dob.setTime(date);

		int year = dob.get(Calendar.YEAR);
		int month = dob.get(Calendar.MONTH);
		int day = dob.get(Calendar.DAY_OF_MONTH);

		dob.set(year, month+1, day);

		int age = today.get(Calendar.YEAR) - dob.get(Calendar.YEAR);

		if (today.get(Calendar.DAY_OF_YEAR) < dob.get(Calendar.DAY_OF_YEAR)){
			age--;
		}

		return age;
	}

	public static int dpToPx(Context context, int dp) {
		return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.getResources().getDisplayMetrics());
	}

	public static int spToPx(Context context, int sp) {
		return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, context.getResources().getDisplayMetrics());
	}

	/**
	 * pixel -> dp 변환
	 *
	 * @param px px 값
	 *           return int dp로 변환된 값
	 ***/
	public static int pxToDp(Context context, int px) {
		Resources resources = context.getResources();
		DisplayMetrics metrics = resources.getDisplayMetrics();
		float dp = px / (metrics.densityDpi / 160f);
		return (int) dp;
	}

	public static String formatAmount(int amount){
		return NumberFormat.getNumberInstance(Locale.US).format(amount);
	}

	public static int getColor(Context context, @ColorRes int id) {
		return context.getResources().getColor(id);
	}

	//String[] -> string with glue (ex: "a,b,c")
	public static String arrayJoin(String glue, String array[]) {
		String result = "";

		for (int i = 0; i < array.length; i++) {
			result += array[i];
			if (i < array.length - 1) result += glue;
		}
		return result;
	}

	//List<String> -> string with glue (ex: "a,b,c")
	public static String listJoin(String glue, List<String> list) {
		String result = "";

		for (int i = 0; i < list.size(); i++) {
			result += list.get(i);
			if (i < list.size() - 1) result += glue;
		}
		return result;
	}

	public static Date getDate(String s){
		s=s.replace(".","-");
		final int YEAR_LENGTH = 4;
		final int MONTH_LENGTH = 2;
		final int DAY_LENGTH = 2;
		final int MAX_MONTH = 12;
		final int MAX_DAY = 31;
		int firstDash;
		int secondDash;
		java.sql.Date d = null;

		if (s == null) {
			throw new java.lang.IllegalArgumentException();
		}

		firstDash = s.indexOf('-');
		secondDash = s.indexOf('-', firstDash + 1);

		if ((firstDash > 0) && (secondDash > 0) && (secondDash < s.length() - 1)) {
			String yyyy = s.substring(0, firstDash);
			String mm = s.substring(firstDash + 1, secondDash);
			String dd = s.substring(secondDash + 1);
			if (yyyy.length() == YEAR_LENGTH &&
					(mm.length() >= 1 && mm.length() <= MONTH_LENGTH) &&
					(dd.length() >= 1 && dd.length() <= DAY_LENGTH)) {
				int year = Integer.parseInt(yyyy);
				int month = Integer.parseInt(mm);
				int day = Integer.parseInt(dd);

				if ((month >= 1 && month <= MAX_MONTH) && (day >= 1 && day <= MAX_DAY)) {
					d = new java.sql.Date(year - 1900, month - 1, day);
				}
			}
		}
		if (d == null) {
			throw new java.lang.IllegalArgumentException();
		}

		return d;
	}
}
