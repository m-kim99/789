package com.kyad.traystorage.app.splash;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;

import androidx.annotation.NonNull;

import com.kyad.traystorage.R;
import com.kyad.traystorage.app.common.dialog.LoadingDialog;
import com.kyad.traystorage.app.common.util.PermissionHelper;
import com.kyad.traystorage.app.common.util.PrefMgr;
import com.kyad.traystorage.app.common.util.Utils;
import com.kyad.traystorage.databinding.ActivityPermissionBinding;

import base.BaseBindingActivity;
import helper.Util;
import android.os.Build;
public class PermissionActivity extends BaseBindingActivity<ActivityPermissionBinding> {

    private String[] requiredPermissions = new String[]{
            Manifest.permission.INTERNET,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
    };

    private static final int RC_PERMISSION = 2001;
    private PermissionHelper permissionHelper;

    @Override
    public int getLayout() {
        return R.layout.activity_permission;

    }
    @Override
    protected Dialog loadingDialog() {
        return new LoadingDialog(this);
    }

    @Override
    public void init() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            requiredPermissions = new String[]{
                Manifest.permission.INTERNET,
                Manifest.permission.READ_MEDIA_IMAGES,
                Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED,
                Manifest.permission.CAMERA,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            };
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requiredPermissions = new String[]{
                    Manifest.permission.INTERNET,
                    Manifest.permission.READ_MEDIA_IMAGES,
                    Manifest.permission.CAMERA,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            };
        }
        initView();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_PERMISSION) {
            if (permissionHelper.isPermisionsRevoked(requiredPermissions)) {
                showPermissionGuide(requestCode);
            } else {
                checkPermissions();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == RC_PERMISSION) {
            if (permissionHelper.isPermisionsRevoked(requiredPermissions)) {
                showPermissionGuide(requestCode);
            } else {
                checkPermissions();
            }
        }
    }

    private void showPermissionGuide(final int RC) {
        Utils.showCustomToast(this, (R.string.permission_not_allow));
    }

    private void checkPermissions() {
        if (permissionHelper.hasPermission(requiredPermissions)) {
            PrefMgr prefMgr = new PrefMgr(getSharedPreferences(PrefMgr.traystorage_PREFS, MODE_PRIVATE));
            prefMgr.put(PrefMgr.HAS_PERMISSION, true);
            Intent intent = new Intent(this, IntroActivity.class);
            startActivity(intent);
            finish();
        } else {
            permissionHelper.requestPermission(PermissionActivity.this, requiredPermissions, RC_PERMISSION);
        }
    }

    private void initView() {
        binding.setActivity(this);
    }
    public void onApprove() {
        permissionHelper = new PermissionHelper(this);
        checkPermissions();
    }
}