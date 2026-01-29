package com.kyad.traystorage.app.splash;

import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProviders;

//import com.blankj.utilcode.util.AppUtils;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.dynamiclinks.PendingDynamicLinkData;
import com.kyad.traystorage.R;
import com.kyad.traystorage.app.common.Common;
import com.kyad.traystorage.app.common.dialog.AlertDialog;
import com.kyad.traystorage.app.common.dialog.LoadingDialog;
import com.kyad.traystorage.app.common.util.Global;
import com.kyad.traystorage.app.common.util.PrefMgr;
import com.kyad.traystorage.app.setting.FaqActivity;
import com.kyad.traystorage.app.setting.InquiryActivity;
import com.kyad.traystorage.app.setting.NoticeActivity;
import com.kyad.traystorage.app.setting.SettingActivity;
import com.kyad.traystorage.app.setting.TermsActivity;
import com.kyad.traystorage.data.DataManager;
import com.kyad.traystorage.data.model.ModelUser;
import com.kyad.traystorage.databinding.ActivitySplashBinding;

import java.util.List;

import base.BaseBindingActivity;

import static android.content.Context.MODE_PRIVATE;

public class SplashActivity extends BaseBindingActivity<ActivitySplashBinding> {

    @Override
    public int getLayout() {
        return R.layout.activity_splash;
    }

    @Override
    protected Dialog loadingDialog() {
        return null;//new LoadingDialog(this);
    }

    @Override
    public void init() {
        binding.setActivity(this);
        
        // 로고 아이콘 1초 표시
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            // 로고+BETA로 변경
            binding.imgLogo.setImageResource(R.drawable.img_intro_logo_text);
            
            // 1초 더 표시 후 다음 화면
            new Handler(Looper.getMainLooper()).postDelayed(this::handleDeepLink, 1000);
        }, 1000);
    }

    void startApp() {
        //if(true){goTest();return;}
        // 테스트용: 온보딩 강제 표시 이건 테스트용!!
        PrefMgr prefMgr = new PrefMgr(getSharedPreferences(PrefMgr.traystorage_PREFS, MODE_PRIVATE));
        prefMgr.put(PrefMgr.FIRST_START, true);
        
        prefMgr = new PrefMgr(getSharedPreferences(PrefMgr.traystorage_PREFS, MODE_PRIVATE));
        boolean isfirst = prefMgr.getBoolean(PrefMgr.FIRST_START, true);
        if(isfirst)
            new Handler(Looper.getMainLooper()).postDelayed(this::goIntro, 2000);
        else
            new Handler(Looper.getMainLooper()).postDelayed(this::goLoading, 2000);
    }

    void goLoading() {
        Intent intent = new Intent(this, LoadingActivity.class);
        startActivity(intent);
        finish();
    }
    void goIntro() {
        Intent intent = new Intent(this, IntroActivity.class);
        startActivity(intent);
        finish();
    }

    void goPermission() {
        Intent intent = new Intent(this, PermissionActivity.class);
        startActivity(intent);
        finish();
    }

    public void onBackPressed() {}

    private void handleDeepLink() {
        FirebaseDynamicLinks.getInstance()
                .getDynamicLink(getIntent())
                .addOnSuccessListener(this, new OnSuccessListener<PendingDynamicLinkData>() {
                    @Override
                    public void onSuccess(PendingDynamicLinkData pendingDynamicLinkData) {
                        if (pendingDynamicLinkData == null) {
                            Log.d("Cozone", "No have dynamic link");

                            startApp();
                            return;
                        }

                        Uri deepLink = pendingDynamicLinkData.getLink();
                        Log.d("Cozone", " + deepLink");

                        Common.gDocumentID = deepLink.getLastPathSegment();

                        startApp();
                    }
                })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("Cozone", "FirebaseDynamicLinks onFailure");

                        startApp();
                    }
                });
    }
}
