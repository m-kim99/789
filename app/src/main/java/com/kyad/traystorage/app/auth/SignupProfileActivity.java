package com.kyad.traystorage.app.auth;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.View;
import android.widget.DatePicker;
import android.widget.Toast;

import androidx.databinding.ObservableBoolean;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelProviders;

import com.kyad.traystorage.App;
import com.kyad.traystorage.R;
import com.kyad.traystorage.app.common.dialog.AlertDialog;
import com.kyad.traystorage.app.common.dialog.LoadingDialog;
import com.kyad.traystorage.app.common.util.Utils;
import com.kyad.traystorage.app.main.MainActivity;
import com.kyad.traystorage.data.ApiResponse;
import com.kyad.traystorage.data.DataManager;
import com.kyad.traystorage.data.model.ModelBase;
import com.kyad.traystorage.data.model.ModelCode;
import com.kyad.traystorage.data.model.ModelUser;
import com.kyad.traystorage.data.remote.ResponseSubscriber;
import com.kyad.traystorage.databinding.ActivitySignupProfileBinding;

import android.text.format.DateFormat;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;

import base.BaseBindingActivity;
import base.BaseViewModel;
import helper.Util;
import helper.Validation;

public class SignupProfileActivity extends BaseBindingActivity<ActivitySignupProfileBinding> {
    ViewModel viewModel;
    String login_id;
    String password;
    String phoneNumber;
    String signupType;
    public ObservableBoolean isDatePickerShown = new ObservableBoolean(false);

    @Override
    public int getLayout() {
        return R.layout.activity_signup_profile;
    }
    @Override
    protected Dialog loadingDialog() {
        return new LoadingDialog(this);
    }

    @Override
    public void init() {
        login_id = getIntent().getStringExtra("login_id");
        password = getIntent().getStringExtra("password");
        phoneNumber = getIntent().getStringExtra("phoneNumber");
        signupType = getIntent().getStringExtra("type");
        if (signupType == null || signupType.equals("")) {
            signupType = "0";
        }
        initViewModel();
        initView();
        onSex1();
        setupKeyboard(binding.bg);
    }

    private void initViewModel() {
        viewModel = new ViewModel();
    }

    private void initView() {
        binding.setActivity(this);
        binding.setViewModel(this.viewModel);

        viewModel.name.observe(this, name -> {
            checkValidData();
        });
        viewModel.birthday.observe(this, birth -> {
            checkValidData();
        });
        viewModel.email.observe(this, email->{
            binding.deleteEmail.setVisibility(email.isEmpty() ? View.GONE : View.VISIBLE);
            checkValidData();
        });

        binding.editBirthday.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                binding.lineBirthday.setBackgroundResource(b?R.color.black:R.color.light_gray);
            }
        });
        binding.editEmail.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                binding.lineEmail.setBackgroundResource(b?R.color.black:R.color.light_gray);
            }
        });
        binding.editName.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                binding.lineName.setBackgroundResource(b?R.color.black:R.color.light_gray);
            }
        });
    }

    public void onClickBirthDay() {
        if(true){
            String date = viewModel.birthday.getValue().equals("") ? "1990.01.01" : viewModel.birthday.getValue();
            com.kyad.traystorage.app.common.dialog.DatePickerDialog.show(this).setDate(date)
                    .setListener(new com.kyad.traystorage.app.common.dialog.DatePickerDialog.ActionListener() {
                        @Override
                        public void onSelect(String date) {
                            binding.editBirthday.setText(date);
                            checkValidData();
                            isDatePickerShown.set(false);
                        }
                    });
        }else {
            final Calendar calendar = Calendar.getInstance();
            try {
                if (!viewModel.birthday.getValue().isEmpty()) {
                    calendar.setTime(Util.getDate(viewModel.birthday.getValue()));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            DatePickerDialog dlg = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
                @Override
                public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                    calendar.set(year, month, dayOfMonth);
                    String currentDate = (String) DateFormat.format("yyyy.MM.dd", calendar.getTime());
                    binding.editBirthday.setText(currentDate);
                    checkValidData();
                    isDatePickerShown.set(false);
                }

            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
            dlg.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    isDatePickerShown.set(false);
                }
            });
            dlg.show();
            isDatePickerShown.set(true);
        }
    }


    public void onDeleteEmail(){
        binding.editEmail.setText("");
    }

    public void onSex1(){
        binding.buttonSex1.setSelected(true);binding.buttonSex1.setTextColor(getResources().getColor(R.color.focus_color));
        binding.buttonSex2.setSelected(false);binding.buttonSex2.setTextColor(getResources().getColor(R.color.light_gray));
    }
    public void onSex2(){
        binding.buttonSex1.setSelected(false);binding.buttonSex1.setTextColor(getResources().getColor(R.color.light_gray));
        binding.buttonSex2.setSelected(true);binding.buttonSex2.setTextColor(getResources().getColor(R.color.focus_color));
    }

    public void checkValidData() {
//        boolean isEnabled = !viewModel.name.getValue().isEmpty() &&
//                !viewModel.birthday.getValue().isEmpty() &&
//                !viewModel.email.getValue().isEmpty();
        boolean isEnabled = !viewModel.name.getValue().isEmpty() &&
                !viewModel.birthday.getValue().isEmpty();
        binding.nextBtn.setEnabled(isEnabled);
    }

    public void onConfirm(){
        if (binding.editName.length() < 2) {
            Utils.showCustomToast(this, R.string.input_name2, Toast.LENGTH_SHORT);
            return;
        }
        if (!binding.editEmail.getPlanText().equals("") && !Validation.isEmail(binding.editEmail.getPlanText())) {
            Utils.showCustomToast(this, R.string.incorrect_email, Toast.LENGTH_SHORT);
            return;
        }

        viewModel.signup(login_id, password, phoneNumber, viewModel.name.getValue(), viewModel.birthday.getValue().replace(".","-"), binding.buttonSex1.isSelected() ? 0 : 1, viewModel.email.getValue(), signupType);
    }

    public class ViewModel extends BaseViewModel{
        public MutableLiveData<String> birthday = new MutableLiveData<>("");
        public MutableLiveData<String> name = new MutableLiveData<>("");
        public MutableLiveData<String> email = new MutableLiveData<>("");

        public void signup(String loginId, String password, String phone, String name, String birthday, Integer gender, String email, String type) {
            addDisposable(DataManager.get().signup(loginId, password, phone, name, birthday, gender, email, type).subscribeWith(new ResponseSubscriber<ModelUser>() {
                @Override
                public void onComplete() {
                    super.onComplete();

                    if (getResponse().result == 0) {
                        ModelUser user = getResponse().data;
                        user.password = password;
                        DataManager.get().setModel(user);
                        goMain();
                    } else if (!getResponse().msg.isEmpty()) {
                        onApiError(getResponse().msg);
                    } else {
                        onApiError(App.get().getString(R.string.error_network_content));
                    }
                }
            }));
        }
    }

    public void goMain() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }


    @Override
    public void onBackPressed(){
        AlertDialog.show(this).setText(getString(R.string.signup_close), "", getString(R.string.confirm), getString(R.string.cancel))
                .setListener(()->{
                    Intent intent = new Intent(this, LoginHomeActivity.class);
                    startActivity(intent);
                    finish();
                });
    }
}
