package com.kyad.traystorage.app.auth;

import android.app.Dialog;
import android.content.Intent;
import android.view.View;
import android.widget.Toast;

import androidx.databinding.ObservableBoolean;
import androidx.lifecycle.ViewModelProviders;

import com.kyad.traystorage.App;
import com.kyad.traystorage.R;
import com.kyad.traystorage.app.common.dialog.AlertDialog;
import com.kyad.traystorage.app.common.dialog.LoadingDialog;
import com.kyad.traystorage.data.ApiResponse;
import com.kyad.traystorage.data.DataManager;
import com.kyad.traystorage.data.model.ModelBase;
import com.kyad.traystorage.data.model.ModelCode;
import com.kyad.traystorage.data.remote.ResponseSubscriber;
import com.kyad.traystorage.databinding.ActivityChangePasswordBinding;

import base.BaseBindingActivity;
import base.BaseViewModel;
import helper.Validation;

public class ChangePasswordActivity extends BaseBindingActivity<ActivityChangePasswordBinding> {
    ViewModel viewModel;
    String loginId;

    @Override
    public int getLayout() {
        return R.layout.activity_change_password;
    }
    @Override
    protected Dialog loadingDialog() {
        return new LoadingDialog(this);
    }

    @Override
    public void init() {
        initViewModel();
        initView();
        setupKeyboard(binding.bg);
    }

    private void initViewModel() {
        viewModel = new ViewModel();
    }

    private void initView() {
        Intent i = getIntent();
        loginId = i.getStringExtra("login_id");

        binding.setActivity(this);
        binding.editPassword.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                binding.linePassword.setBackgroundResource(b?R.color.black:R.color.light_gray);
            }
        });
        binding.editPassword2.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                binding.linePassword2.setBackgroundResource(b?R.color.black:R.color.light_gray);
            }
        });
    }

    public void onConfirm(){
        viewModel.changePassword(loginId, binding.editPassword.getPlanText());
    }

    public ObservableBoolean isEnabled = new ObservableBoolean(false);
    public void onValidate() {
        isEnabled.set(Validation.isPassword(binding.editPassword.getPlanText())
                && binding.editPassword.getPlanText().equals(binding.editPassword2.getPlanText()));
    }


    public class ViewModel extends BaseViewModel{
        public void changePassword(String login_id, String password) {
            addDisposable(DataManager.get().changePassword(login_id, password).subscribeWith(new ResponseSubscriber<ModelBase>() {
                @Override
                public void onComplete() {
                    super.onComplete();

                    if (getResponse().result == 0) {
                        AlertDialog.show(ChangePasswordActivity.this)
                                .setText(getString(R.string.change_pwd_success), "", getString(R.string.confirm))
                                .setListener(() -> {onBackPressed();});
                    } else if (!getResponse().msg.isEmpty()) {
                        onApiError(getResponse().msg);
                    } else {
                        onApiError(App.get().getString(R.string.error_network_content));
                    }
                }
            }));
        }
    }

}
