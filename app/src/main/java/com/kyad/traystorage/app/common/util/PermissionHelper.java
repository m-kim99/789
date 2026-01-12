package com.kyad.traystorage.app.common.util;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class PermissionHelper {
    protected static final int PERM_CAMERA = 1;
    protected static final int PERM_GALLERY = 2;

    protected static final int RC_PERMISSION_CAMERA = 1000;
    protected static final int RC_PERMISSION_GALLERY = 1001;
    protected static final int RC_PERMISSION_DOWNLOAD = 1006;
    protected static final int RC_PHOTO_SELECT = 1003;
    protected static final int RC_ELECT = 1004;

    protected static final String[] Permission_Camera = {
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    protected static final String[] Permission_Gallery = {
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
    };

    private final Activity mActivity;

    public PermissionHelper(Activity activity) {
        mActivity = activity;
    }

    public boolean hasPermission(String permission) {
        return ContextCompat.checkSelfPermission(mActivity, permission) == PackageManager.PERMISSION_GRANTED;
    }

    public boolean hasPermission(String[] permissions) {
        for (String permission : permissions)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                if (permission.equals(Manifest.permission.READ_MEDIA_IMAGES) ||
                permission.equals(Manifest.permission.READ_MEDIA_IMAGES)) {
                    if (PackageManager.PERMISSION_GRANTED != ContextCompat.checkSelfPermission(mActivity, permission)
                        && PackageManager.PERMISSION_GRANTED != ContextCompat.checkSelfPermission(mActivity,
                    Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED)) {
                        return false;
                    }
                } else {
                    if (PackageManager.PERMISSION_GRANTED != ContextCompat.checkSelfPermission(mActivity, permission))
                        return false;
                }
            } else if (PackageManager.PERMISSION_GRANTED != ContextCompat.checkSelfPermission(mActivity, permission))
                return false;
        return true;
    }

    public boolean isPermisionsRevoked(String[] permissions) {
        boolean isRevoked = false;

        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(mActivity, permission) == PackageManager.PERMISSION_DENIED &&
                    ActivityCompat.shouldShowRequestPermissionRationale(mActivity, permission) == false) {
                isRevoked = true;
                break;
            }
        }
        return isRevoked;
    }

    public void requestPermission(Activity p_context, String[] p_requiredPermissions, int requestCode) {
        ActivityCompat.requestPermissions(p_context, p_requiredPermissions, requestCode);
    }
}
