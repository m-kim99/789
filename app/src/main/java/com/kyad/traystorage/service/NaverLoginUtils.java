package com.kyad.traystorage.service;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import androidx.annotation.NonNull;

import com.navercorp.nid.NaverIdLoginSDK;
import com.navercorp.nid.oauth.OAuthLoginCallback;
//import com.nhn.android.naverlogin.OAuthLogin;
//import com.nhn.android.naverlogin.OAuthLoginHandler;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.URL;

import lombok.Data;
import lombok.EqualsAndHashCode;

public class NaverLoginUtils {
    private static String OAUTH_CLIENT_ID = "DYpR8dkema6fR0FcnF0w";
    private static String OAUTH_CLIENT_SECRET = "2iE5dYLVMn";
    private static String OAUTH_CLIENT_NAME = "네이버 아이디로 로그인";
    public static final String NAVIER_USER_DETAIL_URL = "https://apis.naver.com/nidlogin/nid/getUserProfile";

    private Context mContext = null;
//    private OAuthLogin mOAuthLoginInstance;
    private LoginListener mLoginListener = null;
    /**
     * If you pass OAuthLoginHandler as a parameter of the startOAuthLoginActivity() method or
     * register it to the OAuthLoginButton object, you can verify that authorization is
     * terminated.
     */
//    private OAuthLoginHandler mOAuthLoginHandler = new OAuthLoginHandler() {
//        @Override
//        public void run(boolean success) {
//            if (success) {
//                String accessToken = mOAuthLoginInstance.getAccessToken(mContext);
//                String refreshToken = mOAuthLoginInstance.getRefreshToken(mContext);
//                long expiresAt = mOAuthLoginInstance.getExpiresAt(mContext);
//                String tokenType = mOAuthLoginInstance.getTokenType(mContext);
//
//                Log.d("accessToken : ", accessToken);
//                Log.d("refreshToken : ", refreshToken);
//                Log.d("expiresAt : ", String.valueOf(expiresAt));
//                Log.d("tokenType : ", tokenType);
//                Log.d("State : ", mOAuthLoginInstance.getState(mContext).toString());
//
//                /* Fetch User Detail Fron the URL*/
//                new FetchNaverUserData(mContext, NAVIER_USER_DETAIL_URL, accessToken, mLoginListener).execute();
//            } else {
//                String errorCode = mOAuthLoginInstance.getLastErrorCode(mContext).getCode();
//                String errorDesc = mOAuthLoginInstance.getLastErrorDesc(mContext);
//                Log.d("errorCode:" + errorCode, "errorDesc:" + errorDesc);
//
//                if (mLoginListener != null) {
//                    mLoginListener.onFailed(errorCode);
//                }
//            }
//        }
//    };

    private OAuthLoginCallback oauthLoginCallback = new OAuthLoginCallback() {
        @Override
        public void onSuccess() {
            String accessToken = NaverIdLoginSDK.INSTANCE.getAccessToken();
            String refreshToken = NaverIdLoginSDK.INSTANCE.getRefreshToken();
            long expiresAt = NaverIdLoginSDK.INSTANCE.getExpiresAt();
            String tokenType = NaverIdLoginSDK.INSTANCE.getTokenType();

            Log.d("accessToken : ", accessToken);
            Log.d("refreshToken : ", refreshToken);
            Log.d("expiresAt : ", String.valueOf(expiresAt));
            Log.d("tokenType : ", tokenType);
            Log.d("State : ", NaverIdLoginSDK.INSTANCE.getState().toString());

            /* Fetch User Detail Fron the URL*/
            new FetchNaverUserData(mContext, NAVIER_USER_DETAIL_URL, accessToken, mLoginListener).execute();
        }

        @Override
        public void onFailure(int httpStatus, @NonNull String message) {
            Log.d("errorCode: " + httpStatus, "errorDesc: " + message);

            if (mLoginListener != null) {
                mLoginListener.onFailed(String.valueOf(httpStatus));
            }
        }

        @Override
        public void onError(int errorCode, @NonNull String message) {
            // onFailure와 동일 처리
            onFailure(errorCode, message);
        }
    };

