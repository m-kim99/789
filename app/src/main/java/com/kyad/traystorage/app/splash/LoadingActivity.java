package com.kyad.traystorage.app.splash;

import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import androidx.lifecycle.ViewModelProviders;

//import com.blankj.utilcode.util.AppUtils;
import com.kyad.traystorage.App;
import com.kyad.traystorage.R;
import com.kyad.traystorage.app.auth.LoginHomeActivity;
import com.kyad.traystorage.app.auth.SignupActivity;
import com.kyad.traystorage.app.auth.SignupProfileActivity;
import com.kyad.traystorage.app.common.dialog.AlertDialog;
import com.kyad.traystorage.app.common.dialog.LoadingDialog;
import com.kyad.traystorage.app.common.util.Utils;
import com.kyad.traystorage.app.main.MainActivity;
import com.kyad.traystorage.app.setting.TermsActivity;
import com.kyad.traystorage.data.DataManager;
import com.kyad.traystorage.data.model.ModelUser;
import com.kyad.traystorage.data.model.ModelVersion;
import com.kyad.traystorage.databinding.ActivityLoadingBinding;

import base.BaseBindingActivity;

//import static com.blankj.utilcode.util.ActivityUtils.startActivity;

public class LoadingActivity extends BaseBindingActivity<ActivityLoadingBinding> {
    LoadingViewModel viewModel;
    private static LoadingActivity instance;
    @Override
    public int getLayout() {
        return R.layout.activity_loading;
    }

    @Override
    protected Dialog loadingDialog() {
        return null;//new LoadingDialog(this);
    }

    @Override
    public void init() {
        binding.setActivity(this);
        instance = this;
        doAnimation();

        initViewModel();
        checkVersion();
    }

    public static LoadingActivity getInstance() {
        return instance;
    }

    void doAnimation(){
        if(this.isDestroyed())
            return;
        new Handler(Looper.getMainLooper()).postDelayed(this::changeIcons, 100);
    }
    int iconIndex = 0;
    void changeIcons(){
        int rids[] = new int[]{R.drawable.loading1, R.drawable.loading2,R.drawable.loading3};
        binding.loading1.setImageResource(rids[iconIndex%3]);
        binding.loading2.setImageResource(rids[(iconIndex+1)%3]);
        binding.loading3.setImageResource(rids[(iconIndex+2)%3]);
        iconIndex++;
        doAnimation();
    }
    /************************************************************
     *  Helpers & Methods
     ************************************************************/

    void initViewModel() {
        viewModel = ViewModelProviders.of(this).get(LoadingViewModel.class);
        viewModel.apiListener = apiListener();
    }

    void checkVersion() {
        viewModel.getVersionInfo();
    }

    void startApp() {
        // 기존 로직 - 로그인 화면으로 이동
        ModelUser user = DataManager.get().getModel(ModelUser.class);
        if (user == null || !user.isAutoLogin) {
            new Handler(Looper.getMainLooper()).postDelayed(this::goLogin, 1000);
        } else {
            viewModel.autoLogin(user.login_id, user.password);
        }
    }

    boolean isError = false;
    /************************************************************
     *  API Listener
     ************************************************************/
    private final LoadingApiListener apiListener() {
        return new LoadingApiListener() {
            @Override
            public void onError(String msg) {
                // 네트워크 오류 시에도 로그인 화면으로 이동 (테스트 모드 사용 가능)
                new Handler(Looper.getMainLooper()).postDelayed(() -> goLogin(), 500);
            }

            @Override
            public void onLoginSuccess() {
                if(DataManager.get().getModel(ModelUser.class).is_agree == 1)
                    goMain();
                else{
                    Intent intent = new Intent(LoadingActivity.this, SignupActivity.class);
                    intent.putExtra("from_login", 1);
                    startActivity(intent);
                    finish();
                }
            }

            @Override
            public void onLoginFailure(Integer status) {

                goLogin();
            }

            @Override
            public void onGetVersionSuccess(ModelVersion lastedVersion) {
                String currentVersion = Utils.getVersion(LoadingActivity.this);
                ModelVersion optionalVersion = DataManager.get().getModel(ModelVersion.class);
                if (!lastedVersion.version.isEmpty() && checkForUpdate(currentVersion, lastedVersion.version)) {
                    AlertDialog dlg = null;
                    if (lastedVersion.require_update == 1) {
                        dlg = AlertDialog.show(LoadingActivity.this)
                                .setText(getString(R.string.update_version), null,
                                        getString(R.string.update), null)
                                .setListener(() -> go2Store(lastedVersion.store_url));

                    } else if (optionalVersion == null || !optionalVersion.version.equals(lastedVersion.version)) {
                        dlg = AlertDialog.show(LoadingActivity.this)
                                .setText(getString(R.string.update_version), null,
                                        getString(R.string.update), getString(R.string.later))
                                .setListener(() -> go2Store(lastedVersion.store_url));
                    }
                    if (dlg != null) {
                        dlg.setOnCancelListener(new DialogInterface.OnCancelListener() {
                            @Override
                            public void onCancel(DialogInterface dialogInterface) {
                                if (lastedVersion.require_update != 1)
                                    DataManager.get().setModel(lastedVersion);
                                startApp();
                            }
                        });
                        dlg.setOnDismissListener(new DialogInterface.OnDismissListener() {
                            @Override
                            public void onDismiss(DialogInterface dialogInterface) {
                                startApp();
                            }
                        });
                    } else {
                        startApp();
                    }
                } else {
                    startApp();
                }
            }
        };
    }

    public static boolean checkForUpdate(String existingVersion, String newVersion) {
        if (existingVersion.isEmpty() || newVersion.isEmpty()) {
            return false;
        }

        existingVersion = existingVersion.replaceAll("\\.", "");
        newVersion = newVersion.replaceAll("\\.", "");

        int existingVersionLength = existingVersion.length();
        int newVersionLength = newVersion.length();

        StringBuilder versionBuilder = new StringBuilder();
        if (newVersionLength > existingVersionLength) {
            versionBuilder.append(existingVersion);
            for (int i = existingVersionLength; i < newVersionLength; i++) {
                versionBuilder.append("0");
            }
            existingVersion = versionBuilder.toString();
        } else if (existingVersionLength > newVersionLength){
            versionBuilder.append(newVersion);
            for (int i = newVersionLength; i < existingVersionLength; i++) {
                versionBuilder.append("0");
            }
            newVersion = versionBuilder.toString();
        }

        return Integer.parseInt(newVersion) > Integer.parseInt(existingVersion);
    }

    void goLogin() {
        Intent intent = new Intent(this, LoginHomeActivity.class);
        startActivity(intent);
        finish();
    }

    void goMain() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        //finish();
        App.get().finishAllActivity();
    }

    void go2Store(String url) {
        //String appUpdateLink = "market://details?id=" + AppUtils.getAppPackageName();
        //String appUpdateWebLink = "http://play.google.com/store/apps/details?id=" + AppUtils.getAppPackageName();

        try {
            startActivity(
                    new Intent(Intent.ACTION_VIEW, Uri.parse(url))
            );
        }catch (Exception e){
            Utils.showCustomToast(this, e.getMessage(), Toast.LENGTH_SHORT);
        }

    }

    public void onBackPressed() {
        if(isError)
            super.onBackPressed();
    }
}
