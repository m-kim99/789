package com.kyad.traystorage.app.auth;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ObservableArrayList;
import androidx.databinding.ObservableBoolean;
import androidx.databinding.ObservableInt;
import androidx.databinding.ViewDataBinding;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.RecyclerView;

//import com.blankj.utilcode.util.ToastUtils;
import com.kyad.traystorage.App;
import com.kyad.traystorage.R;
import com.kyad.traystorage.app.common.dialog.AlertDialog;
import com.kyad.traystorage.app.common.dialog.LoadingDialog;
import com.kyad.traystorage.app.common.util.Utils;
import com.kyad.traystorage.app.main.MainActivity;
import com.kyad.traystorage.app.setting.FaqActivity;
import com.kyad.traystorage.app.setting.TermsActivity;
import com.kyad.traystorage.data.ApiResponse;
import com.kyad.traystorage.data.DataManager;
import com.kyad.traystorage.data.model.ModelAgreement;
import com.kyad.traystorage.data.model.ModelBase;
import com.kyad.traystorage.data.model.ModelCode;
import com.kyad.traystorage.data.model.ModelFaq;
import com.kyad.traystorage.data.model.ModelUser;
import com.kyad.traystorage.data.remote.ResponseSubscriber;
import com.kyad.traystorage.databinding.ActivitySignupBinding;
import com.kyad.traystorage.databinding.ItemAgreementBinding;
import com.kyad.traystorage.databinding.ItemFaqBinding;

import java.util.List;

import base.BaseBindingActivity;
import base.BaseViewModel;
import helper.RecyclerViewHelper;
import helper.Validation;

public class SignupActivity extends BaseBindingActivity<ActivitySignupBinding> {
    ViewModel viewModel;
    boolean isCheckId = false;
    boolean isAuthComplete = false;
    public ObservableInt step = new ObservableInt(0);
    MutableLiveData<Boolean> isAuthSent = new MutableLiveData<>(false);
    private CertDownCounter certDownCounter;

    private String _id = "";
    private String _password = "";
    private String _type = "0";

    @Override
    public int getLayout() {
        return R.layout.activity_signup;
    }

    @Override
    protected Dialog loadingDialog() {
        return new LoadingDialog(this);
    }

    @Override
    public void init() {
        initParams();
        initViewModel();
        initView();
        if(getIntent().hasExtra("from_login")){
            from_login = true;
            step.set(2);
            binding.nextBtn.setText(R.string.complete);
            binding.textIntro.setText(R.string.login_agree);
            binding.allNum.setText("");
            binding.currentNum.setText("");
            binding.stepLayout.setVisibility(View.GONE);
        }
        viewModel.getAllAgrees();
        setupKeyboard(binding.bg);
    }

    public boolean from_login = false;
    private void initViewModel() {
        viewModel = new ViewModel();
    }

    public void onTermAllClick(){
        isAllChecked.set(!isAllChecked.get());
        for(ModelAgreement a:agreeList){
            a.isChecked.set(isAllChecked.get());
        }
        checkValidData();
    }

    void initParams() {
        Bundle extras = getIntent().getExtras();
        if (extras == null) {
            return;
        }

        _id = (String) extras.get("id");
        _password = (String) extras.get("password");
        _type = (String) extras.get("type");
        step.set(1);
    }

    public ObservableBoolean isAllChecked = new ObservableBoolean(false);

