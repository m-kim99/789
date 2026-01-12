package com.kyad.traystorage.app.setting;

import android.app.Dialog;
import android.content.Intent;

import com.kyad.traystorage.App;
import com.kyad.traystorage.R;
import com.kyad.traystorage.app.auth.LoginHomeActivity;
import com.kyad.traystorage.app.common.dialog.LoadingDialog;
import com.kyad.traystorage.app.main.MainActivity;
import com.kyad.traystorage.databinding.ActivityWithdrawResultBinding;

import base.BaseBindingActivity;

public class WithdrawResultActivity extends BaseBindingActivity<ActivityWithdrawResultBinding> {
    @Override
    public int getLayout() {
        return R.layout.activity_withdraw_result;
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
        String date = getIntent().getStringExtra("date");
        binding.textDate.setText(getResources().getString(R.string.withdrawal3) + date);
    }

    public void onConfirm(){
        Intent intent = new Intent(this, LoginHomeActivity.class);
        startActivity(intent);
        App.get().finishAllActivity();
    }

    @Override
    public void onBackPressed() {
        onConfirm();
    }
}
