package com.kyad.traystorage.app.auth;

import android.app.Dialog;
import android.content.Intent;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Toast;

import androidx.databinding.ObservableBoolean;
import androidx.lifecycle.MutableLiveData;

import com.kyad.traystorage.App;
import com.kyad.traystorage.R;
import com.kyad.traystorage.app.common.dialog.AlertDialog;
import com.kyad.traystorage.app.common.dialog.LoadingDialog;
import com.kyad.traystorage.app.common.util.Utils;
import com.kyad.traystorage.app.main.MainActivity;
import com.kyad.traystorage.app.setting.WithdrawResultActivity;
import com.kyad.traystorage.data.ApiResponse;
import com.kyad.traystorage.data.DataManager;
import com.kyad.traystorage.data.model.ModelBase;
import com.kyad.traystorage.data.model.ModelCode;
import com.kyad.traystorage.data.model.ModelUser;
import com.kyad.traystorage.data.remote.ResponseSubscriber;
import com.kyad.traystorage.databinding.ActivityFindAuthBinding;

import base.BaseBindingActivity;
import base.BaseViewModel;

public class FindAuthActivity extends BaseBindingActivity<ActivityFindAuthBinding> {
    ViewModel viewModel;

    public boolean hasUserId = false;
    public boolean showPhoneTitle = false;
    MutableLiveData<Boolean> isAuthSent = new MutableLiveData<>(false);
    private CertDownCounter certDownCounter;
    public String type;
    public String user_id;
    public String pwd = null;

    @Override
    public int getLayout() {
        return R.layout.activity_find_auth;
    }

    @Override
    protected Dialog loadingDialog() {
        return new LoadingDialog(this);
    }

    @Override
    public void init() {
        Intent i = getIntent();
        type = i.getStringExtra("type");
        user_id = i.getStringExtra("user_id");
        if(user_id == null)
            user_id = "";
        if(i.hasExtra("pwd"))
            pwd = i.getStringExtra("pwd");
        binding.editId.setText(user_id);
        showPhoneTitle = hasUserId = !user_id.isEmpty();
        if(type.equals("find_id")){
            hasUserId = true;
            showPhoneTitle = false;
        }

        initViewModel();
        initView();
        setupKeyboard(binding.bg);
    }

    private void initViewModel() {
        viewModel = new ViewModel();
    }

