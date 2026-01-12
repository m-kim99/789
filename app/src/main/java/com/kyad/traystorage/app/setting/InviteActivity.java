package com.kyad.traystorage.app.setting;

import android.app.Dialog;
import android.content.Intent;

import com.kyad.traystorage.R;
import com.kyad.traystorage.app.common.dialog.LoadingDialog;
import com.kyad.traystorage.databinding.ActivityInviteBinding;

import base.BaseBindingActivity;

public class InviteActivity extends BaseBindingActivity<ActivityInviteBinding> {
    @Override
    public int getLayout() {
        return R.layout.activity_invite;
    }
    @Override
    protected Dialog loadingDialog() {
        return new LoadingDialog(this);
    }

    @Override
    public void init() {
        initView();
    }

    private void initView() {
        binding.setActivity(this);
    }

    public void onConfirm(){

        String appInstallUrl = "https://play.google.com/store/apps/details?id=com.kyad.traystorage";
        String content = "문서관리가 필요해?\nTraystorage으로 해결!\n지금 Traystorage을 설치하고 문서를 안전하게 관리해 보세요!";
        content = content + "\n\n" + appInstallUrl;

        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, content);
        sendIntent.setType("text/plain");

        Intent shareIntent = Intent.createChooser(sendIntent, null);
        startActivity(shareIntent);
    }
}