    public NaverLoginUtils(Context context) {
        mContext = context;

//        mOAuthLoginInstance = OAuthLogin.getInstance();

        NaverIdLoginSDK.INSTANCE.initialize(
            context,
            OAUTH_CLIENT_ID,
            OAUTH_CLIENT_SECRET,
            OAUTH_CLIENT_NAME
        );
//        mOAuthLoginInstance.showDevelopersLog(true);
//        mOAuthLoginInstance.init(context, OAUTH_CLIENT_ID, OAUTH_CLIENT_SECRET, OAUTH_CLIENT_NAME);
    }

    public void login(Activity activity, LoginListener listener) {
        mLoginListener = listener;
        NaverIdLoginSDK.INSTANCE.authenticate(activity, oauthLoginCallback);
//        mOAuthLoginInstance.startOauthLoginActivity(activity, mOAuthLoginHandler);


    }

    @Data
    @EqualsAndHashCode(callSuper = false)
    public static class UserInfo implements Serializable {
        private String token;
        private String id;
        private String nickname;
        private String profile_image;
        private String age;
        private String gender;
        private String email;
        private String name;
        private String birthday;

        public String getToken() {
            return token;
        }

        public void setToken(String token) {
            this.token = token;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getNickname() {
            return nickname;
        }

        public void setNickname(String nickname) {
            this.nickname = nickname;
        }

        public String getProfile_image() {
            return profile_image;
        }

        public void setProfile_image(String profile_image) {
            this.profile_image = profile_image;
        }

        public String getAge() {
            return age;
        }

        public void setAge(String age) {
            this.age = age;
        }

        public String getGender() {
            return gender;
        }

        public void setGender(String gender) {
            this.gender = gender;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getBirthday() {
            return birthday;
        }

        public void setBirthday(String birthday) {
            this.birthday = birthday;
        }
    }

    public interface LoginListener {
        void onSuccessed(UserInfo userInfo);

        void onFailed(String error);
    }

    public class FetchNaverUserData extends AsyncTask<Void, Void, String> {

        String Url, Token;
        Context mContext;
        private LoginListener callback;

        public FetchNaverUserData(Context context, String url, String token, LoginListener listener) {
            this.mContext = context;
            this.Url = url;
            this.Token = token;
            this.callback = listener;
        }

        @Override
        protected String doInBackground(Void... params) {
            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain the raw JSON response as a string.
            String forecastJsonStr = null;

            try {
                // Construct the URL for the OpenWeatherMap query
                // Possible parameters are avaiable at OWM's forecast API page, at
                // http://openweathermap.org/API#forecast
                URL url = new URL(Url);

                // Create the request to OpenWeatherMap, and open the connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("POST");
                urlConnection.setRequestProperty("Authorization", "Bearer " + Token);
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // buffer for debugging.
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    return null;
                }
                forecastJsonStr = buffer.toString();
                return forecastJsonStr;
            } catch (IOException e) {
                Log.e("PlaceholderFragment", "Error ", e);
                // If the code didn't successfully get the weather data, there's no point in attemping
                // to parse it.
                return null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e("PlaceholderFragment", "Error closing stream", e);
                    }
                }
            }
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            Log.i("Response", s);
            if (s != null) {
                Document doc = Jsoup.parse(s, "", Parser.xmlParser());

                String id = doc.select("id").text();
                String nickname = doc.select("nickname").text();
                String profile_image = doc.select("profile_image").text();
                String age = doc.select("age").text();
                String gender = doc.select("gender").text();
                String email = doc.select("email").text();
                String name = doc.select("name").text();
                String birthday = doc.select("birthday").text();

                UserInfo userInfo = new UserInfo();
                userInfo.setId(id);
                userInfo.setNickname(nickname);
                userInfo.setProfile_image(profile_image);
                userInfo.setAge(age);
                userInfo.setGender(gender);
                userInfo.setEmail(email);
                userInfo.setName(name);
                userInfo.setBirthday(birthday);

                if (callback != null) {
                    callback.onSuccessed(userInfo);
                }

                Log.d("ID : ", doc.select("id").text());
                Log.d("NICKNAME : ", doc.select("nickname").text());
                Log.d("PROFILE_IMAGE : ", doc.select("profile_image").text());
                Log.d("AGE : ", doc.select("age").text());
                Log.d("GENDER : ", doc.select("gender").text());
                Log.d("EMAIL : ", doc.select("email").text());
                Log.d("NAME : ", doc.select("name").text());
                Log.d("BIRTHDAY : ", doc.select("birthday").text());
            } else {
                if (callback != null) {
                    callback.onFailed("No Response");
                }
            }
        }
    }
}
