package com.kyad.traystorage.service;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;

import java.io.Serializable;

import lombok.Data;
import lombok.EqualsAndHashCode;

//import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeTokenRequest;
//import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
//import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
//import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
//import com.google.api.client.http.HttpTransport;
//import com.google.api.client.http.javanet.NetHttpTransport;
//import com.google.api.client.json.jackson2.JacksonFactory;
//import com.google.api.services.people.v1.PeopleService;
//import com.google.api.services.people.v1.model.Birthday;
//import com.google.api.services.people.v1.model.Gender;
//import com.google.api.services.people.v1.model.Person;

public class GoogleUtils implements GoogleApiClient.OnConnectionFailedListener {
    private static final int REQ_LOGIN_CODE = 9001;

    private static String CLIENT_ID = "139387942332-ofh3bejqvfj4unlpfb32d3abnmsfv0it.apps.googleusercontent.com";
    private static String CLIENT_SECRET = "RrM24W4GjueNvizBTEF9yjFX";

    private LoginListener mLoginListner = null;
    private Context mContext = null;
    private GoogleApiClient mGoogleApiClient;
    private Activity mLoginActivity = null;

    public GoogleUtils(FragmentActivity context) {
        mContext = context;
        //returns basic user information such as user id,username,profile pic etc
        GoogleSignInOptions signInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestEmail().build();

        //initialise api client
        mGoogleApiClient = new GoogleApiClient.Builder(mContext).enableAutoManage(context, this).addApi(Auth.GOOGLE_SIGN_IN_API, signInOptions).build();
        mLoginActivity = context;
    }

    public void login(FragmentActivity activity, LoginListener listner) {
        mLoginListner = listner;

        Intent intent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        activity.startActivityForResult(intent, REQ_LOGIN_CODE);
    }

    public boolean onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQ_LOGIN_CODE) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResult(result);
            return true;
        }

        return false;
    }

    //handle the sign in result
    private void handleSignInResult(GoogleSignInResult result) {
        Log.d("RESULT:::", String.valueOf(result.isSuccess()));

        if (result.isSuccess()) {
            GoogleSignInAccount account = result.getSignInAccount();
            String nam = account.getDisplayName();
            Log.d("Name:::", nam);
            //Toast.makeText(this, "nam " + nam, Toast.LENGTH_SHORT).show();
            String emai = account.getEmail();
            Log.d("Email:::", emai);

            String imgUrl = null;

            try {

                imgUrl = account.getPhotoUrl().toString();
                Log.d("IMageURL", imgUrl);


            } catch (NullPointerException e) {
                e.printStackTrace();
            }

            UserInfo userInfo = new UserInfo();
            userInfo.setId(account.getId());
            userInfo.setEmail(emai);
            userInfo.setName(nam);
            userInfo.setPhoto(imgUrl);

            if (mLoginListner != null) {
                mLoginListner.onSuccessed(userInfo);
            }
            //new GetGenderAsyncTask(mLoginActivity, userInfo, mLoginListner).execute(account.getEmail());

        } else {
            if (mLoginListner != null) {
                mLoginListner.onFailed(ConnectionResult.INVALID_ACCOUNT);
            }
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        if (mLoginListner != null) {
            mLoginListner.onFailed(connectionResult.getErrorCode());
        }
    }

    @Data
    @EqualsAndHashCode(callSuper = false)
    public static class UserInfo implements Serializable {
        private String id;
        private String email;
        private String name;
        private String photo;
        private String birth;
        private String gender;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
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

        public String getPhoto() {
            return photo;
        }

        public void setPhoto(String photo) {
            this.photo = photo;
        }

        public String getBirth() {
            return birth;
        }

        public void setBirth(String birth) {
            this.birth = birth;
        }

        public String getGender() {
            return gender;
        }

        public void setGender(String gender) {
            this.gender = gender;
        }
    }

    public interface LoginListener {
        void onSuccessed(UserInfo userInfo);

        void onFailed(int error);
    }

    // Retrieve and save the url to the users Cover photo if they have one
//    public static class GetGenderAsyncTask extends AsyncTask<String, Void, Void> {
//        // Retrieved from the sigin result of an authorized GoogleSignIn
//        UserInfo userInfo;
//        Activity activity;
//        LoginListener mLoginListener;
//
//        public GetGenderAsyncTask(Activity activity, UserInfo userInfo, LoginListener loginListener) {
//            this.activity = activity;
//            this.userInfo = userInfo;
//            this.mLoginListener = loginListener;
//        }
//
//        public static PeopleService setUp(Context context, String serverAuthCode) throws IOException {
//            HttpTransport httpTransport = new NetHttpTransport();
//            JacksonFactory jsonFactory = JacksonFactory.getDefaultInstance();
//
//            // Redirect URL for web based applications.
//            // Can be empty too.
//            String redirectUrl = "urn:ietf:wg:oauth:2.0:oob";
//
//
//            // Exchange auth code for access token
//            GoogleTokenResponse tokenResponse = new GoogleAuthorizationCodeTokenRequest(
//                    httpTransport,
//                    jsonFactory,
//                    CLIENT_ID,
//                    CLIENT_SECRET,
//                    serverAuthCode,
//                    redirectUrl).execute();
//
//            // Then, create a GoogleCredential object using the tokens from GoogleTokenResponse
//            GoogleCredential credential = new GoogleCredential.Builder()
//                    .setClientSecrets(CLIENT_ID, CLIENT_SECRET)
//                    .setTransport(httpTransport)
//                    .setJsonFactory(jsonFactory)
//                    .build();
//
//            credential.setFromTokenResponse(tokenResponse);
//
//            // credential can then be used to access Google services
//            PeopleService peopleService =
//                    new PeopleService.Builder(httpTransport, jsonFactory, credential).build();
//            return peopleService;
//        }
//
//
//        @Override
//        protected Void doInBackground(String... params) {
//            String personEmail = params[0];
//
//            Person userProfile = null;
//            try {
//                //PeopleService service = setUp(activity, mCurSignAccount.getIdToken());
//                Collection<String> scopes = new ArrayList<>(Collections.singletonList(Scopes.PROFILE));
//                HttpTransport httpTransport = new NetHttpTransport();
//                JacksonFactory jsonFactory = JacksonFactory.getDefaultInstance();
//                GoogleAccountCredential credential =
//                        GoogleAccountCredential.usingOAuth2(activity, scopes);
//                credential.setSelectedAccount(new Account(personEmail, "com.google"));
//
//                PeopleService service = new PeopleService.Builder(httpTransport, jsonFactory, credential)
//                        .setApplicationName(activity.getString(R.string.app_name)) // your app name
//                        .build();
//
//                userProfile = service.people().get(String.format("people/me")).setPersonFields("genders, birthdays").execute(); // need public gender on google account
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//
//            // Get whatever you want
//            if (userProfile != null) {
//                List<Gender> covers = userProfile.getGenders();
//                List<Birthday> birthdays = userProfile.getBirthdays();
//                if (covers != null && covers.size() > 0 && covers.get(0) != null) {
//                    userInfo.setGender(covers.get(0).getValue());
//                }
//
//                if (birthdays != null && birthdays.size() > 0 && birthdays.get(0) != null) {
//                    String date = birthdays.get(0).getDate().getYear() + "-" + birthdays.get(0).getDate().getMonth() + "-" + birthdays.get(0).getDate().getDay();
//                    userInfo.setBirth(date);
//                }
//            }
//
//            this.activity.runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//                    if (mLoginListener != null) {
//                        mLoginListener.onSuccessed(userInfo);
//                    }
//                }
//            });
//            return null;
//        }
//    }
}

