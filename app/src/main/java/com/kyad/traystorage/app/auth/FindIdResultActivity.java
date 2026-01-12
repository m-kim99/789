package com.kyad.traystorage.app.auth;

import android.app.Dialog;
import android.content.Intent;

import com.kyad.traystorage.R;
import com.kyad.traystorage.app.common.dialog.LoadingDialog;
import com.kyad.traystorage.databinding.ActivityFindidResultBinding;

import base.BaseBindingActivity;

public class FindIdResultActivity extends BaseBindingActivity<ActivityFindidResultBinding> {

    @Override
    public int getLayout() {
        return R.layout.activity_findid_result;
    }
    @Override
    protected Dialog loadingDialog() {
        return new LoadingDialog(this);
    }

    @Override
    public void init() {
        binding.setActivity(this);
        Intent i = getIntent();
        String loginId = i.getStringExtra("login_id");
        String type = i.getStringExtra("type");
        String showId = loginId;/*.substring(0, Math.min(loginId.length(), 4));
        for (int pad = 0; pad < loginId.length() - 4; pad++)
            showId += "*";*/
        if (type.equals("0")) {
            binding.textResult.setText(showId);
        } else {
            String temp = "";
            if (type.equals("1")) {
                temp = "구글로 가입하셨습니다";
            } else if (type.equals("2")) {
                temp = "네이버로 가입하셨습니다";
            } else if (type.equals("3")) {
                temp = "페북으로 가입하셨습니다";
            } else if (type.equals("5")) {
                temp = "카카오로 가입하셨습니다";
            }
            binding.textResult.setText(temp);
        }

    }

    public void onPassword(){
        Intent intent = new Intent(this, FindAuthActivity.class);
        intent.putExtra("type", "password_reset");
        startActivity(intent);
        finish();
    }
    public void onLogin(){

        finish();
    }
}
