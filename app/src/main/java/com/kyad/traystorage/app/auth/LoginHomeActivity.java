package com.kyad.traystorage.app.auth;

import android.app.Dialog;
import android.content.Intent;
import android.os.Handler;

import androidx.annotation.Nullable;

import com.facebook.FacebookException;
import com.kyad.traystorage.App;
import com.kyad.traystorage.R;
import com.kyad.traystorage.app.common.dialog.LoadingDialog;
import com.kyad.traystorage.app.common.util.Utils;
import com.kyad.traystorage.app.main.MainActivity;
import com.kyad.traystorage.data.DataManager;
import com.kyad.traystorage.data.LocalStorageManager;
import com.kyad.traystorage.data.model.ModelUser;
import com.kyad.traystorage.data.remote.ResponseSubscriber;
import com.kyad.traystorage.databinding.ActivityLoginHomeBinding;
import com.kyad.traystorage.service.GoogleUtils;
import com.kyad.traystorage.service.KakaoLoginUtils;
import com.kyad.traystorage.service.NaverLoginUtils;
import com.kyad.traystorage.service.facebook.FacebookUtils;
import com.kyad.traystorage.service.facebook.model.MFacebookUser;

import base.BaseBindingActivity;
import base.BaseViewModel;
import helper.Util;

public class LoginHomeActivity extends BaseBindingActivity<ActivityLoginHomeBinding> {
    ViewModel viewModel;
    NaverLoginUtils naverUtils = null;
    KakaoLoginUtils kakaoUtils = null;
    GoogleUtils googleUtils = null;
    FacebookUtils facebookUtils = null;

    @Override
    public int getLayout() {
        return R.layout.activity_login_home;
    }

    @Override
    protected Dialog loadingDialog() {
        return new LoadingDialog(this);
    }

    @Override
    public void init() {
        initViewModel();
        initView();

        naverUtils = new NaverLoginUtils(this);
        kakaoUtils = KakaoLoginUtils.getInstance(this);
        googleUtils = new GoogleUtils(this);
        facebookUtils = new FacebookUtils(this);
    }

    private void initView() {
        binding.setActivity(this);
    }

    private void initViewModel() {
        viewModel = new ViewModel();
    }

    //click event
    public void goLogin() {
        Intent intent = new Intent(this, SigninActivity.class);
        startActivity(intent);
        finish();
    }

    public void goSignUp() {
        Intent intent = new Intent(this, SignupActivity.class);
        startActivity(intent);
        finish();
    }

    // 테스트 모드: 로그인 없이 메인으로 이동
    public void goTestMode() {
        // 더미 유저 데이터 생성
        ModelUser testUser = new ModelUser();
        testUser.id = 999;
        testUser.login_id = "test_user";
        testUser.name = "테스트유저";
        testUser.access_token = "test_access_token_12345";
        testUser.phone_number = "01012345678";
        testUser.email = "test@test.com";
        testUser.isAutoLogin = false;
        testUser.is_agree = 1;
        
        // 더미 유저 저장
        DataManager.get().setModel(testUser);
        
        // 로컬 저장소 초기화 및 기본 카테고리 생성
        LocalStorageManager.get().initDefaultCategory();
        
        // 메인으로 이동
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    public void goSnsSignup(Integer type) {
        if (type == 0) {
            //kakao
            kakaoUtils.login(new KakaoLoginUtils.LoginListner() {
                @Override
                public void onSucceed(String userId, KakaoLoginUtils.UserInfo userInfo) {
                    viewModel.login(userInfo.getId(), userInfo.getId(), "5",true);
                }

                @Override
                public void onFailed(String error) {
                    Utils.showCustomToast(LoginHomeActivity.this, R.string.kakao_login_failed);
                }
            });
        } else if (type == 1) {
            //google
            googleUtils.login(LoginHomeActivity.this, new GoogleUtils.LoginListener() {
                @Override
                public void onSuccessed(GoogleUtils.UserInfo userInfo) {
                    if (userInfo == null) {
                        Utils.showCustomToast(LoginHomeActivity.this, R.string.google_login_failed);
                        return;
                    }
                    String userid = String.valueOf(userInfo.getId());
                    viewModel.login(userid, userid, "1", true);
                }

                @Override
                public void onFailed(int error) {
                    Utils.showCustomToast(LoginHomeActivity.this, R.string.google_login_failed);
                }
            });
        } else if (type == 2) {
            //facebook
            facebookUtils.login(LoginHomeActivity.this, new FacebookUtils.FacebookUserInfoListner() {
                @Override
                public void onGetUserInfo(MFacebookUser userInfo) {
                    if (userInfo == null) {
                        Utils.showCustomToast(LoginHomeActivity.this, R.string.facebook_login_failed);
                        return;
                    }

                    String userid = String.valueOf(userInfo.id);
                    viewModel.login(userid, userid, "3",true);
                }

                @Override
                public void onFailed(FacebookException e) {
                    Utils.showCustomToast(LoginHomeActivity.this, R.string.facebook_login_failed);
                }
            });
        } else if (type == 3) {
            //naver
            naverUtils.login(LoginHomeActivity.this, new NaverLoginUtils.LoginListener() {
                @Override
                public void onSuccessed(NaverLoginUtils.UserInfo userInfo) {
                    //viewModel.login(userInfo.getEmail(), userInfo.getId(), login_type);
                    //Util.showToast(LoginActivity.this, "Naver Login=>" + userInfo.getId());
                    String userid = userInfo.getId();
                    viewModel.login(userid, userid, "2", true);
                }

                @Override
                public void onFailed(String error) {
                    Utils.showCustomToast(LoginHomeActivity.this, R.string.naver_login_failed);
                }
            });
        }
    }


    private boolean bFinish = false;

    @Override
    public void onBackPressed() {
        if (!bFinish) {
            bFinish = true;
            Utils.showCustomToast(this, R.string.app_finish_message);
            new Handler().postDelayed(() -> bFinish = false, 2000);
        } else {
            finish();
        }
    }

    public class ViewModel extends BaseViewModel {
        public void login(String id, String pwd, String type, boolean isAutoLogin) {
            addDisposable(DataManager.get().login(id, pwd).subscribeWith(new ResponseSubscriber<ModelUser>() {
                @Override
                public void onComplete() {
                    super.onComplete();

                    if (getResponse().result == 0) {
                        ModelUser user = getResponse().data;
                        user.password = pwd;
                        user.isAutoLogin = isAutoLogin;
                        DataManager.get().setModel(user);

                        Intent intent = new Intent(LoginHomeActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    } else if (getResponse().result > 200) {
                        Intent intent = new Intent(LoginHomeActivity.this, SignupActivity.class);
                        intent.putExtra("id", id);
                        intent.putExtra("password", pwd);
                        intent.putExtra("type", type);
                        startActivity(intent);
                        finish();
                    } else if (!getResponse().msg.isEmpty()) {
                        onApiError(getResponse().msg);
                    } else {
                        onApiError(App.get().getString(R.string.error_network_content));
                    }
                }
            }));
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (googleUtils != null && googleUtils.onActivityResult(requestCode, resultCode, data)) {
            return;
        }

        if (facebookUtils != null && facebookUtils.onActivityResult(requestCode, resultCode, data)) {
            return;
        }
    }
}
