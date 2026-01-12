package com.kyad.traystorage.service;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.util.Log;

import com.kakao.sdk.auth.model.OAuthToken;
import com.kakao.sdk.common.KakaoSdk;
import com.kakao.sdk.common.util.KakaoCustomTabsClient;
import com.kakao.sdk.common.util.Utility;
import com.kakao.sdk.link.LinkClient;
import com.kakao.sdk.link.WebSharerClient;
import com.kakao.sdk.network.UtilityKt;
import com.kakao.sdk.template.model.Button;
import com.kakao.sdk.template.model.Content;
import com.kakao.sdk.template.model.FeedTemplate;
import com.kakao.sdk.template.model.Link;
import com.kakao.sdk.user.UserApiClient;
import com.kakao.sdk.user.model.Account;
import com.kyad.traystorage.R;
import com.kyad.traystorage.app.common.util.Utils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import helper.Util;
import kotlin.Unit;
import kotlin.jvm.functions.Function2;
import lombok.Data;
import lombok.EqualsAndHashCode;

public class KakaoLoginUtils {
    public final static String TAG = "KakaoUtils";
    public final static String KAKAO_KEY = "98498c102944c0477c37fcaa25a98fa7";
    public final static String ERR_NOT_SIGNED = "err_not_signed";

    private static KakaoLoginUtils mKakaoUtils = null;

    public static KakaoLoginUtils getInstance(Context context) {
        if (mKakaoUtils == null) {
            mKakaoUtils = new KakaoLoginUtils(context);
        }

        return mKakaoUtils;
    }

    public static void shareKakao(Context context, String text) {
        Intent sharingIntent = new Intent(Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");        // 고정 text
        sharingIntent.putExtra(Intent.EXTRA_TEXT, text);
        sharingIntent.setPackage("com.kakao.talk");    // 고정 text
        context.startActivity(sharingIntent);
    }

    public static boolean isInstalledPackage(Context context, String packageName) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("image/*");

        List<ResolveInfo> resInfo = context.getPackageManager().queryIntentActivities(intent, 0);
        if (resInfo.isEmpty()) {
            return false;
        }

        List<Intent> shareIntentList = new ArrayList<Intent>();
        boolean isShare = false;
        for (ResolveInfo info : resInfo) {
            Intent shareIntent = (Intent) intent.clone();

            if (info.activityInfo.packageName.toLowerCase().equals(packageName)) {
                isShare = true;
                break;
            }
        }

        return isShare;
    }

    // parmas는 blog_key=%s 형식임.
    public static void shareKakao(Context context, String title, String content, String img_url, String web_url, Map<String, String> params) {
        if (isInstalledPackage(context, "com.kakao.talk") == false) {
            Utils.showCustomToast(context, R.string.kakao_install_error);
            return;
        }

        if (web_url == null || web_url.isEmpty() == true) {
            web_url = "https://developers.kakao.com";
        }

        //if(img_url == null || img_url.isEmpty() == true) {
        //img_url = BuildConfig.SERVER_URL + "assets/images/djb_logo.jpg";
        //}

        List<Button> buttons = new ArrayList<>();
        buttons.add(new Button(context.getResources().getString(R.string.app_name), new Link(web_url, web_url, params, params)));
        FeedTemplate feedTemplate = new FeedTemplate(
                new Content(title, img_url, new Link(web_url, web_url, params, params), content),
                null, null, buttons);

        if (LinkClient.getInstance().isKakaoLinkAvailable(context)) {
            LinkClient.getInstance().defaultTemplate(context, feedTemplate, ((linkResult, error) -> {
                if (error != null) {
                    Log.e(TAG, "카카오링크 보내기 실패", error);
                } else if (linkResult != null) {
                    Log.d(TAG, "카카오링크 보내기 성공 ${linkResult.intent}");
                    context.startActivity(linkResult.getIntent());

                    // 카카오링크 보내기에 성공했지만 아래 경고 메시지가 존재할 경우 일부 컨텐츠가 정상 동작하지 않을 수 있습니다.
                    Log.w(TAG, "Warning Msg: ${linkResult.warningMsg}");
                    Log.w(TAG, "Argument Msg: ${linkResult.argumentMsg}");
                }
                return null;
            }));
        } else {
            Uri sharerUrl = WebSharerClient.getInstance().defaultTemplateUri(feedTemplate);
            // 1. CustomTabs으로 Chrome 브라우저 열기
            try {
                KakaoCustomTabsClient.INSTANCE.openWithDefault(context, sharerUrl);
            } catch (UnsupportedOperationException e) {
                // Chrome 브라우저가 없을 때 예외처리
                e.printStackTrace();
            }

            // 2. CustomTabs으로 디바이스 기본 브라우저 열기
            try {
                KakaoCustomTabsClient.INSTANCE.open(context, sharerUrl);
            } catch (ActivityNotFoundException e) {
                // 인터넷 브라우저가 없을 때 예외처리
                e.printStackTrace();
            }
        }
    }

