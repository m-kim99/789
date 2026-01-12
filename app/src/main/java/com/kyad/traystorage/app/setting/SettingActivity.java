package com.kyad.traystorage.app.setting;

import android.app.Dialog;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ObservableArrayList;
import androidx.databinding.ViewDataBinding;
import androidx.lifecycle.MutableLiveData;
import androidx.recyclerview.widget.RecyclerView;

//import com.blankj.utilcode.util.AppUtils;
import com.kyad.traystorage.App;
import com.kyad.traystorage.R;
import com.kyad.traystorage.app.auth.FindAuthActivity;
import com.kyad.traystorage.app.auth.LoginHomeActivity;
import com.kyad.traystorage.app.auth.SignupActivity;
import com.kyad.traystorage.app.common.dialog.AlertDialog;
import com.kyad.traystorage.app.common.dialog.LoadingDialog;
import com.kyad.traystorage.app.common.util.Utils;
import com.kyad.traystorage.app.main.MainActivity;
import com.kyad.traystorage.data.ApiResponse;
import com.kyad.traystorage.data.DataManager;
import com.kyad.traystorage.data.model.ModelAgreement;
import com.kyad.traystorage.data.model.ModelBase;
import com.kyad.traystorage.data.model.ModelCode;
import com.kyad.traystorage.data.model.ModelUser;
import com.kyad.traystorage.data.remote.ResponseSubscriber;
import com.kyad.traystorage.databinding.ActivitySettingBinding;
import com.kyad.traystorage.databinding.ItemAgreementBinding;
import com.kyad.traystorage.databinding.ItemSettingAgreeBinding;

import base.BaseBindingActivity;
import base.BaseViewModel;
import helper.RecyclerViewHelper;
import lombok.Data;

public class SettingActivity extends BaseBindingActivity<ActivitySettingBinding> {
    @Override
    public int getLayout() {
        return R.layout.activity_setting;
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
        binding.editId.setText(DataManager.get().getModel(ModelUser.class).login_id);
        binding.textVersion.setText(Utils.getVersion(this));
        int signupType = DataManager.get().getModel(ModelUser.class).signup_type;
        if (signupType == 0) {
            binding.llyAccountInfo.setVisibility(View.VISIBLE);
        } else {
            binding.llyAccountInfo.setVisibility(View.GONE);
        }

        binding.agreeList.setAdapter(new ListAdapter());
        RecyclerViewHelper.linkAdapterAndObserable(binding.agreeList.getAdapter(), agreeList);
        new ViewModel().getAllAgrees();

    }
    public class ViewModel extends BaseViewModel {
        public void getAllAgrees(){
            addDisposable(DataManager.get().getAgreeList("").subscribeWith(new ResponseSubscriber<ModelAgreement.ListModel>() {
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

    }
    public ObservableArrayList<ModelAgreement> agreeList = new ObservableArrayList<>();
    public class ListAdapter extends RecyclerView.Adapter {
        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, final int i) {
            ViewDataBinding itemBinding;
            itemBinding = DataBindingUtil.inflate(LayoutInflater.from(viewGroup.getContext()), R.layout.item_setting_agree, viewGroup, false);
            RecyclerView.ViewHolder viewHolder = new ListItemViewHolder((ItemSettingAgreeBinding) itemBinding);
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
            ItemSettingAgreeBinding itemBinding;
            ModelAgreement agree;

            public ListItemViewHolder(ItemSettingAgreeBinding itemBinding) {
                super(itemBinding.getRoot());
                this.itemBinding = itemBinding;
                itemBinding.setHolder(this);
            }
            public void onTermViewClick(){
                Intent intent = new Intent(SettingActivity.this, TermsActivity.class);
                intent.putExtra("id", agree.id);
                startActivity(intent);
            }

            public void bindItem(int i) {
                this.agree = agreeList.get(i);
                itemBinding.setData(agree);
            }
        }

    }

    public void onConfirm(){}

    public void onLicense(){
        Intent intent = new Intent(this, LicenseActivity.class);
        startActivity(intent);
    }
    public void onFaq(){
        Intent intent = new Intent(this, FaqActivity.class);
        startActivity(intent);
    }
    public void onTerms(int nType){
        Intent intent = new Intent(this, TermsActivity.class);
        intent.putExtra("type", nType);
        startActivity(intent);
    }
    public void onVersion(){
        Intent intent = new Intent(this, VersionActivity.class);
        startActivity(intent);
    }
    public void onPassword(){
        AlertDialog.show(this).setText(getString(R.string.change_password_confirm), "", getString(R.string.confirm), getString(R.string.cancel))
                .setListener(()->{
                    Intent intent = new Intent(this, FindAuthActivity.class);
                    intent.putExtra("type", "password_reset");
                    intent.putExtra("user_id", DataManager.get().getModel(ModelUser.class).login_id);
                    startActivity(intent);
                });
    }
    public void onLogout(){
        AlertDialog.show(this).setText(getString(R.string.logout_confirm), "", getString(R.string.confirm), getString(R.string.cancel))
                .setListener(()->{
                    DataManager.get().removeModel(ModelUser.class);
                    Intent intent = new Intent(this, LoginHomeActivity.class);
                    startActivity(intent);
                    App.get().finishAllActivity();
                });
    }
    public void onWidthdraw(){
        Intent intent = new Intent(this, WithdrawActivity.class);
        startActivity(intent);
    }
}
