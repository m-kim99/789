package com.kyad.traystorage.app.setting;

import android.app.Dialog;
import android.view.View;
import android.widget.Toast;

import androidx.databinding.ObservableBoolean;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelProviders;

//import com.blankj.utilcode.util.ToastUtils;
import com.kyad.traystorage.App;
import com.kyad.traystorage.R;
import com.kyad.traystorage.app.common.dialog.AlertDialog;
import com.kyad.traystorage.app.common.dialog.LoadingDialog;
import com.kyad.traystorage.app.common.util.Utils;
import com.kyad.traystorage.data.DataManager;
import com.kyad.traystorage.data.model.ModelAsk;
import com.kyad.traystorage.data.model.ModelBase;
import com.kyad.traystorage.data.remote.ResponseSubscriber;
import com.kyad.traystorage.databinding.ActivityContactusBinding;

import base.BaseBindingActivity;
import base.BaseViewModel;

public class ContactusActivity extends BaseBindingActivity<ActivityContactusBinding> {
    @Override
    public int getLayout() {
        return R.layout.activity_contactus;
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

    private void initView() {
        binding.setActivity(this);
        binding.setModel(viewModel);
    }


    public ViewModel viewModel;
    public class ViewModel extends BaseViewModel {
        public MutableLiveData<String> title = new MutableLiveData<>("");
        public MutableLiveData<String> content = new MutableLiveData<>("");
        public ObservableBoolean isValid = new ObservableBoolean(false);
        public ViewModel(){
            title.observe(ContactusActivity.this, value ->{
                checkValidate();
            });
            content.observe(ContactusActivity.this, value ->{
                checkValidate();
            });
        }

        private void checkValidate() {
            isValid.set(!title.getValue().isEmpty() && !content.getValue().isEmpty());
        }

        public void insertAsk() {
            addDisposable(DataManager.get().insertAsk(
                    title.getValue(),
                    content.getValue())
                    .subscribeWith(new ResponseSubscriber<ModelBase>() {
                @Override
                public void onComplete() {
                    super.onComplete();

                    if (getResponse().result == 0) {
                        Utils.showCustomToast(ContactusActivity.this, (R.string.inquiry_save_confirm));
                        binding.textSubject.setText("");
                        binding.textContent.setText("");
                        finish();
                    } else if (!getResponse().msg.isEmpty()) {
                        onApiError(getResponse().msg);
                    } else {
                        onApiError(App.get().getString(R.string.error_network_content));
                    }
                }
            }));
        }
    }

    void initViewModel(){
        viewModel = new ViewModel();//ViewModelProviders.of(this).get(ViewModel.class);
    }

    public void onConfirm(){
//        if(viewModel.title.getValue().isEmpty()){
//            Utils.showCustomToast(getString(R.string.inquiry_title_input));
//            return;
//        }
//        if(viewModel.content.getValue().isEmpty()){
//            Utils.showCustomToast(getString(R.string.inquiry_content_input));
//            return;
//        }
        if(viewModel.content.getValue().length()<10){
            Utils.showCustomToast(this, (R.string.inquiry_content10_input));
            return;
        }
        AlertDialog.show(this).setText(getString(R.string.inquiry_save), "", getString(R.string.confirm), getString(R.string.cancel))
                .setListener(() -> {
                    viewModel.insertAsk();
                });
    }
}