    private Context mContext;
    private LoginListner mLoginListener;
    private String mLoginedID = "";

    KakaoLoginUtils(Context context) {
        KakaoSdk.init(context, KAKAO_KEY);

        mContext = context;
    }

    public void login(LoginListner loginListner) {
        mLoginListener = loginListner;
        String keyHash = Utility.INSTANCE.getKeyHash(mContext);
        Log.d("Keyhash", keyHash);

        loginKakao((token, error) -> {
            if (error != null) {
                if (mLoginListener != null) {
                    mLoginListener.onFailed(error.getMessage());
                }
            } else if (token != null) {
                Log.i(TAG, "로그인 성공" + token.getAccessToken());
                requestMe();
            }
            return null;
        });
    }

    private void loginKakao(Function2<OAuthToken, Throwable, Unit> callback) {
        if (UserApiClient.getInstance().isKakaoTalkLoginAvailable(mContext)) {
            UserApiClient.getInstance().loginWithKakaoTalk(mContext, callback);
        } else {
            UserApiClient.getInstance().loginWithKakaoAccount(mContext, callback);
        }
    }

    private void requestMe() {
        UserApiClient.getInstance().me(true, (user, error) -> {
            if (error != null) {
                Log.e(TAG, "사용자 정보 요청 실패", error);
                if (mLoginListener != null) {
                    mLoginListener.onFailed(error.getMessage());
                }
            } else if (user != null) {
                UserInfo userInfo = new UserInfo();
                userInfo.setId(user.getId() + "");

                Account result = user.getKakaoAccount();
                if(result != null) {
                    if (result.getProfile() != null) {
                        userInfo.setNickname(result.getProfile().getNickname());
                        userInfo.setProfile_image(result.getProfile().getProfileImageUrl());
                    }
                    userInfo.setEmail(result.getEmail());
                    userInfo.setBirthday(result.getBirthday());
                    if (result.getGender() != null)
                        userInfo.setGender(result.getGender().name());
                    else
                        userInfo.setGender("");
                }

                if (mLoginListener != null) {
                    mLoginListener.onSucceed(mLoginedID, userInfo);
                }
            }
            return null;
        });
    }

    public void logout() {
        UserApiClient.getInstance().logout((error) -> {
            if (error != null) {
                Log.e(TAG, "로그아웃 실패. SDK에서 토큰 삭제됨", error);
            } else {
                Log.i(TAG, "로그아웃 성공. SDK에서 토큰 삭제됨");
            }
            return null;
        });
    }

    @Data
    @EqualsAndHashCode(callSuper = false)
    public static class UserInfo implements Serializable {
        private String id;
        private String nickname;
        private String birthday;
        private String gender;
        private String email;
        private String profile_image;

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

        public String getBirthday() {
            return birthday;
        }

        public void setBirthday(String birthday) {
            this.birthday = birthday;
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

        public String getProfile_image() {
            return profile_image;
        }

        public void setProfile_image(String profile_image) {
            this.profile_image = profile_image;
        }
    }

    public interface LoginListner {
        void onSucceed(String userId, UserInfo userInfo);

        void onFailed(String error);
    }
}
