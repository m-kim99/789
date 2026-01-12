package com.kyad.traystorage.app.setting;

import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;

import androidx.databinding.Observable;
import androidx.databinding.ObservableBoolean;
import androidx.databinding.ObservableField;

//import com.blankj.utilcode.util.AppUtils;
import com.kyad.traystorage.App;
import com.kyad.traystorage.R;
import com.kyad.traystorage.app.common.dialog.LoadingDialog;
import com.kyad.traystorage.app.common.util.Utils;
import com.kyad.traystorage.data.DataManager;
import com.kyad.traystorage.data.model.ModelFaq;
import com.kyad.traystorage.data.model.ModelFaqItem;
import com.kyad.traystorage.data.model.ModelVersion;
import com.kyad.traystorage.data.remote.ResponseSubscriber;
import com.kyad.traystorage.databinding.ActivityVersionBinding;

import base.BaseBindingActivity;
import base.BaseViewModel;

public class VersionActivity extends BaseBindingActivity<ActivityVersionBinding> {
    @Override
    public int getLayout() {
        return R.layout.activity_version;
    }
    @Override
    protected Dialog loadingDialog() {
        return new LoadingDialog(this);
    }

    @Override
    public void init() {
        initViewModel();
        initView();
    }

    ModelVersion version;
    public ViewModel viewModel;
    public class ViewModel extends BaseViewModel {
        public ObservableField<String> oldVersion = new ObservableField<String>();
        public ObservableField<String> newVersion = new ObservableField<String>();
        public ObservableBoolean isUpdate = new ObservableBoolean(false);
        public void getVersion() {
            addDisposable(DataManager.get().getVersionInfo().subscribeWith(new ResponseSubscriber<ModelVersion>() {
                @Override
                public void onComplete() {
                    super.onComplete();

                    if (getResponse().result == 0) {
                        version = getResponse().data;
                        String currentVersion = Utils.getVersion(getApplicationContext());
                        ModelVersion optionalVersion = DataManager.get().getModel(ModelVersion.class);
                        oldVersion.set(currentVersion);
                        newVersion.set(version.version);
                        //isUpdate.set(!currentVersion.equals(version.version));
                        isUpdate.set(checkForUpdate(currentVersion, version.version));
                    } else if (!getResponse().msg.isEmpty()) {
                        onApiError(getResponse().msg);
                    } else {
                        onApiError(App.get().getString(R.string.error_network_content));
                    }
                }
            }));
        }
    }

    public static boolean checkForUpdate(String existingVersion, String newVersion) {
        if (existingVersion.isEmpty() || newVersion.isEmpty()) {
            return false;
        }

        existingVersion = existingVersion.replaceAll("\\.", "");
        newVersion = newVersion.replaceAll("\\.", "");

        int existingVersionLength = existingVersion.length();
        int newVersionLength = newVersion.length();

        StringBuilder versionBuilder = new StringBuilder();
        if (newVersionLength > existingVersionLength) {
            versionBuilder.append(existingVersion);
            for (int i = existingVersionLength; i < newVersionLength; i++) {
                versionBuilder.append("0");
            }
            existingVersion = versionBuilder.toString();
        } else if (existingVersionLength > newVersionLength){
            versionBuilder.append(newVersion);
            for (int i = newVersionLength; i < existingVersionLength; i++) {
                versionBuilder.append("0");
            }
            newVersion = versionBuilder.toString();
        }

        return Integer.parseInt(newVersion) > Integer.parseInt(existingVersion);
    }

    private void initViewModel() {
        viewModel = new ViewModel();
        viewModel.getVersion();
    }

    private void initView() {
        binding.setActivity(this);
        binding.setModel(viewModel);
    }

    public void onConfirm(){
        try {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(version.store_url));
            startActivity(browserIntent);
        }catch(Exception e){
            Utils.showCustomToast(this, e.getMessage(), Toast.LENGTH_SHORT);
        }
    }
}
