package com.kyad.traystorage.app.setting;

import android.app.Dialog;

import androidx.lifecycle.MutableLiveData;

import com.kyad.traystorage.App;
import com.kyad.traystorage.R;
import com.kyad.traystorage.app.common.dialog.LoadingDialog;
import com.kyad.traystorage.data.DataManager;
import com.kyad.traystorage.data.RemoteDataSource;
import com.kyad.traystorage.data.model.ModelAgreement;
import com.kyad.traystorage.data.model.ModelUser;
import com.kyad.traystorage.data.remote.ResponseSubscriber;
import com.kyad.traystorage.databinding.ActivityTermsBinding;

import base.BaseBindingActivity;
import base.BaseViewModel;

public class TermsActivity extends BaseBindingActivity<ActivityTermsBinding> {
    Integer id = 0;
    @Override
    public int getLayout() {
        return R.layout.activity_terms;
    }
    @Override
    protected Dialog loadingDialog() {
        return new LoadingDialog(this);
    }

    @Override
    public void init() {
        id = getIntent().getIntExtra("id", 0);
        initView();
    }

    private void initView() {
        binding.setActivity(this);
        new ViewModel().getDetail(id);
    }
    public class ViewModel extends BaseViewModel {
        public MutableLiveData<String> birthday = new MutableLiveData<>("");
        public MutableLiveData<String> name = new MutableLiveData<>("");
        public MutableLiveData<String> email = new MutableLiveData<>("");

        public void getDetail(Integer id) {
            addDisposable(DataManager.get().getAgreeDetail(id).subscribeWith(new ResponseSubscriber<ModelAgreement>() {
                @Override
                public void onComplete() {
                    super.onComplete();

                    if (getResponse().result == 0) {
                        ModelAgreement user = getResponse().data;
                        binding.title.setText(user.title);
                        //binding.webView.loadData(user.content, "text/html", "UTF-8");
                        binding.webView.loadDataWithBaseURL(null, user.content, "text/html; charset=utf-8", "utf8", null);
                    } else if (!getResponse().msg.isEmpty()) {
                        onApiError(getResponse().msg);
                    } else {
                        onApiError(App.get().getString(R.string.error_network_content));
                    }
                }
            }));
        }
    }
    public void onConfirm(){}
}