    private void initView() {
        binding.setActivity(this);
        binding.setViewModel(viewModel);

        if (_id != null && !"".equals(_id) && _password != null && !"".equals(_password)) {
            viewModel.loginID.setValue(_id);
            viewModel.password.setValue(_password);
            viewModel.type.setValue(_type);
            step.set(1);
        } else {
            step.set(0);
        }

        viewModel.loginID.observe(this, id -> {
            isCheckId = false;
            binding.buttonDoubleCheck.setVisibility(id.isEmpty() ? View.GONE : View.VISIBLE);
            checkValidData();
        });
        viewModel.password.observe(this, pwd -> {
            checkValidData();
        });
        viewModel.password2.observe(this, pwd2->{
            checkValidData();
        });

        viewModel.phoneNumber.observe(this, phone -> {
            if(phone.length()>0){
                binding.deleteText.setVisibility(View.VISIBLE);
                binding.buttonReq.setVisibility(View.VISIBLE);
                binding.resendButton.setVisibility(View.VISIBLE);
            }else{
                binding.deleteText.setVisibility(View.GONE);
                binding.buttonReq.setVisibility(View.GONE);
                binding.resendButton.setVisibility(View.GONE);
            }
            isAuthComplete = false;
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
            binding.buttonCertConfirm.setVisibility(cert.isEmpty() ? View.GONE : View.VISIBLE);
            checkValidData();
        });

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

        binding.editId.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                int length = editable.length();
                if (length > 0 && length < 6) {
                    binding.editId.setError("6자 이상 입력해주세요");
                } else {
                    binding.editId.setError(null);
                }
            }
        });
        binding.editId.setFilters(new InputFilter[] { new InputFilter.LengthFilter(20) });
        binding.editCertnumber.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                binding.lineCertnumber.setBackgroundResource(b?R.color.black:R.color.light_gray);
            }
        });
        binding.editPassword.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                binding.linePassword.setBackgroundResource(b?R.color.black:R.color.light_gray);
            }
        });
        binding.editPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                int length = editable.length();
                if (length > 0 && length < 6) {
                    binding.editPassword.setError("6자 이상 입력해주세요");
                } else {
                    binding.editPassword.setError(null);
                }
            }
        });
        binding.editPassword.setFilters(new InputFilter[] { new InputFilter.LengthFilter(20) });
        binding.editPassword2.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                binding.linePassword2.setBackgroundResource(b?R.color.black:R.color.light_gray);
            }
        });

        binding.agreeList.setAdapter(new ListAdapter());
        RecyclerViewHelper.linkAdapterAndObserable(binding.agreeList.getAdapter(), agreeList);
    }
    public ObservableArrayList<ModelAgreement> agreeList = new ObservableArrayList<>();
    public class ListAdapter extends RecyclerView.Adapter {
        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, final int i) {
            ViewDataBinding itemBinding;
            itemBinding = DataBindingUtil.inflate(LayoutInflater.from(viewGroup.getContext()), R.layout.item_agreement, viewGroup, false);
            RecyclerView.ViewHolder viewHolder = new ListItemViewHolder((ItemAgreementBinding) itemBinding);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
            ((ListItemViewHolder) viewHolder).bindItem(i);

        }

        @Override
        public int getItemCount() {
            return agreeList.size();
        }

        public class ListItemViewHolder extends RecyclerView.ViewHolder {
            ItemAgreementBinding itemBinding;
            ModelAgreement agree;

            public ListItemViewHolder(ItemAgreementBinding itemBinding) {
                super(itemBinding.getRoot());
                this.itemBinding = itemBinding;
                itemBinding.setHolder(this);
            }
            public void onTermClick(){
                agree.isChecked.set(!agree.isChecked.get());

                for(ModelAgreement a:agreeList){
                    if(a.isChecked.get()==false){
                        isAllChecked.set(false);
                        checkValidData();
                        return;
                    }
                }
                isAllChecked.set(true);
                checkValidData();
            }
            public void onTermViewClick(){
                Intent intent = new Intent(SignupActivity.this, TermsActivity.class);
                intent.putExtra("id", agree.id);
                startActivity(intent);
            }

            public void bindItem(int i) {
                this.agree = agreeList.get(i);
                itemBinding.setData(agree);
            }
        }

    }

    public void checkValidData() {
        boolean isEnabled = false;
        isEnabled = !viewModel.loginID.getValue().isEmpty() && !viewModel.password.getValue().isEmpty() &&
                viewModel.password.getValue().equals(viewModel.password2.getValue());        if (step.get() == 0) {

        } else if (step.get() == 1) {
            isEnabled = /*isAuthComplete &&*/!viewModel.phoneNumber.getValue().isEmpty() && !viewModel.certNumber.getValue().isEmpty();
        } else if (step.get() == 2) {
            isEnabled = isAllChecked.get();
        }
        //binding.nextBtn.setEnabled(isEnabled);
        this.isEnabled.set(isEnabled);
    }

    private void onStartDownCounter() {
        if (certDownCounter != null)
            certDownCounter.cancel();
        certDownCounter = new CertDownCounter();
    }

    public ObservableBoolean isEnabled = new ObservableBoolean(false);
    public void onNext(){
       if (step.get()== 0) {
            if (!isCheckId) {
                Utils.showCustomToast(this, R.string.dbl_check_id, Toast.LENGTH_SHORT);
                return;
            }
            if (!Validation.isPassword(viewModel.password.getValue())) {
                Utils.showCustomToast(this, R.string.input_password6, Toast.LENGTH_SHORT);
                return;
            }
           viewModel.phoneNumber.setValue("");
           isAuthSent.setValue(false);

       } else if (step.get() == 1) {
            if (!isAuthComplete) {
                Utils.showCustomToast(this, R.string.verify_phone, Toast.LENGTH_SHORT);
                return;
            }
       } else if (step.get() == 2) {
           if(from_login){
               viewModel.agreeTerms();
               return;
           }
           AlertDialog.show(SignupActivity.this)
                   .setText(getString(R.string.signup_comp), "", getString(R.string.confirm), getString(R.string.cancel))
                   .setListener(() -> {
                       goProfile();
                   });
           return;
       }

        step.set(step.get() + 1);
        checkValidData();
        binding.currentNum.setText("" + (step.get() + 1));
    }

    public void onIdDoubleCheck(){
        if (binding.editId.length() < 4) {
            Utils.showCustomToast(this, R.string.input_id4, Toast.LENGTH_SHORT);
            return;
        }

        viewModel.checkId(binding.editId.getPlanText());
    }

    public void onResendCode(){
        isAuthComplete = false;
        isAuthSent.setValue(false);
        onAuthReq();
    }
    public void onDeletetext(){
        binding.editPhonenumber.setText("");
    }
    public void onAuthReq(){
//        if (binding.editPhonenumber.getText().length() < 1) {
//            Utils.showCustomToast(this, R.string.input_phone, Toast.LENGTH_LONG);
//            return;
//        }
        viewModel.requestCode(viewModel.phoneNumber.getValue());
    }

    public void onAuthConfirm(){
        if(viewModel.certNumber.getValue().equals(codeResult.code)){
            onVerifySuccess();
        }else{
            Utils.showCustomToast(this, R.string.cert_wrong);
        }
    }

    public void onTermViewClick(Integer nType) {
        Intent intent = new Intent(this, TermsActivity.class);
        intent.putExtra("type", nType);
        startActivity(intent);
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
            isAuthComplete = false;
            binding.textTime.setText("");
            isAuthSent.setValue(false);
        }

        @Override
        public void onTick(long duration) {
            binding.textTime.setText(String.format("%d:%02d", secs / 60, secs % 60));
            secs = secs - 1;
        }
    }

    ModelCode codeResult;
    public class ViewModel extends BaseViewModel{
        public MutableLiveData<String> loginID = new MutableLiveData<>("");
        public MutableLiveData<String> phoneNumber = new MutableLiveData<>("");
        public MutableLiveData<String> certNumber = new MutableLiveData<>("");
        public MutableLiveData<String> password = new MutableLiveData<>("");
        public MutableLiveData<String> password2 = new MutableLiveData<>("");
        public MutableLiveData<String> name = new MutableLiveData<>("");
        public MutableLiveData<String> type = new MutableLiveData<>("");

        public void requestCode(String phone) {
            addDisposable(DataManager.get().requestCodeForSignup(phone).subscribeWith(new ResponseSubscriber<ModelCode>() {
                @Override
                public void onComplete() {
                    super.onComplete();

                    if (getResponse().result == 0) {
                        codeResult = getResponse().data;
                        onSendAuthRequestSuccess();
                    } else if (getResponse().result == ApiResponse.API_RESULT_ERROR_PHONE_DUPLICATED) {
                        onPhoneNumberDuplicated();
                    } else if (!getResponse().msg.isEmpty()) {
                        onApiError(getResponse().msg);
                    } else {
                        onApiError(App.get().getString(R.string.error_network_content));
                    }
                }
            }));
        }
        public void checkId(String loginId) {
            addDisposable(DataManager.get().checkLoginId(loginId).subscribeWith(new ResponseSubscriber<ModelBase>() {
                @Override
                public void onComplete() {
                    super.onComplete();

                    if (getResponse().result == 0) {
                        onCheckIdSuccess();
                    } else if (getResponse().result == ApiResponse.API_RESULT_ERROR_ID_DUPLICATED) {
                        onCheckIdFail();
                    } else if (!getResponse().msg.isEmpty()) {
                        onApiError(getResponse().msg);
                    } else {
                        onApiError(App.get().getString(R.string.error_network_content));
                    }
                }
            }));
        }
        public void getAllAgrees(){
            addDisposable(DataManager.get().getAgreeList(from_login?DataManager.get().getModel(ModelUser.class).access_token:"").subscribeWith(new ResponseSubscriber<ModelAgreement.ListModel>() {
                @Override
                public void onComplete() {
                    super.onComplete();

                    if (getResponse().result == 0) {
                        agreeList.clear();
                        agreeList.addAll(getResponse().data.list);
                    }else if (!getResponse().msg.isEmpty()) {
                        onApiError(getResponse().msg);
                    }  else {
                        onApiError(App.get().getString(R.string.error_network_content));
                    }
                }
            }));
        }

        public void agreeTerms() {

            addDisposable(DataManager.get().agreeTerms().subscribeWith(new ResponseSubscriber<ModelBase>() {
                @Override
                public void onComplete() {
                    super.onComplete();

                    if (getResponse().result == 0) {
                        Intent intent = new Intent(SignupActivity.this, MainActivity.class);
                        startActivity(intent);
                        App.get().finishAllActivity();
                    }else if (!getResponse().msg.isEmpty()) {
                        onApiError(getResponse().msg);
                    }  else {
                        onApiError(App.get().getString(R.string.error_network_content));
                    }
                }
            }));
        }
    }
    public void onError(String msg) {
        AlertDialog.show(SignupActivity.this)
                .setText(msg, "", getString(R.string.ok))
                .setListener(() -> {});
    }
    public void onSendAuthRequestSuccess() {
        isAuthSent.setValue(true);
    }
    public void onPhoneNumberDuplicated() {
        AlertDialog.show(SignupActivity.this)
                .setText(App.get().getString(R.string.authreq_dup), "", getString(R.string.ok), getString(R.string.cancel))
                .setListener(() -> {
                    Intent intent = new Intent(SignupActivity.this, FindAuthActivity.class);
                    intent.putExtra("type", "find_id");
                    startActivity(intent);
                    finish();
                });
    }
    public void onCheckIdFail() {
        isCheckId = false;
        Utils.showCustomToast(this,(R.string.id_duplicated));
    }
    public void onCheckIdSuccess() {
        isCheckId = true;
        Utils.showCustomToast(this, (R.string.id_available));
    }
    public void onVerifySuccess() {
        isAuthSent.setValue(false);
        isAuthComplete = true;
        Utils.showCustomToast(SignupActivity.this, R.string.verify_comp, Toast.LENGTH_SHORT);
        checkValidData();
    }
    public void goProfile()
    {
        Intent intent = new Intent(this, SignupProfileActivity.class);
        intent.putExtra("login_id", viewModel.loginID.getValue());
        intent.putExtra("password", viewModel.password.getValue());
        intent.putExtra("phoneNumber", viewModel.phoneNumber.getValue());
        intent.putExtra("type", viewModel.type.getValue());
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed(){
        if(from_login){
            super.onBackPressed();
            return;
        }
        AlertDialog.show(this).setText(getString(R.string.signup_close), "", getString(R.string.confirm), getString(R.string.cancel))
                .setListener(()->{
                    Intent intent = new Intent(this, LoginHomeActivity.class);
                    startActivity(intent);
                    finish();
                });
    }
}
