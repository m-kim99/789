package helper;

import android.util.Patterns;
import android.webkit.URLUtil;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Validation {
	public static boolean isContainAlpha(String str) {
		Pattern regex = Pattern.compile("[a-zA-Z]");

        return regex.matcher(str).find();
    }

	public static boolean isContainDigit(String str) {
		Pattern regex = Pattern.compile("[0-9]");

        return regex.matcher(str).find();
    }

	public static boolean isContainSpecial(String str) {
		Pattern regex = Pattern.compile("[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?~`]");

        return regex.matcher(str).find();
    }

	public static boolean isContainAlphaDigit(String str) {
		Pattern regex = Pattern.compile("[a-zA-Z0-9]");

        return regex.matcher(str).find();
    }

	public static boolean isDigit(String str) {
		return Pattern.matches("^[0-9]*$", str);
	}

	public static boolean isAlpha(String str) {
		return Pattern.matches("^[a-zA-Z]*$", str);
	}

	public static boolean isSpecial(String str) {
		return Pattern.matches("^[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?~`]+$", str);
	}

	public static boolean isAlphaDigitSpecial(String str) {
		return Pattern.matches("^[a-zA-Z0-9!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?~`]+$", str);
	}

	public static boolean isKorDigit(String str) {
		return Pattern.matches("^[ㄱ-ㅎ가-힣0-9]*$", str);
	}

	// 010-1234-1234, 01012341234, 01612341234, 016-1234-1234
	public static boolean isPhoneNumber(String str) {
		return Pattern.matches("^01(?:0|1|[6-9])[-\\s\\.]?[0-9]{4}[-\\s\\.]?[0-9]{4}$", str);
	}

	//check email
	public static boolean isEmail(String email) {
		boolean isValid = false;

		int count = email.length() - email.replaceAll("@", "").length();
		if (count >= 2) {
			return false;
		}

		String expression = "^[\\w\\.-]+@([\\w\\-]+\\.)+[A-Z]{2,4}$";
		CharSequence inputStr = email;

		Pattern pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
		Matcher matcher = pattern.matcher(inputStr);
		if (matcher.matches()) {
			isValid = true;
		}
		return isValid;
	}

	public static boolean isUrl(String urlString) {
		try {
			URL url = new URL(urlString);
			return URLUtil.isValidUrl(urlString) && Patterns.WEB_URL.matcher(urlString).matches();
		} catch (MalformedURLException e) {

		}

		return false;
	}

	public static boolean isDate(String checkDate){
		try{
			SimpleDateFormat dateFormat = new  SimpleDateFormat("yyyy-MM-dd");

			dateFormat.setLenient(false);
			dateFormat.parse(checkDate);
			return  true;

		}catch (ParseException e){
			return  false;
		}
	}

	public static boolean isPassword(String password){
		return password.length() >= 6;
	}
}
