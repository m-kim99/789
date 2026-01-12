package com.kyad.traystorage.service.facebook;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookAuthorizationException;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.share.Sharer;
import com.facebook.share.model.SharePhoto;
import com.facebook.share.model.SharePhotoContent;
import com.facebook.share.widget.ShareDialog;
import com.google.gson.Gson;
import com.kyad.traystorage.service.facebook.model.MFacebookUser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FacebookUtils {
    private static final List<String> loginPermissions = Arrays.asList("public_profile", "email", "user_birthday", "user_friends", "user_gender");
    private CallbackManager callbackManager;
    private ShareDialog shareDialog;
    private Context context;

    public interface FacebookLoginListner {
        void onSuccessed(String userId, String token);

        void onFailed(FacebookException e);
    }

    public interface FacebookUserInfoListner {
        void onGetUserInfo(MFacebookUser userInfo);

        void onFailed(FacebookException e);
    }

    public interface FacebookFriendListListner {
        void onFriendList(ArrayList<String> arrayList);

        void onFailed(FacebookException e);
    }

    public FacebookUtils(Context context) {
        this.context = context;
        FacebookSdk.sdkInitialize(context);
    }

    public boolean onActivityResult(int requestCode, int resultCode, Intent data) {
        if (callbackManager == null) {
            return false;
        }
        return callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    public void login(final Activity context, final FacebookUserInfoListner onGetFacebookUserListener) {
        if (AccessToken.getCurrentAccessToken() == null) {
            loginFacebook(context, loginPermissions, new FacebookLoginListner() {
                @Override
                public void onSuccessed(String userId, String token) {
                    login(context, onGetFacebookUserListener);
                }

                @Override
                public void onFailed(FacebookException e) {
                    if (onGetFacebookUserListener != null) {
                        onGetFacebookUserListener.onFailed(e);
                    }
                }
            });
            return;
        }

        GraphRequest newMeRequest = GraphRequest.newMeRequest(AccessToken.getCurrentAccessToken(), new GraphRequest.GraphJSONObjectCallback() {
            @Override
            public void onCompleted(JSONObject jsonObject, GraphResponse graphResponse) {
                MFacebookUser facebookUser = new Gson().fromJson(jsonObject.toString(), MFacebookUser.class);

                if (onGetFacebookUserListener != null) {
                    onGetFacebookUserListener.onGetUserInfo(facebookUser);
                }
            }
        });

        Bundle bundle = new Bundle();
        bundle.putString(GraphRequest.FIELDS_PARAM, "id,email,gender,birthday,name");
        newMeRequest.setParameters(bundle);
        newMeRequest.executeAsync();
    }

    public boolean isNeedLogin() {
        if (AccessToken.getCurrentAccessToken() == null) {
            return true;
        }
        return false;
    }

    // Only friends who installed this app are returned
    public void requestGetFriends(final FacebookFriendListListner onGetFacebookUserListener) {
        AccessToken token = AccessToken.getCurrentAccessToken();
        GraphRequest graphRequest = GraphRequest.newMeRequest(token, new GraphRequest.GraphJSONObjectCallback() {
            @Override
            public void onCompleted(JSONObject jsonObject, GraphResponse graphResponse) {
                ArrayList<String> list = new ArrayList<>();
                try {
                    JSONArray jsonArrayFriends = jsonObject.getJSONObject("friends").getJSONArray("data");
                    for (int i = 0; i < jsonArrayFriends.length(); i++) {
                        JSONObject friendlistObject = jsonArrayFriends.getJSONObject(i);
                        list.add(friendlistObject.getString("id"));
                    }
                } catch (JSONException e) {

                }
                if (onGetFacebookUserListener != null) {
                    onGetFacebookUserListener.onFriendList(list);
                }
            }
        });
        Bundle param = new Bundle();
        param.putString("fields", "friends");
        graphRequest.setParameters(param);
        graphRequest.executeAsync();
    }


    private void loginFacebook(final Activity context, List<String> permisson, final FacebookLoginListner listner) {
        LoginManager.getInstance().logInWithReadPermissions(context, permisson);
        callbackManager = CallbackManager.Factory.create();
        LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                if (listner != null) {
                    listner.onSuccessed(loginResult.getAccessToken().getUserId(), loginResult.getAccessToken().getToken());
                }
            }

            @Override
            public void onCancel() {
                if (listner != null) {
                    listner.onFailed(null);
                }
            }

            @Override
            public void onError(FacebookException e) {
                if (listner != null) {
                    listner.onFailed(e);
                }
                if (e instanceof FacebookAuthorizationException) {
                    if (AccessToken.getCurrentAccessToken() != null) {
                        LoginManager.getInstance().logOut();
                    }
                }
            }
        });
    }

    public void shareImageToFacebook(final Activity activity, Bitmap bitmap) {
        SharePhotoContent shareContent = new SharePhotoContent.Builder().addPhoto(new SharePhoto.Builder().setBitmap(bitmap).build()).build();
        shareDialog = new ShareDialog(activity);
        if (ShareDialog.canShow(SharePhotoContent.class)) {
            callbackManager = CallbackManager.Factory.create();
            shareDialog.registerCallback(callbackManager, new FacebookCallback<Sharer.Result>() {
                public void onSuccess(Sharer.Result obj) {
                    //CommonUtil.showToast(activity, R.string.facebook_share);
                }

                public void onCancel() {
                    // CommonUtil.showToast(activity, R.string.facebook_error_message_cancel_share);
                }

                public void onError(FacebookException facebookException) {
                    // CommonUtil.showToast(activity, R.string.facebook_error_message_isnt_installed);
                }
            });
            shareDialog.show(shareContent);
            return;
        }
        //CommonUtil.showToast(activity, R.string.facebook_error_message_cannot_share);
    }

    public void logout() {
        LoginManager.getInstance().logOut();
    }
}