    private void initView() {
        binding.setActivity(this);
        binding.setViewModel(viewModel);
        viewModel.loginID.setValue(user_id);

        binding.editPhonenumber.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                binding.linePhonenumber.setBackgroundResource(b?R.color.black:R.color.light_gray);
            }
        });
        binding.editId.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                binding.lineId.setBackgroundResource(b?R.color.black:R.color.light_gray);
            }
        });
        binding.editCertnumber.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                binding.lineCertnumber.setBackgroundResource(b?R.color.black:R.color.light_gray);
            }
        });

        viewModel.phoneNumber.setValue("");
        isAuthSent.setValue(false);

        viewModel.loginID.observe(this, value->{
            isAuthSent.setValue(false);
            checkValidData();
        });
        viewModel.phoneNumber.observe(this, phone -> {
            isAuthSent.setValue(false);
            checkValidData();
        });
        isAuthSent.observe(this, sent -> {
            binding.textTime.setText("");
            if (!sent && certDownCounter != null)
                certDownCounter.cancel();
            if (sent)
                onStartDownCounter();
            checkValidData();
        });
        viewModel.certNumber.observe(this, cert -> {
            checkValidData();
        });
    }
    public ObservableBoolean isEnabled = new ObservableBoolean(false);
    public void checkValidData(){

        if((type.equals("find_id") || binding.editId.getPlanText().length()>0) && binding.editPhonenumber.getPlanText().length()>0){
            binding.deleteText.setVisibility(View.VISIBLE);
            binding.buttonReq.setVisibility(View.VISIBLE);
            binding.resendButton.setVisibility(View.VISIBLE);
        }else{
            binding.deleteText.setVisibility(View.GONE);
            binding.buttonReq.setVisibility(View.GONE);
            binding.resendButton.setVisibility(View.GONE);
        }
        isEnabled.set(isAuthSent.getValue() && !viewModel.phoneNumber.getValue().isEmpty() && !viewModel.certNumber.getValue().isEmpty());
        if (!hasUserId)
            isEnabled.set(isEnabled.get() && !viewModel.loginID.getValue().isEmpty());
        //binding.nextBtn.setEnabled(isEnabled);
    }
    public void onConfirm(){
        if(viewModel.certNumber.getValue().equals(result.code)){
            onVerifySuccess();
        }else{
            Utils.showCustomToast(this, R.string.cert_wrong);
        }
    }

    public void onResendCode(){
        isAuthSent.setValue(false);

        onAuthReq();
    }
    public void onDeletetext(){
        binding.editPhonenumber.setText("");
    }
    public void onAuthReq(){
        if (viewModel.phoneNumber.getValue().length() < 1) {
            Utils.showCustomToast(this, R.string.input_phone, Toast.LENGTH_SHORT);
            return;
        }
        viewModel.requestCode(binding.editId.getPlanText(), viewModel.phoneNumber.getValue());
    }

    private void onStartDownCounter() {
        if (certDownCounter != null)
            certDownCounter.cancel();
        certDownCounter = new CertDownCounter();
    }

    private class CertDownCounter extends CountDownTimer
    {
        public long secs = 3 * 60;
        public CertDownCounter() {
            super(3 * 60 * 1000, 1000);

            binding.textTime.setText("3:00");
            binding.textTime.setVisibility(View.VISIBLE);

            start();
        }

        @Override
        public void onFinish() {
            secs = 3 * 60;
            binding.textTime.setText("");
            isAuthSent.setValue(false);
        }

        @Override
        public void onTick(long duration) {
            binding.textTime.setText(String.format("%d:%02d", secs / 60, secs % 60));
            secs = secs - 1;
        }
    }

    ModelCode result;
    public class ViewModel extends BaseViewModel{
        public MutableLiveData<String> loginID = new MutableLiveData<>("");
        public MutableLiveData<String> phoneNumber = new MutableLiveData<>("");
        public MutableLiveData<String> certNumber = new MutableLiveData<>("");

        public void requestCode(String login_id, String phone) {
            addDisposable(DataManager.get().requestCodeForFind(login_id, phone).subscribeWith(new ResponseSubscriber<ModelCode>() {
                @Override
                public void onComplete() {
                    super.onComplete();

                    if (getResponse().result == 0) {
                        isAuthSent.setValue(true);
                        result = getResponse().data;
                    } else if (getResponse().result == ApiResponse.API_RESULT_ERROR_USER_NO_EXIST) {
                        AlertDialog.show(FindAuthActivity.this)
                                .setText(App.get().getString(R.string.findid_not_registered), "", getString(R.string.ok))
                                .setListener(() -> {});
                    } else if (!getResponse().msg.isEmpty()) {
                        onApiError(getResponse().msg);
                    } else {
                        onApiError(App.get().getString(R.string.error_network_content));
                    }
                }
            }));
        }

        public void cancelExit(String user_id) {
            addDisposable(DataManager.get().cancelExit(user_id).subscribeWith(new ResponseSubscriber<ModelUser>() {
                @Override
                public void onComplete() {
                    super.onComplete();

                    if (getResponse().result == 0) {
                        ModelUser user = getResponse().data;
                        if(pwd!=null) {
                            user.password = pwd;
                            user.isAutoLogin = true;
                        }
                        DataManager.get().setModel(user);
                        Intent intent = new Intent(FindAuthActivity.this, MainActivity.class);
                        startActivity(intent);
                        App.get().finishAllActivity();
                    } else if (!getResponse().msg.isEmpty()) {
                        onApiError(getResponse().msg);
                    } else {
                        onApiError(App.get().getString(R.string.error_network_content));
                    }
                }

            }));
        }

        public void requestExit() {
            addDisposable(DataManager.get().requestExit().subscribeWith(new ResponseSubscriber<ModelBase>() {
                @Override
                public void onComplete() {
                    super.onComplete();

                    if (getResponse().result == 0) {
                        DataManager.get().removeModel(ModelUser.class);
                        Intent intent = new Intent(FindAuthActivity.this, WithdrawResultActivity.class);
                        intent.putExtra("date", getResponse().data.login_id);
                        startActivity(intent);
                        finish();
                    } else if (getResponse().result == 104) {
                        onApiError(getResponse().msg);
                        Intent intent = new Intent(FindAuthActivity.this, LoginHomeActivity.class);
                        startActivity(intent);
                        App.get().finishAllActivity();
                    } else if (!getResponse().msg.isEmpty()) {
                        onApiError(getResponse().msg);
                    } else {
                        onApiError(App.get().getString(R.string.error_network_content));
                    }
                }
            }));
        }
    }

    public void onVerifySuccess() {
        if(type.equals("find_id")){
            goFindResult(result.login_id, result.signup_type);
        }
        else if(type.equals("password_reset")){
            goPasswordChange();
        }else if(type.equals("exit_cancel")){
            viewModel.cancelExit(user_id);
        }else if(type.equals("exit_request")){
            AlertDialog.show(this).setText(getString(R.string.withdraw_confirm), "", getString(R.string.confirm), getString(R.string.cancel))
                    .setListener(()->{viewModel.requestExit();});
        }
    }
    public void goFindResult(String loginId, String  type) {
        Intent intent = new Intent(this, FindIdResultActivity.class);
        intent.putExtra("login_id", loginId);
        intent.putExtra("type", type);
        startActivity(intent);
        finish();
    }

    public void goPasswordChange() {
        Intent intent = new Intent(this, ChangePasswordActivity.class);
        intent.putExtra("login_id", viewModel.loginID.getValue());
        startActivity(intent);
        finish();
    }
}
